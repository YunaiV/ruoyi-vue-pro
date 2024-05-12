package org.springframework.ai.models.midjourney.webSocket;


public interface SuccessCallback {

	void onSuccess(String sessionId, Object sequence, String resumeGatewayUrl);
}
