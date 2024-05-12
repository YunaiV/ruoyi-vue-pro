package org.springframework.ai.models.yiyan.exception;

/**
 * 一言 api 调用异常
 */
public class YiYanApiException extends RuntimeException {

	public YiYanApiException(String message) {
		super(message);
	}

	public YiYanApiException(String message, Throwable cause) {
		super(message, cause);
	}

}