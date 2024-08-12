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

package com.alibaba.cloud.ai.tongyi.embedding;

import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import org.springframework.ai.embedding.EmbeddingOptions;

import java.util.List;

/**
 * @author why_ohh
 * @author yuluo
 * @author <a href="mailto:550588941@qq.com">why_ohh</a>
 * @since 2023.0.1.0
 */

public final class TongYiEmbeddingOptions implements EmbeddingOptions {

	private List<String> texts;

	private TextEmbeddingParam.TextType textType;

	public List<String> getTexts() {
		return texts;
	}

	public void setTexts(List<String> texts) {
		this.texts = texts;
	}

	public TextEmbeddingParam.TextType getTextType() {
		return textType;
	}

	public void setTextType(TextEmbeddingParam.TextType textType) {
		this.textType = textType;
	}

	public static Builder builder() {
		return new Builder();
	}

	public final static class Builder {

		private final TongYiEmbeddingOptions options;

		private Builder() {
			this.options = new TongYiEmbeddingOptions();
		}

		public Builder withtexts(List<String> texts) {

			options.setTexts(texts);
			return this;
		}

		public Builder withtextType(TextEmbeddingParam.TextType textType) {

			options.setTextType(textType);
			return this;
		}

		public TongYiEmbeddingOptions build() {

			return options;
		}

	}

}
