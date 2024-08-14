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
package com.xiaomi.mone.monitor.listener;

import com.xiaomi.mone.monitor.service.rocketmq.RocketMqHeraMonitorConsumer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author gaoxihui
 * @date 2021/7/9 10:23 PM
 */
@Slf4j
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {

    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {

        log.info("accept and process ApplicationStartedEvent ... ");
        RocketMqHeraMonitorConsumer rocketMqConsumerHera = (RocketMqHeraMonitorConsumer) applicationStartedEvent.getApplicationContext().getBean("heraMonitorMqConsumer");
        rocketMqConsumerHera.start();
        log.info("process ApplicationStartedEvent finish ... ");
    }

}
