/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package cn.iocoder.dashboard.framework.tracer.skywalking;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import cn.iocoder.dashboard.framework.tracer.core.util.TracerUtils;

/**
 * Created by mashu on 2021/3/6.
 */
public class LocalLogbackPatternConverter extends ClassicConverter {
    /**
     * 作为默认的方式, 从{@link TracerUtils}中获取traceId,
     * 需要用这个去替代logback的Layout,
     * 同时避免了sky-walking agent生效的情况下仍然输出定值的问题.
     *
     * @param iLoggingEvent the event
     * @return the traceId: UUID, or the real traceId.
     */
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        return TracerUtils.getTraceId();
    }
}
