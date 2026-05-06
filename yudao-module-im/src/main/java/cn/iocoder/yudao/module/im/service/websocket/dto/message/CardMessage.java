package cn.iocoder.yudao.module.im.service.websocket.dto.message;

import lombok.Data;

/**
 * 名片消息内容
 */
@Data
public class CardMessage {

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 引用消息（可选）
     */
    private QuoteMessage quote;

}
