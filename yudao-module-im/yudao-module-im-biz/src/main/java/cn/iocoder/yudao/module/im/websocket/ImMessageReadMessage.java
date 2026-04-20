package cn.iocoder.yudao.module.im.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IM 消息已读 WebSocket 推送消息
 * <p>
 * 用于多端同步：通知自己的其他终端"我已经读了某个会话"
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImMessageReadMessage {

    public static final String TYPE = "im-message-read";

    /**
     * 私聊场景：接收方编号
     */
    private Long receiverId;
    /**
     * 群聊场景：群编号
     */
    private Long groupId;
    /**
     * 消息场景
     *
     * 关联 {@link cn.iocoder.yudao.module.im.enums.message.ImMessageSceneEnum}
     */
    private Integer scene;

}
