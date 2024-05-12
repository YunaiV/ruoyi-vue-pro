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

import org.springframework.ai.model.ModelResponse;

import java.util.List;
import java.util.Objects;

public class ImageResponse implements ModelResponse<ImageGeneration> {

	private final ImageResponseMetadata imageResponseMetadata;

	private final List<ImageGeneration> imageGenerations;

	public ImageResponse(List<ImageGeneration> generations) {
		this(generations, ImageResponseMetadata.NULL);
	}

	public ImageResponse(List<ImageGeneration> generations, ImageResponseMetadata imageResponseMetadata) {
		this.imageResponseMetadata = imageResponseMetadata;
		this.imageGenerations = List.copyOf(generations);
	}

	@Override
	public ImageGeneration getResult() {
		return imageGenerations.get(0);
	}

	@Override
	public List<ImageGeneration> getResults() {
		return imageGenerations;
	}

	@Override
	public ImageResponseMetadata getMetadata() {
		return imageResponseMetadata;
	}

	@Override
	public String toString() {
		return "ImageResponse{" + "imageResponseMetadata=" + imageResponseMetadata + ", imageGenerations="
				+ imageGenerations + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ImageResponse that))
			return false;
		return Objects.equals(imageResponseMetadata, that.imageResponseMetadata)
				&& Objects.equals(imageGenerations, that.imageGenerations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(imageResponseMetadata, imageGenerations);
	}

}
