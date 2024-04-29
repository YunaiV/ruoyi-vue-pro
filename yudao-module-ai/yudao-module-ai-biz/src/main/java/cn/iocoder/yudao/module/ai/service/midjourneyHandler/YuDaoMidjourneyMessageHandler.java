package cn.iocoder.yudao.module.ai.service.midjourneyHandler;

import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyMessage;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.MidjourneyMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * yudao message handler
 *
 * @author fansili
 * @time 2024/4/29 14:34
 * @since 1.0
 */
@Component
@Slf4j
public class YuDaoMidjourneyMessageHandler implements MidjourneyMessageHandler {

    @Override
    public void messageHandler(MidjourneyMessage midjourneyMessage) {
        log.info("yudao-midjourney-midjourney-message-handler", midjourneyMessage);
    }
}
