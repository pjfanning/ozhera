/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.log.manager.service.impl;

import com.google.gson.Gson;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/9/23 15:11
 */
@Slf4j
public class MqConfigServiceTest {

    private Gson gson = new Gson();

    String ak = "";
    String sk = "/";
    String serviceUrl = "http://127.0.0.1";
    String nameServer = "127.0.0.1:9876";
    String authorization = "";
    String orgId = "";
    String teamId = "";
    Long appId = 23L;
    String appName = "wudalang";
    String source = "china";
    Long spaceId = 234L;
    Long storeId = 456L;
    Long tailId = 789L;

    @Test
    public void testTalosCreateTopic() {
        Ioc.ins().init("com.xiaomi");
    }


    @Test
    public void testTalosCreateTopicTag() {
        Ioc.ins().init("com.xiaomi");
    }

    @Test
    public void testRocketmqCreateTopicTag() {
        Ioc.ins().init("com.xiaomi");
        RocketMqConfigService mqConfigService = Ioc.ins().getBean(RocketMqConfigService.class);
        String nameServer = "127.0.0.1:9876";
//        List<String> commonTagTopic = mqConfigService.createCommonTagTopic(
//                ak, sk, nameServer, serviceUrl, "", orgId, teamId);
//        log.info("assemble common topic tag:{}", gson.toJson(commonTagTopic));
    }

    @Test
    public void testTalosQueryTopic() {
        Ioc.ins().init("com.xiaomi");
    }

    @Test
    public void testRockerMqQueryTopic() {
        Ioc.ins().init("com.xiaomi");
        RocketMqConfigService mqConfigService = Ioc.ins().getBean(RocketMqConfigService.class);
//        List<DictionaryDTO> dictionaryDTOS = mqConfigService.queryExistsTopic(ak, sk, "", serviceUrl, "", orgId, teamId);
//        System.out.println(dictionaryDTOS);
    }

    @Test
    public void testMqChooseTopic() {
        Ioc.ins().init("com.xiaomi");
        RocketMqConfigService mqConfigService = Ioc.ins().getBean(RocketMqConfigService.class);
//        MilogAppMiddlewareRel.Config config = mqConfigService.generateConfig(ak, sk, nameServer, serviceUrl, authorization, orgId, teamId, appId, appName, source, 12L);
//        System.out.println(config);
    }


    @Test
    public void testMqCreateGroup() {
        Ioc.ins().init("com.xiaomi");
        RocketMqConfigService mqConfigService = Ioc.ins().getBean(RocketMqConfigService.class);
//        boolean isSuccess = mqConfigService.createSubscribeGroup(serviceUrl, authorization, orgId, spaceId, storeId, tailId, null);
//        System.out.println(isSuccess);
    }

    @Test
    public void testQuerySubGroup() {
        Ioc.ins().init("com.xiaomi");
        RocketMqConfigService mqConfigService = Ioc.ins().getBean(RocketMqConfigService.class);
//        List<RocketMqResponseDTO.SubGroup> subGroupList = mqConfigService.querySubGroupList(serviceUrl, authorization, orgId);
//        System.out.println(gson.toJson(subGroupList));
    }

    @Test
    public void testMqDeleteGroup() {
        Ioc.ins().init("com.xiaomi");
        RocketMqConfigService mqConfigService = Ioc.ins().getBean(RocketMqConfigService.class);
//        boolean isSuccess = mqConfigService.deleteSubscribeGroup(serviceUrl, authorization, orgId, spaceId, storeId, tailId);
//        System.out.println(isSuccess);
    }

    @Test
    public void testTalosDeleteTopic() {
        String topicName = "";
        Ioc.ins().init("com.xiaomi");
    }
}
