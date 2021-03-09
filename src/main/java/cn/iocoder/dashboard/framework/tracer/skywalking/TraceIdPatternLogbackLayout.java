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

import ch.qos.logback.classic.PatternLayout;

/**
 * Based on the logback-component convert register mechanism,
 * register {@link LocalLogbackPatternConverter} as a new convert, match to "tid".
 * You can use "%tid" in logback config file, "Pattern" section.
 * If sky-walking agent is not active mode, it will use UUID as tid.
 * <p>
 * logback 的转换组件,为tid 添加占位符的转换器
 * Created by mashu on 2021/3/6.
 */
public class TraceIdPatternLogbackLayout extends PatternLayout {
    static {
        defaultConverterMap.put("tid", LocalLogbackPatternConverter.class.getName());
    }
}
