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


import org.springframework.ai.model.ModelResult;

public class ImageGeneration implements ModelResult<Image> {

	// metadata 信息为空现在
	private ImageGenerationMetadata imageGenerationMetadata;

	private Image image;

	public ImageGeneration(Image image) {
		this.image = image;
	}

	public ImageGeneration(Image image, ImageGenerationMetadata imageGenerationMetadata) {
		this.image = image;
		this.imageGenerationMetadata = imageGenerationMetadata;
	}

	@Override
	public Image getOutput() {
		return image;
	}

	@Override
	public ImageGenerationMetadata getMetadata() {
		return imageGenerationMetadata;
	}

	@Override
	public String toString() {
		return "ImageGeneration{" + "imageGenerationMetadata=" + imageGenerationMetadata + ", image=" + image + '}';
	}

}
