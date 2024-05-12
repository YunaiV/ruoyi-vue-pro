package org.springframework.ai.models.midjourney.webSocket;


public interface FailureCallback {
	void onFailure(int code, String reason);
}
