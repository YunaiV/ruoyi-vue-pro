package cn.iocoder.yudao.module.im.service.websocket.dto.message;

import lombok.Data;

/**
 * 文本消息内容
 */
@Data
public class TextMessage {

    /**
     * 文本内容
     */
    private String content;

    /**
     * 引用消息（可选）
     */
    private QuoteMessage quote;

}
