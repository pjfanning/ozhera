/*
 * Copyright (C) 2020 Xiaomi Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.ozhera.log.manager.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.Lists;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.common.Utils;
import org.apache.ozhera.log.manager.common.utils.ManagerUtil;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.domain.EsCluster;
import org.apache.ozhera.log.manager.domain.SearchLog;
import org.apache.ozhera.log.manager.model.StatisticsQuery;
import org.apache.ozhera.log.manager.model.dto.EsStatisticsKeyWord;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.vo.LogQuery;
import org.apache.ozhera.log.manager.service.StatisticsService;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.ozhera.log.common.Constant.GSON;
import static org.apache.ozhera.log.manager.common.utils.ManagerUtil.getKeyColonPrefix;

@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {


    @Resource
    private MilogLogstoreDao logstoreDao;

    @Resource
    private EsCluster esCluster;


    @Resource
    private SearchLog searchLog;


    /**
     * Hourly data volume statistics for a single tail
     */
    @Override
    public Result<Map<String, Long>> queryTailStatisticsByHour(StatisticsQuery statisticsQuery) throws IOException {

        Map<String, Long> result = new LinkedHashMap<>();
        if (statisticsQuery.getStartTime() == null || statisticsQuery.getStartTime() == 0) {
            statisticsQuery.setStartTime(Utils.getTodayTime().get("start"));
            statisticsQuery.setEndTime(Utils.getTodayTime().get("end"));
        }
        MilogLogStoreDO milogLogstoreDO = logstoreDao.queryById(statisticsQuery.getLogstoreId());
        if (milogLogstoreDO == null) {
            return Result.success();
        }
        EsService esService = esCluster.getEsService(milogLogstoreDO.getEsClusterId());
        String esIndexName = milogLogstoreDO.getEsIndex();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders
                .rangeQuery("timestamp")
                .gte(statisticsQuery.getStartTime())
                .lte(statisticsQuery.getEndTime()));
        boolQueryBuilder.filter(QueryBuilders.termQuery("logstore", milogLogstoreDO.getLogstoreName()));
        boolQueryBuilder.filter(QueryBuilders.termQuery("tail", statisticsQuery.getTail()));
        AbstractAggregationBuilder aggregation = AggregationBuilders
                .dateHistogram("agg")
                .field("timestamp")
                .calendarInterval(DateHistogramInterval.hours(1))
                .format("yyyy-MM-dd HH:mm:ss")
                .timeZone(ZoneId.of("+08:00"))
                .minDocCount(0L);
        builder.query(boolQueryBuilder);
        builder.aggregation(aggregation);
        SearchRequest searchRequest = new SearchRequest(esIndexName);
        searchRequest.source(builder);
        SearchResponse searchResponse = esService.search(searchRequest);
        ParsedDateHistogram parsedDateHistogram = (ParsedDateHistogram) searchResponse.getAggregations().asMap().get("agg");
        for (Histogram.Bucket bucket : parsedDateHistogram.getBuckets()) {
            result.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return Result.success(result);
    }


    /**
     * Top 5 in the data volume of all tails in a single store on the day
     */
    @Override
    public Result<Map<String, Long>> queryStoreTopTailStatisticsByDay(StatisticsQuery statisticsQuery) throws IOException {

        if (statisticsQuery.getStartTime() == null || statisticsQuery.getStartTime() == 0) {
            statisticsQuery.setStartTime(Utils.getTodayTime().get("start"));
            statisticsQuery.setEndTime(Utils.getTodayTime().get("end"));
        }
        MilogLogStoreDO logstoreDO = logstoreDao.queryById(statisticsQuery.getLogstoreId());
        if (logstoreDO == null) {
            return Result.success();
        }
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders
                .rangeQuery("timestamp")
                .from(statisticsQuery.getStartTime())
                .to(statisticsQuery.getEndTime()));
        boolQueryBuilder.filter(QueryBuilders
                .termQuery("logstore", logstoreDO.getLogstoreName()));
        TermsAggregationBuilder tailAggr = AggregationBuilders.terms("tail");
        tailAggr.field("tail");
        tailAggr.order(BucketOrder.count(false));
        searchSourceBuilder.query(boolQueryBuilder).aggregation(tailAggr).size(0);
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(logstoreDO.getEsIndex());
        EsService esService = esCluster.getEsService(logstoreDO.getEsClusterId());
        SearchResponse searchResponse = esService.search(searchRequest);
        ParsedStringTerms aggregations = searchResponse.getAggregations().get("tail");
        Map<String, Long> result = new LinkedHashMap<>();
        for (Terms.Bucket bucket : aggregations.getBuckets()) {
            result.put(String.valueOf(bucket.getKey()), bucket.getDocCount());
        }
        return Result.success(result);
    }

    /**
     * Top 5 in the volume of data of all stores in a single space
     */
    @Override
    public Result<Map<String, Long>> querySpaceTopStoreByDay(StatisticsQuery statisticsQuery) throws IOException {

        Map<String, Long> result = new LinkedHashMap<>();
        if (statisticsQuery.getStartTime() == null || statisticsQuery.getStartTime() == 0) {
            statisticsQuery.setStartTime(Utils.getTodayTime().get("start"));
            statisticsQuery.setEndTime(Utils.getTodayTime().get("end"));
        }
        List<MilogLogStoreDO> logstoreList = logstoreDao.getMilogLogstoreBySpaceId(statisticsQuery.getSpaceId());
        if (null != logstoreList && logstoreList.size() > 0) {
            for (MilogLogStoreDO logstoreDO : logstoreList) {
                SearchRequest searchRequest = new SearchRequest();
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                boolQueryBuilder.filter(QueryBuilders
                        .rangeQuery("timestamp")
                        .from(statisticsQuery.getStartTime())
                        .to(statisticsQuery.getEndTime()));
                boolQueryBuilder.filter(QueryBuilders
                        .termQuery("logstore", logstoreDO.getLogstoreName()));
                TermsAggregationBuilder tailAggr = AggregationBuilders.terms("logstore");
                tailAggr.field("logstore");
                tailAggr.order(BucketOrder.count(false));
                searchSourceBuilder.query(boolQueryBuilder).aggregation(tailAggr).size(0);
                searchRequest.source(searchSourceBuilder);
                searchRequest.indices(logstoreDO.getEsIndex());
                EsService esService = esCluster.getEsService(logstoreDO.getEsClusterId());
                SearchResponse searchResponse = esService.search(searchRequest);
                ParsedStringTerms aggregations = searchResponse.getAggregations().get("logstore");
                for (Terms.Bucket bucket : aggregations.getBuckets()) {
                    result.put(String.valueOf(bucket.getKey()), bucket.getDocCount());
                }
            }
        }
        return Result.success(result);
    }
    
    @Override
    public Result<List<EsStatisticsKeyWord>> queryEsStatisticsRation(LogQuery logQuery) {
        List<EsStatisticsKeyWord> results = Lists.newArrayList();
        if (null == logQuery.getStoreId()) {
            return Result.failParam("The storeId cannot be empty");
        }
        MilogLogStoreDO logStoreDO = logstoreDao.queryById(logQuery.getStoreId());
        if (null == logStoreDO) {
            return Result.fail(CommonError.NOT_EXISTS_DATA.getCode(), "The store does not exist");
        }
        if (null == logStoreDO.getEsClusterId() || StringUtils.isEmpty(logStoreDO.getEsIndex())) {
            return Result.fail(CommonError.NOT_EXISTS_DATA.getCode(), "ES index-related information does not exist");
        }
        EsService esService = esCluster.getEsService(logStoreDO.getEsClusterId());
        String esIndexName = logStoreDO.getEsIndex();

        List<String> keyColons = ManagerUtil.getKeyColonPrefix(logStoreDO.getKeyList());
        for (String field : keyColons) {
            EsStatisticsKeyWord esStatisticsKeyWord = new EsStatisticsKeyWord();
            esStatisticsKeyWord.setKey(field);
            try {
                String aggregationName = String.format("%s-%s", field, "static-name");
                BoolQueryBuilder boolQueryBuilder = searchLog.getQueryBuilder(logQuery, getKeyColonPrefix(logStoreDO.getKeyList()));

                // Create a search request
                SearchRequest searchRequest = new SearchRequest(esIndexName);
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(boolQueryBuilder);
                searchSourceBuilder.size(5);
                searchRequest.source(searchSourceBuilder);

                // Perform a search request
                SearchResponse searchResponse = esService.search(searchRequest);

                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(boolQueryBuilder);
                sourceBuilder.aggregation(AggregationBuilders.terms(aggregationName)
                        .field(field)
                        .size(5)
                        .subAggregation(AggregationBuilders.sum("total")
                                .field(field)));
                sourceBuilder.sort(new FieldSortBuilder(field).order(SortOrder.DESC));

                SearchRequest aggregationRequest = new SearchRequest(esIndexName);
                aggregationRequest.source(sourceBuilder);

                SearchResponse aggregationResponse = esService.search(aggregationRequest);
                if (null != aggregationResponse.getAggregations()) {
                    Terms terms = aggregationResponse.getAggregations().get(aggregationName);
                    List<EsStatisticsKeyWord.StatisticsRation> staticsKeyWords = Lists.newArrayList();
                    for (Terms.Bucket bucket : terms.getBuckets()) {
                        String fieldValue = bucket.getKeyAsString();
                        Sum sum = bucket.getAggregations().get("total");
                        double total = sum.getValue();
                        double percentage = NumberUtil.div(total, searchResponse.getHits().getTotalHits().value, 4);
                        EsStatisticsKeyWord.StatisticsRation statisticsKeyWord = new EsStatisticsKeyWord.StatisticsRation();
                        statisticsKeyWord.setValue(fieldValue);
                        statisticsKeyWord.setRation(percentage + "");
                        staticsKeyWords.add(statisticsKeyWord);
                    }
                    esStatisticsKeyWord.setStatisticsRation(staticsKeyWords);
                    results.add(esStatisticsKeyWord);
                }
            } catch (Exception e) {
                log.error("query es index exception,field:{},logQuery:{}", field, GSON.toJson(logQuery), e);
            }
        }

        return Result.success(results);
    }


}
