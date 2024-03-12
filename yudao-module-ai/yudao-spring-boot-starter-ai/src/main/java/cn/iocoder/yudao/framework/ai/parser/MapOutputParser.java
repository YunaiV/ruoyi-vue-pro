/*
 * Copyright 2023 the original author or authors.
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

package cn.iocoder.yudao.framework.ai.parser;

import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link OutputParser} implementation that uses a pre-configured
 * {@link MappingJackson2MessageConverter} to convert the LLM output into a
 * java.util.Map&lt;String, Object&gt; instance.
 *
 * @author Mark Pollack
 * @author Christian Tzolov
 */
public class MapOutputParser extends AbstractMessageConverterOutputParser<Map<String, Object>> {

	public MapOutputParser() {
		super(new MappingJackson2MessageConverter());
	}

	@Override
	public Map<String, Object> parse(String text) {
		Message<?> message = MessageBuilder.withPayload(text.getBytes(StandardCharsets.UTF_8)).build();
		return (Map) getMessageConverter().fromMessage(message, HashMap.class);
	}

	@Override
	public String getFormat() {
		String raw = """
				Your response should be in JSON format.
				The data structure for the JSON should match this Java class: %s
				Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
				 """;
		return String.format(raw, "java.util.HashMap");
	}

}
