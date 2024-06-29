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

package com.alibaba.cloud.ai.tongyi.metadata;

import com.alibaba.dashscope.embeddings.TextEmbeddingUsage;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;

/**
 * @author why_ohh
 * @author yuluo
 * @author <a href="mailto:550588941@qq.com">why_ohh</a>
 * @since 2023.0.1.0
 */

public class TongYiTextEmbeddingResponseMetadata extends EmbeddingResponseMetadata {

	private Integer totalTokens;

	protected TongYiTextEmbeddingResponseMetadata(Integer totalTokens) {

		this.totalTokens = totalTokens;
	}

	public static TongYiTextEmbeddingResponseMetadata from(TextEmbeddingUsage usage) {

		return new TongYiTextEmbeddingResponseMetadata(usage.getTotalTokens());
	}

	public Integer getTotalTokens() {

		return totalTokens;
	}

	public void setTotalTokens(Integer totalTokens) {

		this.totalTokens = totalTokens;
	}

}
