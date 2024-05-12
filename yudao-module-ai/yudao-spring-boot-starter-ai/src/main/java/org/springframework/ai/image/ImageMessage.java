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

package org.springframework.ai.image;

import java.util.Objects;

public class ImageMessage {

	private String text;

	private Float weight;

	public ImageMessage(String text) {
		this.text = text;
	}

	public ImageMessage(String text, Float weight) {
		this.text = text;
		this.weight = weight;
	}

	public String getText() {
		return text;
	}

	public Float getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return "mageMessage{" + "text='" + text + '\'' + ", weight=" + weight + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ImageMessage that))
			return false;
		return Objects.equals(text, that.text) && Objects.equals(weight, that.weight);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text, weight);
	}

}
