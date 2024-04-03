package cn.iocoder.yudao.framework.ai.midjourney.wss.user;


public interface FailureCallback {
	void onFailure(int code, String reason);
}
