package cn.iocoder.yudao.framework.ai.midjourney.demo.wss.user;


public interface SuccessCallback {

	void onSuccess(String sessionId, Object sequence, String resumeGatewayUrl);
}
