package cn.iocoder.yudao.module.im.websocket;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImPrivateMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * IM 私聊消息 WebSocket 统一推送 DTO
 * <p>
 * 发送、已读、回执、撤回都通过 {@link #type} 字段区分，
 * 并统一复用 {@link #id} 字段表达目标消息或已读位置。
 *
 * @author 芋道源码
 */
@Data
@Accessors(chain = true)
public class ImPrivateMessageDTO {

    public static final String TYPE = "im-private-message";

    /**
     * 消息编号
     * <p>
     * 普通消息：当前消息 id
     * READ：已读位置（我已读到这条消息）
     * RECEIPT：已读位置（对方已读到这条消息）
     * RECALL：被撤回的原消息 id
     */
    private Long id;
    /**
     * 客户端消息编号
     */
    private String clientMessageId;
    /**
     * 发送人编号
     */
    private Long senderId;
    /**
     * 接收人编号
     */
    private Long receiverId;
    /**
     * 消息类型
     * <p>
     * TEXT / IMAGE / FILE / VOICE / VIDEO / READ / RECEIPT / RECALL / TIP_TEXT
     */
    private Integer type;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 消息状态
     */
    private Integer status;
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    // ========== 静态工厂方法 ==========

    /**
     * 构建发送消息 DTO
     *
     * @param message 私聊消息 DO
     * @return 私聊 DTO
     */
    public static ImPrivateMessageDTO ofSend(ImPrivateMessageDO message) {
        return BeanUtils.toBean(message, ImPrivateMessageDTO.class);
    }

    /**
     * 构建已读同步 DTO（多端同步：通知自己的其他终端"我已经读了某个会话"）
     *
     * @param senderId   当前用户编号
     * @param receiverId 对方用户编号
     * @param readId     已读位置（最大已读消息编号）
     * @return 私聊 DTO
     */
    public static ImPrivateMessageDTO ofRead(Long senderId, Long receiverId, Long readId) {
        return new ImPrivateMessageDTO()
                .setId(readId).setType(ImMessageTypeEnum.READ.getType())
                .setSenderId(senderId).setReceiverId(receiverId);
    }

    /**
     * 构建已读回执 DTO（通知对方"我已读了你的消息"）
     *
     * @param senderId   已读方的用户编号
     * @param receiverId 对方用户编号
     * @param readId     已读位置（最大已读消息编号）
     * @return 私聊 DTO
     */
    public static ImPrivateMessageDTO ofReceipt(Long senderId, Long receiverId, Long readId) {
        return new ImPrivateMessageDTO()
                .setId(readId).setType(ImMessageTypeEnum.RECEIPT.getType())
                .setSenderId(senderId).setReceiverId(receiverId);
    }

}
