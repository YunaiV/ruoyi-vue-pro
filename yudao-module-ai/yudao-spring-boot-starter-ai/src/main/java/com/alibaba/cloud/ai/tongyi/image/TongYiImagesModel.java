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

package com.alibaba.cloud.ai.tongyi.image;

import com.alibaba.cloud.ai.tongyi.common.exception.TongYiImagesException;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.*;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Base64;
import java.util.stream.Collectors;

import static com.alibaba.cloud.ai.tongyi.metadata.TongYiImagesResponseMetadata.from;

/**
 * TongYiImagesClient is a class that implements the ImageClient interface. It provides a
 * client for calling the TongYi AI image generation API.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public class TongYiImagesModel implements ImageModel {

	private final Logger logger = LoggerFactory.getLogger(TongYiImagesModel.class);

	/**
	 * Gen Images API.
	 */
	private final ImageSynthesis imageSynthesis;

	/**
	 * TongYi Gen images properties.
	 */
	private TongYiImagesOptions defaultOptions;

	/**
	 * Adapt TongYi images api size properties.
	 */
	private final String sizeConnection = "*";

	/**
	 * Get default images options.
	 *
	 * @return Default TongYiImagesOptions.
	 */
	public TongYiImagesOptions getDefaultOptions() {

		return this.defaultOptions;
	}

	/**
	 * TongYiImagesClient constructor.
	 * @param imageSynthesis the image synthesis
	 * {@link ImageSynthesis}
	 */
	public TongYiImagesModel(ImageSynthesis imageSynthesis) {

		this(imageSynthesis, TongYiImagesOptions.
				builder()
				.withModel(ImageSynthesis.Models.WANX_V1)
				.withN(1)
				.build()
		);
	}

	/**
	 * TongYiImagesClient constructor.
	 * @param imageSynthesis {@link ImageSynthesis}
	 * @param imagesOptions {@link TongYiImagesOptions}
	 */
	public TongYiImagesModel(ImageSynthesis imageSynthesis, TongYiImagesOptions imagesOptions) {

		Assert.notNull(imageSynthesis, "ImageSynthesis must not be null");
		Assert.notNull(imagesOptions, "TongYiImagesOptions must not be null");

		this.imageSynthesis = imageSynthesis;
		this.defaultOptions = imagesOptions;
	}

	/**
	 * Call the TongYi images service.
	 * @param imagePrompt the image prompt.
	 * @return the image response.
	 * {@link ImageSynthesis#call(ImageSynthesisParam)}
	 */
	@Override
	public ImageResponse call(ImagePrompt imagePrompt) {

		ImageSynthesisResult result;
		String prompt = imagePrompt.getInstructions().get(0).getText();
		var imgParams = ImageSynthesisParam.builder()
				.prompt("")
				.model(ImageSynthesis.Models.WANX_V1)
				.build();

		if (this.defaultOptions != null) {

			imgParams = merge(this.defaultOptions);
		}

		if (imagePrompt.getOptions() != null) {

			imgParams = merge(toTingYiImageOptions(imagePrompt.getOptions()));
		}
		imgParams.setPrompt(prompt);

		try {
			result = imageSynthesis.call(imgParams);
		}
		catch (NoApiKeyException e) {

			logger.error("TongYi models gen images failed: {}.", e.getMessage());
			throw new TongYiImagesException(e.getMessage());
		}

		return convert(result);
	}

	public ImageSynthesisParam merge(TongYiImagesOptions target) {

		var builder = ImageSynthesisParam.builder();

		builder.model(this.defaultOptions.getModel() != null ? this.defaultOptions.getModel() : target.getModel());
		builder.n(this.defaultOptions.getN() != null ? this.defaultOptions.getN() : target.getN());
		builder.size((this.defaultOptions.getHeight() != null && this.defaultOptions.getWidth() != null)
				? this.defaultOptions.getHeight() + "*" + this.defaultOptions.getWidth()
				: target.getHeight() + "*" + target.getWidth()
		);

		// prompt is marked non-null but is null.
		builder.prompt("");

		return builder.build();
	}

	private ImageResponse convert(ImageSynthesisResult result) {

		return new ImageResponse(
				result.getOutput().getResults().stream()
						.flatMap(value -> value.entrySet().stream())
						.map(entry -> {
							String key = entry.getKey();
							String value = entry.getValue();
							try {
								String base64Image = convertImageToBase64(value);
								Image image = new Image(value, base64Image);
								return new ImageGeneration(image);
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}
						})
						.collect(Collectors.toList()),
				from(result)
		);
	}

	public TongYiImagesOptions toTingYiImageOptions(ImageOptions runtimeImageOptions) {

		var builder = TongYiImagesOptions.builder();

		if (runtimeImageOptions != null) {
			if (runtimeImageOptions.getN() != null) {

				builder.withN(runtimeImageOptions.getN());
			}
			if (runtimeImageOptions.getModel() != null) {

				builder.withModel(runtimeImageOptions.getModel());
			}
			if (runtimeImageOptions.getHeight() != null) {

				builder.withHeight(runtimeImageOptions.getHeight());
			}
			if (runtimeImageOptions.getWidth() != null) {

				builder.withWidth(runtimeImageOptions.getWidth());
			}

			// todo ImagesParams.
		}

		return builder.build();
	}

	/**
	 * Convert image to base64.
	 * @param imageUrl the image url.
	 * @return the base64 image.
	 * @throws Exception the exception.
	 */
	public String convertImageToBase64(String imageUrl) throws Exception {

		var url = new URL(imageUrl);
		var inputStream = url.openStream();
		var outputStream = new ByteArrayOutputStream();
		var buffer = new byte[4096];
		int bytesRead;

		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}

		var imageBytes = outputStream.toByteArray();

		String base64Image = Base64.getEncoder().encodeToString(imageBytes);

		inputStream.close();
		outputStream.close();

		return base64Image;
	}

}
