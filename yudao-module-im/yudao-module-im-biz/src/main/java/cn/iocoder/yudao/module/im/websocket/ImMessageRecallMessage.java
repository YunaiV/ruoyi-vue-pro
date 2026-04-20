package cn.iocoder.yudao.module.im.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IM 消息撤回 WebSocket 推送消息
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImMessageRecallMessage {

    public static final String TYPE = "im-message-recall";

    /**
     * 消息编号
     */
    private Long messageId;
    /**
     * 发送人编号
     */
    private Long senderId;
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
