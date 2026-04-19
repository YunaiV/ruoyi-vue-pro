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
     * 私聊场景：好友编号
     */
    private String friendId;
    /**
     * 群聊场景：群编号
     */
    private String groupId;
    /**
     * 消息场景：private / group
     */
    private String messageScene;

}
