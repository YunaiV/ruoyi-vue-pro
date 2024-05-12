package org.springframework.ai.models.midjourney.webSocket;

import org.springframework.ai.models.midjourney.MidjourneyMessage;

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
