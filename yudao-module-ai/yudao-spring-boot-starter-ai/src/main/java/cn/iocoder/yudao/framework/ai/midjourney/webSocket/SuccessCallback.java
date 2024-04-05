package cn.iocoder.yudao.framework.ai.midjourney.webSocket;


public interface SuccessCallback {

	void onSuccess(String sessionId, Object sequence, String resumeGatewayUrl);
}
