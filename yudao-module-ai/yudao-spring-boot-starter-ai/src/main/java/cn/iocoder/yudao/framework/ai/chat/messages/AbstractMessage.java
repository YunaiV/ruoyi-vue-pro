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

package cn.iocoder.yudao.framework.ai.chat.messages;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractMessage implements Message {

	protected final MessageType messageType;

	protected final String textContent;

	protected final List<MediaData> mediaData;

	/**
	 * Additional options for the message to influence the response, not a generative map.
	 */
	protected final Map<String, Object> properties;

	protected AbstractMessage(MessageType messageType, String content) {
		this(messageType, content, Map.of());
	}

	protected AbstractMessage(MessageType messageType, String content, Map<String, Object> messageProperties) {
		Assert.notNull(messageType, "Message type must not be null");
		// Assert.notNull(content, "Content must not be null");
		this.messageType = messageType;
		this.textContent = content;
		this.mediaData = new ArrayList<>();
		this.properties = messageProperties;
	}

	protected AbstractMessage(MessageType messageType, String textContent, List<MediaData> mediaData) {
		this(messageType, textContent, mediaData, Map.of());
	}

	protected AbstractMessage(MessageType messageType, String textContent, List<MediaData> mediaData,
							  Map<String, Object> messageProperties) {

		Assert.notNull(messageType, "Message type must not be null");
		Assert.notNull(textContent, "Content must not be null");
		Assert.notNull(mediaData, "media data must not be null");

		this.messageType = messageType;
		this.textContent = textContent;
		this.mediaData = new ArrayList<>(mediaData);
		this.properties = messageProperties;
	}

	protected AbstractMessage(MessageType messageType, Resource resource) {
		this(messageType, resource, Collections.emptyMap());
	}

	@SuppressWarnings("null")
	protected AbstractMessage(MessageType messageType, Resource resource, Map<String, Object> messageProperties) {
		Assert.notNull(messageType, "Message type must not be null");
		Assert.notNull(resource, "Resource must not be null");

		this.messageType = messageType;
		this.properties = messageProperties;
		this.mediaData = new ArrayList<>();

		try (InputStream inputStream = resource.getInputStream()) {
			this.textContent = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
		}
		catch (IOException ex) {
			throw new RuntimeException("Failed to read resource", ex);
		}
	}

	@Override
	public String getContent() {
		return this.textContent;
	}

	@Override
	public List<MediaData> getMediaData() {
		return this.mediaData;
	}

	@Override
	public Map<String, Object> getProperties() {
		return this.properties;
	}

	@Override
	public MessageType getMessageType() {
		return this.messageType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mediaData == null) ? 0 : mediaData.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((messageType == null) ? 0 : messageType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractMessage other = (AbstractMessage) obj;
		if (mediaData == null) {
			if (other.mediaData != null)
				return false;
		}
		else if (!mediaData.equals(other.mediaData))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		}
		else if (!properties.equals(other.properties))
			return false;
		if (messageType != other.messageType)
			return false;
		return true;
	}

}
