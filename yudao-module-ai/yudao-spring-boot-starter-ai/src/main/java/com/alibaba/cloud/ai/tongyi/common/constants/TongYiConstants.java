/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.tongyi.common.constants;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public final class TongYiConstants {

	private TongYiConstants() {
	}

	/**
	 * Spring Cloud Alibaba AI configuration prefix.
	 */
	public static final String SCA_AI_CONFIGURATION = "spring.cloud.ai.tongyi.";

	/**
	 * Spring Cloud Alibaba AI constants prefix.
	 */
	public static final String SCA_AI = "SPRING_CLOUD_ALIBABA_";

	/**
	 * TongYi AI apikey env name.
	 */
	public static final String SCA_AI_TONGYI_API_KEY = SCA_AI + "TONGYI_API_KEY";

}
