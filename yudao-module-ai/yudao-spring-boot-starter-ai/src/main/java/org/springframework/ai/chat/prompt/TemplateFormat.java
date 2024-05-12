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

package org.springframework.ai.chat.prompt;

public enum TemplateFormat {

	ST("ST");

	private final String value;

	TemplateFormat(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static TemplateFormat fromValue(String value) {
		for (TemplateFormat templateFormat : TemplateFormat.values()) {
			if (templateFormat.getValue().equals(value)) {
				return templateFormat;
			}
		}
		throw new IllegalArgumentException("Invalid TemplateFormat value: " + value);
	}

}
