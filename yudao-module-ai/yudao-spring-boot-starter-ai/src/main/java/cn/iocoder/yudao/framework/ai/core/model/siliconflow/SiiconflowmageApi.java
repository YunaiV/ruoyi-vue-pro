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

package cn.iocoder.yudao.framework.ai.core.model.siliconflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.model.NoopApiKey;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Siiconflow Image API.
 *
 * @see <a href= "https://docs.siliconflow.cn/cn/api-reference/images/images-generations">Images</a>
 *
 * @author zzt
 */
public class SiiconflowmageApi {

	private final RestClient restClient;

	/**
	 * Create a new Siiconflow Image api with base URL set.
	 * @param aiToken OpenAI apiKey.
	 */
	public SiiconflowmageApi(String aiToken) {
		this(SiiconflowApiConstants.DEFAULT_BASE_URL, aiToken, RestClient.builder());
	}

	/**
	 * Create a new Siiconflow Image API with the provided base URL.
	 * @param baseUrl the base URL for the OpenAI API.
	 * @param openAiToken Siiconflow apiKey.
	 */
	public SiiconflowmageApi(String baseUrl, String openAiToken, RestClient.Builder restClientBuilder) {
		this(baseUrl, openAiToken, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}

	/**
	 * Create a new OpenAI Image API with the provided base URL.
	 * @param baseUrl the base URL for the OpenAI API.
	 * @param apiKey OpenAI apiKey.
	 * @param restClientBuilder the rest client builder to use.
	 */
	public SiiconflowmageApi(String baseUrl, String apiKey, RestClient.Builder restClientBuilder,
                             ResponseErrorHandler responseErrorHandler) {
		this(baseUrl, apiKey, CollectionUtils.toMultiValueMap(Map.of()), restClientBuilder, responseErrorHandler);
	}

	/**
	 * Create a new OpenAI Image API with the provided base URL.
	 * @param baseUrl the base URL for the OpenAI API.
	 * @param apiKey OpenAI apiKey.
	 * @param headers the http headers to use.
	 * @param restClientBuilder the rest client builder to use.
	 * @param responseErrorHandler the response error handler to use.
	 */
	public SiiconflowmageApi(String baseUrl, String apiKey, MultiValueMap<String, String> headers,
                             RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

		this(baseUrl, new SimpleApiKey(apiKey), headers, restClientBuilder, responseErrorHandler);
	}

	/**
	 * Create a new OpenAI Image API with the provided base URL.
	 * @param baseUrl the base URL for the OpenAI API.
	 * @param apiKey OpenAI apiKey.
	 * @param headers the http headers to use.
	 * @param restClientBuilder the rest client builder to use.
	 * @param responseErrorHandler the response error handler to use.
	 */
	public SiiconflowmageApi(String baseUrl, ApiKey apiKey, MultiValueMap<String, String> headers,
                             RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

		// @formatter:off
		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(h -> {
				if(!(apiKey instanceof NoopApiKey)) {
					h.setBearerAuth(apiKey.getValue());
				}
				h.setContentType(MediaType.APPLICATION_JSON);
				h.addAll(headers);
			})
			.defaultStatusHandler(responseErrorHandler)
			.build();
		// @formatter:on
	}

	public ResponseEntity<OpenAiImageApi.OpenAiImageResponse> createImage(SiliconflowImageRequest siliconflowImageRequest) {
		Assert.notNull(siliconflowImageRequest, "Image request cannot be null.");
		Assert.hasLength(siliconflowImageRequest.prompt(), "Prompt cannot be empty.");

		return this.restClient.post()
			.uri("v1/images/generations")
			.body(siliconflowImageRequest)
			.retrieve()
			.toEntity(OpenAiImageApi.OpenAiImageResponse.class);
	}


	// @formatter:off
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record SiliconflowImageRequest (
			@JsonProperty("prompt") String prompt,
			@JsonProperty("model") String model,
			@JsonProperty("batch_size") Integer batchSize,
			@JsonProperty("negative_prompt") String negativePrompt,
			@JsonProperty("seed") Integer seed,
			@JsonProperty("num_inference_steps") Integer numInferenceSteps,
			@JsonProperty("guidance_scale") Float guidanceScale,
			@JsonProperty("image") String image) {

		public SiliconflowImageRequest(String prompt, String model) {
			this(prompt, model, null, null, null, null, null, null);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to construct {@link SiiconflowmageApi} instance.
	 */
	public static class Builder {

		private String baseUrl = SiiconflowApiConstants.DEFAULT_BASE_URL;

		private ApiKey apiKey;

		private MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

		private RestClient.Builder restClientBuilder = RestClient.builder();

		private ResponseErrorHandler responseErrorHandler = RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER;

		public Builder baseUrl(String baseUrl) {
			Assert.hasText(baseUrl, "baseUrl cannot be null or empty");
			this.baseUrl = baseUrl;
			return this;
		}

		public Builder apiKey(ApiKey apiKey) {
			Assert.notNull(apiKey, "apiKey cannot be null");
			this.apiKey = apiKey;
			return this;
		}

		public Builder apiKey(String simpleApiKey) {
			Assert.notNull(simpleApiKey, "simpleApiKey cannot be null");
			this.apiKey = new SimpleApiKey(simpleApiKey);
			return this;
		}

		public Builder headers(MultiValueMap<String, String> headers) {
			Assert.notNull(headers, "headers cannot be null");
			this.headers = headers;
			return this;
		}

		public Builder restClientBuilder(RestClient.Builder restClientBuilder) {
			Assert.notNull(restClientBuilder, "restClientBuilder cannot be null");
			this.restClientBuilder = restClientBuilder;
			return this;
		}

		public Builder responseErrorHandler(ResponseErrorHandler responseErrorHandler) {
			Assert.notNull(responseErrorHandler, "responseErrorHandler cannot be null");
			this.responseErrorHandler = responseErrorHandler;
			return this;
		}

		public SiiconflowmageApi build() {
			Assert.notNull(this.apiKey, "apiKey must be set");
			return new SiiconflowmageApi(this.baseUrl, this.apiKey, this.headers, this.restClientBuilder,
					this.responseErrorHandler);
		}

	}

}
