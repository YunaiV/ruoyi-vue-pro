package cn.iocoder.yudao.framework.ai.midjourney.webSocket;


public interface FailureCallback {
	void onFailure(int code, String reason);
}
