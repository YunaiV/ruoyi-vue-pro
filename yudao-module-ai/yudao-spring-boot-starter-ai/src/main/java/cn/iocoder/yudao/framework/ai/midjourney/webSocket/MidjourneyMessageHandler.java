package cn.iocoder.yudao.framework.ai.midjourney.webSocket;

import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyMessage;

/**
 * message handler
 *
 * @author fansili
 * @time 2024/4/29 14:29
 * @since 1.0
 */
public interface MidjourneyMessageHandler {

    void messageHandler(MidjourneyMessage midjourneyMessage);
}
