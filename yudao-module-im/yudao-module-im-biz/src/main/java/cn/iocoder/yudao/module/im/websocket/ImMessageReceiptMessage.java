package cn.iocoder.yudao.module.im.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IM 已读回执 WebSocket 推送消息
 * <p>
 * 私聊：通知对方"我已读了你的消息"
 * 群聊：广播回执状态和已读人数
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImMessageReceiptMessage {

    public static final String TYPE = "im-message-receipt";

    /**
     * 消息场景
     *
     * 关联 {@link cn.iocoder.yudao.module.im.enums.message.ImMessageSceneEnum}
     */
    private Integer scene;

    // ========== 私聊场景 ==========

    /**
     * 私聊场景：已读方的用户编号
     */
    private Long receiverId;

    // ========== 群聊场景 ==========

    /**
     * 群聊场景：消息编号
     */
    private Long messageId;
    /**
     * 群聊场景：群编号
     */
    private Long groupId;
    /**
     * 群聊场景：已读人数
     */
    private Integer readCount;
    /**
     * 群聊场景：回执状态
     */
    private Integer receiptStatus;

}
