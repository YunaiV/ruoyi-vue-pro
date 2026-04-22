package cn.iocoder.yudao.module.im.service.websocket.dto;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * IM 群聊消息 WebSocket 统一推送 DTO
 * <p>
 * 发送、已读、回执都通过 {@link #type} 字段区分，
 * 并统一复用 {@link #id} 字段表达目标消息或已读位置。
 *
 * @author 芋道源码
 */
@Data
public class ImGroupMessageDTO {

    public static final String TYPE = "im-group-message";

    /**
     * 消息编号
     * <p>
     * 普通消息：当前消息 id
     * READ：无（群已读不需要）
     * RECEIPT：需要回执的目标消息 id
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
     * 群编号
     */
    private Long groupId;
    /**
     * 消息类型
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
    /**
     * @ 目标用户编号列表
     */
    private List<Long> atUserIds;
    /**
     * 定向接收用户编号列表
     */
    private List<Long> receiverUserIds;
    /**
     * 群回执状态
     */
    private Integer receiptStatus;
    /**
     * 群回执已读人数
     */
    private Integer readCount;

    // ========== 静态工厂方法 ==========

    /**
     * 构建发送消息 DTO
     *
     * @param message 群聊消息 DO
     * @return 群聊 DTO
     */
    public static ImGroupMessageDTO ofSend(ImGroupMessageDO message) {
        return BeanUtils.toBean(message, ImGroupMessageDTO.class);
    }

    /**
     * 构建已读同步 DTO（多端同步：通知自己的其他终端"我已经读了某个群"）
     *
     * @param senderId 当前用户编号
     * @param groupId  群编号
     * @param readId   已读位置（最大已读消息编号）
     * @return 群聊 DTO
     */
    public static ImGroupMessageDTO ofRead(Long senderId, Long groupId, Long readId) {
        return new ImGroupMessageDTO()
                .setId(readId).setType(ImMessageTypeEnum.READ.getType())
                .setSenderId(senderId).setGroupId(groupId);
    }

    /**
     * 构建群回执 DTO（广播回执状态和已读人数）
     *
     * @param messageId     消息编号
     * @param groupId       群编号
     * @param readCount     已读人数
     * @param receiptStatus 回执状态
     * @return 群聊 DTO
     */
    public static ImGroupMessageDTO ofReceipt(Long messageId, Long groupId,
                                              Integer readCount, Integer receiptStatus) {
        return new ImGroupMessageDTO()
                .setId(messageId).setType(ImMessageTypeEnum.RECEIPT.getType())
                .setGroupId(groupId).setReadCount(readCount).setReceiptStatus(receiptStatus);
    }

    // ==================== 群变更相关 ====================

    /**
     * 构建群创建推送 DTO（通知群成员"有一个新群"）
     *
     * @param senderId 创建者（群主）用户编号
     * @param groupId  群编号
     * @return 群聊 DTO
     */
    public static ImGroupMessageDTO ofGroupCreate(Long senderId, Long groupId) {
        return new ImGroupMessageDTO()
                .setType(ImMessageTypeEnum.GROUP_CREATE.getType())
                .setSenderId(senderId).setGroupId(groupId)
                .setSendTime(LocalDateTime.now());
    }

    /**
     * 构建群信息变更推送 DTO（通知所有群成员"群信息变了"）
     *
     * @param senderId 操作者用户编号
     * @param groupId  群编号
     * @return 群聊 DTO
     */
    public static ImGroupMessageDTO ofGroupUpdate(Long senderId, Long groupId) {
        return new ImGroupMessageDTO()
                .setType(ImMessageTypeEnum.GROUP_UPDATE.getType())
                .setSenderId(senderId).setGroupId(groupId)
                .setSendTime(LocalDateTime.now());
    }

    /**
     * 构建群删除推送 DTO（通知群成员"群已移除"）
     * <p>
     * 适用于群解散、退群、踢出等场景
     *
     * @param senderId 操作者用户编号
     * @param groupId  群编号
     * @return 群聊 DTO
     */
    public static ImGroupMessageDTO ofGroupDelete(Long senderId, Long groupId) {
        return new ImGroupMessageDTO()
                .setType(ImMessageTypeEnum.GROUP_DELETE.getType())
                .setSenderId(senderId).setGroupId(groupId)
                .setSendTime(LocalDateTime.now());
    }

    /**
     * 构建群成员信息变更推送 DTO（多端同步：通知自己"在某群的个人设置变了"）
     *
     * @param senderId 当前用户编号
     * @param groupId  群编号
     * @return 群聊 DTO
     */
    public static ImGroupMessageDTO ofGroupMemberUpdate(Long senderId, Long groupId) {
        return new ImGroupMessageDTO()
                .setType(ImMessageTypeEnum.GROUP_MEMBER_UPDATE.getType())
                .setSenderId(senderId).setGroupId(groupId)
                .setSendTime(LocalDateTime.now());
    }

}
