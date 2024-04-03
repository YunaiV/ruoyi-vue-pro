package cn.iocoder.yudao.framework.ai.midjourney.wss.user;


public interface SuccessCallback {

	void onSuccess(String sessionId, Object sequence, String resumeGatewayUrl);
}
