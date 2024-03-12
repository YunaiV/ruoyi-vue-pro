/*
 * Copyright 2024-2024 the original author or authors.
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

package cn.iocoder.yudao.framework.ai.model.function;

import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christian Tzolov
 */
public abstract class AbstractFunctionCallSupport<Msg, Req, Resp> {

	protected final static boolean IS_RUNTIME_CALL = true;

	/**
	 * The function callback register is used to resolve the function callbacks by name.
	 */
	protected final Map<String, FunctionCallback> functionCallbackRegister = new ConcurrentHashMap<>();

	/**
	 * The function callback context is used to resolve the function callbacks by name
	 * from the Spring context. It is optional and usually used with Spring
	 * auto-configuration.
	 */
	protected final FunctionCallbackContext functionCallbackContext;

	public AbstractFunctionCallSupport(FunctionCallbackContext functionCallbackContext) {
		this.functionCallbackContext = functionCallbackContext;
	}

	public Map<String, FunctionCallback> getFunctionCallbackRegister() {
		return this.functionCallbackRegister;
	}

	protected Set<String> handleFunctionCallbackConfigurations(FunctionCallingOptions options, boolean isRuntimeCall) {

		Set<String> functionToCall = new HashSet<>();

		if (options != null) {
			if (!CollectionUtils.isEmpty(options.getFunctionCallbacks())) {
				options.getFunctionCallbacks().stream().forEach(functionCallback -> {

					// Register the tool callback.
					if (isRuntimeCall) {
						this.functionCallbackRegister.put(functionCallback.getName(), functionCallback);
					}
					else {
						this.functionCallbackRegister.putIfAbsent(functionCallback.getName(), functionCallback);
					}

					// Automatically enable the function, usually from prompt callback.
					if (isRuntimeCall) {
						functionToCall.add(functionCallback.getName());
					}
				});
			}

			// Add the explicitly enabled functions.
			if (!CollectionUtils.isEmpty(options.getFunctions())) {
				functionToCall.addAll(options.getFunctions());
			}
		}

		return functionToCall;
	}

	/**
	 * Resolve the function callbacks by name. Retrieve them from the registry or try to
	 * resolve them from the Application Context.
	 * @param functionNames Name of function callbacks to retrieve.
	 * @return list of resolved FunctionCallbacks.
	 */
	protected List<FunctionCallback> resolveFunctionCallbacks(Set<String> functionNames) {

		List<FunctionCallback> retrievedFunctionCallbacks = new ArrayList<>();

		for (String functionName : functionNames) {
			if (!this.functionCallbackRegister.containsKey(functionName)) {

				if (this.functionCallbackContext != null) {
					FunctionCallback functionCallback = this.functionCallbackContext.getFunctionCallback(functionName,
							null);
					if (functionCallback != null) {
						this.functionCallbackRegister.put(functionName, functionCallback);
					}
					else {
						throw new IllegalStateException(
								"No function callback [" + functionName + "] fund in tht FunctionCallbackContext");
					}
				}
				else {
					throw new IllegalStateException("No function callback found for name: " + functionName);
				}
			}
			FunctionCallback functionCallback = this.functionCallbackRegister.get(functionName);

			retrievedFunctionCallbacks.add(functionCallback);
		}

		return retrievedFunctionCallbacks;
	}

	///
	protected Resp callWithFunctionSupport(Req request) {
		Resp response = this.doChatCompletion(request);
		return this.handleFunctionCallOrReturn(request, response);
	}

	protected Resp handleFunctionCallOrReturn(Req request, Resp response) {

		if (!this.isToolFunctionCall(response)) {
			return response;
		}

		// The chat completion tool call requires the complete conversation
		// history. Including the initial user message.
		List<Msg> conversationHistory = new ArrayList<>();

		conversationHistory.addAll(this.doGetUserMessages(request));

		Msg responseMessage = this.doGetToolResponseMessage(response);

		// Add the assistant response to the message conversation history.
		conversationHistory.add(responseMessage);

		Req newRequest = this.doCreateToolResponseRequest(request, responseMessage, conversationHistory);

		return this.callWithFunctionSupport(newRequest);
	}

	abstract protected Req doCreateToolResponseRequest(Req previousRequest, Msg responseMessage,
			List<Msg> conversationHistory);

	abstract protected List<Msg> doGetUserMessages(Req request);

	abstract protected Msg doGetToolResponseMessage(Resp response);

	abstract protected Resp doChatCompletion(Req request);

	abstract protected boolean isToolFunctionCall(Resp response);

}
