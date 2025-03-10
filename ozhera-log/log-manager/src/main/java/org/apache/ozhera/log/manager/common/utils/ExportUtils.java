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
package org.apache.ozhera.log.manager.common.utils;

import org.apache.ozhera.log.manager.model.dto.LogDataDTO;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Log download utility class
 */
public class ExportUtils {

    // The HSSFWorkbook4Map method writes xls with a maximum cell length limit of 32767, which splits very long content
    public static Map<String, Object> SplitTooLongContent(LogDataDTO logDataDto) {
        int maxCellLen = 32767;
        Map<String, Object> logOfKV = logDataDto.getLogOfKV();
        Map<String, Object> newLogOfKV = new LinkedHashMap<>();
        Iterator<Map.Entry<String, Object>> it = logOfKV.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String keyStr = entry.getKey();
            String valueStr = entry.getValue() == null ? "" : entry.getValue().toString();
            int entryLen = valueStr.length();
            if (entryLen > maxCellLen) {
                int cnt = entryLen / maxCellLen;
                if (entryLen % maxCellLen != 0) {
                    cnt++;
                }
                for (int i = 0; i < cnt; i++) {
                    String entryKey = String.format("%s-%d", keyStr, i);
                    String entryValue = "";
                    int end = (i + 1) * maxCellLen;
                    if (end > entryLen) {
                        end = entryLen;
                    }
                    entryValue = valueStr.substring(i * maxCellLen, end);
                    newLogOfKV.put(entryKey, entryValue);
                }
            } else {
                newLogOfKV.put(keyStr, entry.getValue());
            }
        }
        return newLogOfKV;
    }
}
