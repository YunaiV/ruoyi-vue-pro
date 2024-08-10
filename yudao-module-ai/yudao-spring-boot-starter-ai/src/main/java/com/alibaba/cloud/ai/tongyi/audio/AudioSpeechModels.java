/*
 * Copyright 2023-2024 the original author or authors.
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

package com.alibaba.cloud.ai.tongyi.audio;

/**
 * More models see: https://help.aliyun.com/zh/dashscope/model-list?spm=a2c4g.11186623.0.i5
 * Support all models in list.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public final class AudioSpeechModels {

	private AudioSpeechModels() {
	}

	/**
	 * Male Voice of the Tongue(舌尖男声).
	 * zh & en.
	 * Default sample rate: 48 Hz.
	 */
	public static final String SAMBERT_ZHICHU_V1 = "sambert-zhichu-v1";

}
