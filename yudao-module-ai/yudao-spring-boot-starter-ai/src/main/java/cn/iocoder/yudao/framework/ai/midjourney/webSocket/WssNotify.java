package cn.iocoder.yudao.framework.ai.midjourney.webSocket;

/**
 * 通知信息
 *
 * @author fansili
 * @time 2024/4/29 14:21
 * @since 1.0
 */
public interface WssNotify {

    void notify(int code, String message);
}
