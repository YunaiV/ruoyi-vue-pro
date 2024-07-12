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
import org.springframework.ai.image.ImageOptions;

import java.util.Objects;

/**
 * TongYi Image API options.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

public class TongYiImagesOptions implements ImageOptions {

	/**
	 * Specify the model name, currently only wanx-v1 is supported.
	 */
	private String model = ImageSynthesis.Models.WANX_V1;

	/**
	 * Gen images number.
	 */
	private Integer n;

	/**
	 * The width of the generated images.
	 */
	private Integer width = 1024;

	/**
	 * The height of the generated images.
	 */
	private Integer height = 1024;

	@Override
	public Integer getN() {

		return this.n;
	}

	@Override
	public String getModel() {

		return this.model;
	}

	@Override
	public Integer getWidth() {

		return this.width;
	}

	@Override
	public Integer getHeight() {

		return this.height;
	}

	@Override
	public String getResponseFormat() {

		throw new TongYiImagesException("unimplemented!");
	}

	public void setModel(String model) {

		this.model = model;
	}

	public void setN(Integer n) {

		this.n = n;
	}

	public void setWidth(Integer width) {

		this.width = width;
	}

	public void setHeight(Integer height) {

		this.height = height;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {

			return true;
		}
		if (o == null || getClass() != o.getClass()) {

			return false;
		}

		TongYiImagesOptions that = (TongYiImagesOptions) o;

		return Objects.equals(model, that.model)
				&& Objects.equals(n, that.n)
				&& Objects.equals(width, that.width)
				&& Objects.equals(height, that.height);
	}

	@Override
	public int hashCode() {

		return Objects.hash(model, n, width, height);
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder("TongYiImagesOptions{");

		sb.append("model='").append(model).append('\'');
		sb.append(", n=").append(n);
		sb.append(", width=").append(width);
		sb.append(", height=").append(height);
		sb.append('}');

		return sb.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	public final static class Builder {

		private final TongYiImagesOptions options;

		private Builder() {
			this.options = new TongYiImagesOptions();
		}

		public Builder withN(Integer n) {

			options.setN(n);
			return this;
		}

		public Builder withModel(String model) {

			options.setModel(model);
			return this;
		}

		public Builder withWidth(Integer width) {

			options.setWidth(width);
			return this;
		}

		public Builder withHeight(Integer height) {

			options.setHeight(height);
			return this;
		}

		public TongYiImagesOptions build() {

			return options;
		}

	}

}
