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

import cn.hutool.core.collection.ListUtil;
import com.alibaba.cloud.ai.tongyi.common.exception.TongYiException;
import com.alibaba.cloud.ai.tongyi.metadata.TongYiTextEmbeddingResponseMetadata;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.embeddings.TextEmbeddingResultItem;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link TongYiTextEmbeddingModel} implementation for {@literal Alibaba DashScope}.
 *
 * @author why_ohh
 * @author yuluo
 * @author <a href="mailto:550588941@qq.com">why_ohh</a>
 * @since 2023.0.1.0
 */

public class TongYiTextEmbeddingModel extends AbstractEmbeddingModel {

	private final Logger logger = LoggerFactory.getLogger(TongYiTextEmbeddingModel.class);

	/**
	 * TongYi Text Embedding client.
	 */
	private final TextEmbedding textEmbedding;

	/**
	 * {@link MetadataMode}.
	 */
	private final MetadataMode metadataMode;

	private final TongYiEmbeddingOptions defaultOptions;

	public TongYiTextEmbeddingModel(TextEmbedding textEmbedding) {

		this(textEmbedding, MetadataMode.EMBED);
	}

	public TongYiTextEmbeddingModel(TextEmbedding textEmbedding, MetadataMode metadataMode) {

		this(textEmbedding, metadataMode,
				TongYiEmbeddingOptions.builder()
						.withtextType(TextEmbeddingParam.TextType.DOCUMENT)
						.build()
		);
	}

	public TongYiTextEmbeddingModel(
			TextEmbedding textEmbedding,
			MetadataMode metadataMode,
			TongYiEmbeddingOptions options
	) {
		Assert.notNull(textEmbedding, "textEmbedding must not be null");
		Assert.notNull(metadataMode, "Metadata mode must not be null");
		Assert.notNull(options, "TongYiEmbeddingOptions must not be null");

		this.metadataMode = metadataMode;
		this.textEmbedding = textEmbedding;
		this.defaultOptions = options;
	}

	public TongYiEmbeddingOptions getDefaultOptions() {

		return this.defaultOptions;
	}

	@Override
	public List<Double> embed(Document document) {

		return this.call(
						new EmbeddingRequest(
								ListUtil.of(document.getFormattedContent(this.metadataMode)),
								null)
				).getResults().stream()
				.map(Embedding::getOutput)
				.flatMap(List::stream)
				.toList();
	}

	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {

		TextEmbeddingParam embeddingParams = toEmbeddingParams(request);
		logger.debug("Embedding request: {}", embeddingParams);
		TextEmbeddingResult resp;

		try {
			resp = textEmbedding.call(embeddingParams);
		}
		catch (NoApiKeyException e) {
			throw new TongYiException(e.getMessage());
		}

		return genEmbeddingResp(resp);
	}

	private EmbeddingResponse genEmbeddingResp(TextEmbeddingResult result) {

		return new EmbeddingResponse(
				genEmbeddingList(result.getOutput().getEmbeddings()),
				TongYiTextEmbeddingResponseMetadata.from(result.getUsage())
		);
	}

	private List<Embedding> genEmbeddingList(List<TextEmbeddingResultItem> embeddings) {

		return embeddings.stream()
				.map(embedding ->
						new Embedding(
								embedding.getEmbedding(),
								embedding.getTextIndex()
						))
				.collect(Collectors.toList());
	}

	/**
	 * We recommend setting the model parameters by passing the embedding parameters through the code;
	 * yml configuration compatibility is not considered here.
	 * It is not recommended that users set parameters from yml,
	 * as this reduces the flexibility of the configuration.
	 * Because the model name keeps changing, strings are used here and constants are undefined:
	 * Model list: <a href="https://help.aliyun.com/zh/dashscope/developer-reference/text-embedding-quick-start">Text Embedding Model List</a>
	 * @param requestOptions Client params. {@link EmbeddingRequest}
	 * @return {@link TextEmbeddingParam}
	 */
	private TextEmbeddingParam toEmbeddingParams(EmbeddingRequest requestOptions) {

		TextEmbeddingParam tongYiEmbeddingParams = TextEmbeddingParam.builder()
				.texts(requestOptions.getInstructions())
				.textType(defaultOptions.getTextType() != null ? defaultOptions.getTextType() : TextEmbeddingParam.TextType.DOCUMENT)
				.model("text-embedding-v1")
				.build();

		try {
			tongYiEmbeddingParams.validate();
		}
		catch (InputRequiredException e) {
			throw new TongYiException(e.getMessage());
		}

		return tongYiEmbeddingParams;
	}

}
