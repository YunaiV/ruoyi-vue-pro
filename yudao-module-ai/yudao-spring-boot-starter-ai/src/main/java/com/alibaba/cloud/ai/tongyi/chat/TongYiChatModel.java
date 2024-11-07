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

package com.alibaba.cloud.ai.tongyi.chat;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.cloud.ai.tongyi.common.exception.TongYiException;
import com.alibaba.dashscope.aigc.conversation.ConversationParam;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.alibaba.dashscope.tools.ToolCallBase;
import com.alibaba.dashscope.tools.ToolCallFunction;
import com.alibaba.dashscope.utils.ApiKeywords;
import com.alibaba.dashscope.utils.JsonUtils;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * {@link ChatModel} and {@link StreamingChatModel} implementation for {@literal Alibaba DashScope}
 * backed by {@link Generation}.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 * @see ChatModel
 * @see com.alibaba.dashscope.aigc.generation
 */

public class TongYiChatModel extends
		AbstractFunctionCallSupport<
				com.alibaba.dashscope.common.Message,
				ConversationParam,
				GenerationResult>
		implements ChatModel, StreamingChatModel {

	private static final Logger logger = LoggerFactory.getLogger(TongYiChatModel.class);

	/**
	 * DashScope generation client.
	 */
	private final Generation generation;

	/**
	 * The TongYi models default chat completion api.
	 */
	private TongYiChatOptions defaultOptions;

	/**
	 * User role message manager.
	 */
	@Autowired
	private MessageManager msgManager;

	/**
	 * Initializes an instance of the TongYiChatClient.
	 * @param generation DashScope generation client.
	 */
	public TongYiChatModel(Generation generation) {

		this(generation,
				TongYiChatOptions.builder()
						.withTopP(0.8)
						.withEnableSearch(true)
						.withResultFormat(ConversationParam.ResultFormat.MESSAGE)
						.build(),
				null
		);
	}

	/**
	 * Initializes an instance of the TongYiChatClient.
	 * @param generation DashScope generation client.
	 * @param options TongYi model params.
	 */
	public TongYiChatModel(Generation generation, TongYiChatOptions options) {

		this(generation, options, null);
	}

	/**
	 * Create a TongYi models client.
	 * @param generation DashScope model generation client.
	 * @param options TongYi default chat completion api.
	 */
	public TongYiChatModel(Generation generation, TongYiChatOptions options,
			FunctionCallbackContext functionCallbackContext) {

		super(functionCallbackContext);
		this.generation = generation;
		this.defaultOptions = options;
	}

	/**
	 * Get default sca chat options.
	 *
	 * @return TongYiChatOptions default object.
	 */
	public TongYiChatOptions getDefaultOptions() {

		return this.defaultOptions;
	}

	@Override
	public ChatResponse call(Prompt prompt) {

		ConversationParam params = toTongYiChatParams(prompt);

		// TongYi models context loader.
		com.alibaba.dashscope.common.Message message = new com.alibaba.dashscope.common.Message();
		message.setRole(Role.USER.getValue());
		message.setContent(prompt.getContents());
		msgManager.add(message);
		params.setMessages(msgManager.get());

		logger.trace("TongYi ConversationOptions: {}", params);
		GenerationResult chatCompletions = this.callWithFunctionSupport(params);
		logger.trace("TongYi ConversationOptions: {}", params);

		msgManager.add(chatCompletions);

		List<org.springframework.ai.chat.model.Generation> generations =
				chatCompletions
						.getOutput()
						.getChoices()
						.stream()
						.map(choice ->
								new org.springframework.ai.chat.model.Generation(
										choice
												.getMessage()
												.getContent()
								).withGenerationMetadata(generateChoiceMetadata(choice)
								))
						.toList();

		return new ChatResponse(generations);

	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {

		Flowable<GenerationResult> genRes;
		ConversationParam tongYiChatParams = toTongYiChatParams(prompt);

		// See https://help.aliyun.com/zh/dashscope/developer-reference/api-details?spm=a2c4g.11186623.0.0.655fc11aRR0jj7#b9ad0a10cfhpe
		// tongYiChatParams.setIncrementalOutput(true);

		try {
			genRes = generation.streamCall(tongYiChatParams);
		}
		catch (NoApiKeyException | InputRequiredException e) {
			logger.warn("TongYi chat client: " + e.getMessage());
			throw new TongYiException(e.getMessage());
		}

		return Flux.from(genRes)
				.flatMap(
						message -> Flux.just(
										message.getOutput()
												.getChoices()
												.get(0)
												.getMessage()
												.getContent())
								.map(content -> {
									var gen = new org.springframework.ai.chat.model.Generation(content)
											.withGenerationMetadata(generateChoiceMetadata(
													message.getOutput()
															.getChoices()
															.get(0)
											));
									return new ChatResponse(ListUtil.of(gen));
								})
				)
				.publishOn(Schedulers.parallel());

	}

	/**
	 * Configuration properties to Qwen model params.
	 * Test access.
	 *
	 * @param prompt {@link Prompt}
	 * @return Qwen models params {@link ConversationParam}
	 */
	public ConversationParam toTongYiChatParams(Prompt prompt) {

		Set<String> functionsForThisRequest = new HashSet<>();

		List<com.alibaba.dashscope.common.Message> tongYiMessage = prompt.getInstructions().stream()
				.map(this::fromSpringAIMessage)
				.toList();

		ConversationParam chatParams = ConversationParam.builder()
				.messages(tongYiMessage)
				// models setting
				// {@link HalfDuplexServiceParam#models}
				.model(Generation.Models.QWEN_TURBO)
				// {@link GenerationOutput}
				.resultFormat(ConversationParam.ResultFormat.MESSAGE)
				.incrementalOutput(true)

				.build();

		if (this.defaultOptions != null) {

			chatParams = merge(chatParams, this.defaultOptions);
			Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions, !IS_RUNTIME_CALL);
			functionsForThisRequest.addAll(defaultEnabledFunctions);
		}
		if (prompt.getOptions() != null) {
			if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
				TongYiChatOptions updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions,
						ChatOptions.class, TongYiChatOptions.class);

				chatParams = merge(updatedRuntimeOptions, chatParams);

				Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions,
						IS_RUNTIME_CALL);
				functionsForThisRequest.addAll(promptEnabledFunctions);

			}
			else {
				throw new IllegalArgumentException("Prompt options are not of type ConversationParam:"
						+ prompt.getOptions().getClass().getSimpleName());
			}
		}

		// Add the enabled functions definitions to the request's tools parameter.

		if (!CollectionUtils.isEmpty(functionsForThisRequest)) {
			List<FunctionDefinition> tools = this.getFunctionTools(functionsForThisRequest);

			// todo chatParams.setTools(tools)
		}

		return chatParams;
	}

	private ChatGenerationMetadata generateChoiceMetadata(GenerationOutput.Choice choice) {

		return ChatGenerationMetadata.from(
				String.valueOf(choice.getFinishReason()),
				choice.getMessage().getContent()
		);
	}

	private List<FunctionDefinition> getFunctionTools(Set<String> functionNames) {
		return this.resolveFunctionCallbacks(functionNames).stream().map(functionCallback -> {

			FunctionDefinition functionDefinition = FunctionDefinition.builder()
					.name(functionCallback.getName())
					.description(functionCallback.getDescription())
					.parameters(JsonUtils.parametersToJsonObject(
							ModelOptionsUtils.jsonToMap(functionCallback.getInputTypeSchema())
					))
					.build();

			return functionDefinition;
		}).toList();
	}


	private ConversationParam merge(ConversationParam tongYiParams, TongYiChatOptions scaChatParams) {

		if (scaChatParams == null) {

			return tongYiParams;
		}

		return ConversationParam.builder()
				.messages(tongYiParams.getMessages())
				.maxTokens((tongYiParams.getMaxTokens() != null) ? tongYiParams.getMaxTokens() : scaChatParams.getMaxTokens())
				// When merge options. Because ConversationParams is must not null. So is setting.
				.model(scaChatParams.getModel())
				.resultFormat((tongYiParams.getResultFormat() != null) ? tongYiParams.getResultFormat() : scaChatParams.getResultFormat())
				.enableSearch((tongYiParams.getEnableSearch() != null) ? tongYiParams.getEnableSearch() : scaChatParams.getEnableSearch())
				.topK((tongYiParams.getTopK() != null) ? tongYiParams.getTopK() : scaChatParams.getTopK())
				.topP((tongYiParams.getTopP() != null) ? tongYiParams.getTopP() : scaChatParams.getTopP())
				.incrementalOutput((tongYiParams.getIncrementalOutput() != null) ? tongYiParams.getIncrementalOutput() : scaChatParams.getIncrementalOutput())
				.temperature((tongYiParams.getTemperature() != null) ? tongYiParams.getTemperature() : scaChatParams.getTemperature())
				.repetitionPenalty((tongYiParams.getRepetitionPenalty() != null) ? tongYiParams.getRepetitionPenalty() : scaChatParams.getRepetitionPenalty())
				.seed((tongYiParams.getSeed() != null) ? tongYiParams.getSeed() : scaChatParams.getSeed())
				.build();

	}

	private ConversationParam merge(TongYiChatOptions scaChatParams, ConversationParam tongYiParams) {

		if (scaChatParams == null) {

			return tongYiParams;
		}

		ConversationParam mergedTongYiParams = ConversationParam.builder()
				.model(Generation.Models.QWEN_TURBO)
				.messages(tongYiParams.getMessages())
				.build();
		mergedTongYiParams = merge(tongYiParams, scaChatParams);

		if (scaChatParams.getMaxTokens() != null) {
			mergedTongYiParams.setMaxTokens(scaChatParams.getMaxTokens());
		}

		if (scaChatParams.getStop() != null) {
			mergedTongYiParams.setStopStrings(scaChatParams.getStop());
		}

		if (scaChatParams.getTemperature() != null) {
			mergedTongYiParams.setTemperature(scaChatParams.getTemperature());
		}

		if (scaChatParams.getTopK() != null) {
			mergedTongYiParams.setTopK(scaChatParams.getTopK());
		}

		if (scaChatParams.getTopK() != null) {
			mergedTongYiParams.setTopK(scaChatParams.getTopK());
		}

		return mergedTongYiParams;
	}

	private com.alibaba.dashscope.common.Message fromSpringAIMessage(Message message) {

		return switch (message.getMessageType()) {
			case USER -> com.alibaba.dashscope.common.Message.builder()
					.role(Role.USER.getValue())
					.content(message.getContent())
					.build();
			case SYSTEM -> com.alibaba.dashscope.common.Message.builder()
					.role(Role.SYSTEM.getValue())
					.content(message.getContent())
					.build();
			case ASSISTANT -> com.alibaba.dashscope.common.Message.builder()
					.role(Role.ASSISTANT.getValue())
					.content(message.getContent())
					.build();
			default -> throw new IllegalArgumentException("Unknown message type " + message.getMessageType());
		};

	}

	@Override
	protected ConversationParam doCreateToolResponseRequest(
			ConversationParam previousRequest,
			com.alibaba.dashscope.common.Message responseMessage,
			List<com.alibaba.dashscope.common.Message> conversationHistory
	) {
		for (ToolCallBase toolCall : responseMessage.getToolCalls()) {
			if (toolCall instanceof ToolCallFunction toolCallFunction) {
				if (toolCallFunction.getFunction() != null) {
					var functionName = toolCallFunction.getFunction().getName();
					var functionArguments = toolCallFunction.getFunction().getArguments();

					if (!this.functionCallbackRegister.containsKey(functionName)) {
						throw new IllegalStateException("No function callback found for function name: " + functionName);
					}

					String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);

					// Add the function response to the conversation.
					conversationHistory
							.add(com.alibaba.dashscope.common.Message.builder()
									.content(functionResponse)
									.role(Role.BOT.getValue())
									.toolCallId(toolCall.getId())
									.build()
							);
				}
			}

		}

		ConversationParam newRequest = ConversationParam.builder().messages(conversationHistory).build();

		// todo: No @JsonProperty fields.
		newRequest = ModelOptionsUtils.merge(newRequest, previousRequest, ConversationParam.class);

		return newRequest;

	}

	@Override
	protected List<com.alibaba.dashscope.common.Message> doGetUserMessages(ConversationParam request) {

		return request.getMessages();
	}

	@Override
	protected com.alibaba.dashscope.common.Message doGetToolResponseMessage(GenerationResult response) {

		var message = response.getOutput().getChoices().get(0).getMessage();
		var assistantMessage = com.alibaba.dashscope.common.Message.builder().role(Role.ASSISTANT.getValue())
				.content("").build();
		assistantMessage.setToolCalls(message.getToolCalls());

		return assistantMessage;
	}

	@Override
	protected GenerationResult doChatCompletion(ConversationParam request) {

		GenerationResult result;
		try {
			result = generation.call(request);
		}
		catch (NoApiKeyException | InputRequiredException e) {
			throw new RuntimeException(e);
		}

		return result;
	}

	@Override
	protected Flux<GenerationResult> doChatCompletionStream(ConversationParam request) {
		final Flowable<GenerationResult> genRes;
		try {
			genRes = generation.streamCall(request);
		}
		catch (NoApiKeyException | InputRequiredException e) {
			logger.warn("TongYi chat client: " + e.getMessage());
			throw new TongYiException(e.getMessage());
		}
		return Flux.from(genRes);

	}

	@Override
	protected boolean isToolFunctionCall(GenerationResult response) {

		if (response == null || CollectionUtils.isEmpty(response.getOutput().getChoices())) {

			return false;
		}
		var choice = response.getOutput().getChoices().get(0);
		if (choice == null || choice.getFinishReason() == null) {

			return false;
		}

		return Objects.equals(choice.getFinishReason(), ApiKeywords.TOOL_CALLS);
	}
}
