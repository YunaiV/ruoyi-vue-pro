package cn.iocoder.yudao.module.im.service.message.dto;

import cn.iocoder.yudao.module.im.dal.dataobject.message.ImGroupMessageDO;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.service.websocket.notification.group.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * IM 群聊消息发送 DTO
 *
 * @author 芋道源码
 */
@Data
public class ImGroupMessageSendDTO {

    /**
     * 群编号
     */
    @NotNull(message = "群编号不能为空")
    private Long groupId;
    /**
     * 消息类型
     * <p>
     * 枚举 {@link ImContentTypeEnum}
     */
    @NotNull(message = "消息类型不能为空")
    private Integer type;
    /**
     * 消息内容
     * <p>
     * 支持 String / POJO；非 String 时由 service 序列化为 JSON
     */
    private Object content;
    /**
     * @ 目标用户编号列表
     */
    private List<Long> atUserIds;
    /**
     * 定向接收用户编号列表
     * <p>
     * 为空表示全员可见
     */
    private List<Long> receiverUserIds;
    /**
     * 是否需要回执
     * <p>
     * 缺省视为无需回执（false）
     */
    private Boolean receipt;

    // ========== 群广播事件静态工厂（对应 ImContentTypeEnum 群事件） ==========

    public static ImGroupMessageSendDTO ofGroupCreate(Long groupId, Long operatorUserId, Collection<Long> memberUserIds) {
        GroupCreateNotification notification = new GroupCreateNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setMemberUserIds(new ArrayList<>(memberUserIds));
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_CREATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupInfoUpdate(Long groupId, Long operatorUserId,
                                                          String oldAvatar, String newAvatar,
                                                          Boolean oldJoinApproval, Boolean newJoinApproval) {
        GroupInfoUpdateNotification notification = new GroupInfoUpdateNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setOldAvatar(oldAvatar);
        notification.setNewAvatar(newAvatar);
        notification.setOldJoinApproval(oldJoinApproval);
        notification.setNewJoinApproval(newJoinApproval);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_INFO_UPDATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberQuit(Long groupId, Long operatorUserId) {
        GroupMemberQuitNotification notification = new GroupMemberQuitNotification();
        notification.setOperatorUserId(operatorUserId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MEMBER_QUIT.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupOwnerTransfer(Long groupId, Long operatorUserId, Long newOwnerUserId) {
        GroupOwnerTransferNotification notification = new GroupOwnerTransferNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setNewOwnerUserId(newOwnerUserId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_OWNER_TRANSFER.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberKick(Long groupId, Long operatorUserId, Collection<Long> memberUserIds) {
        GroupMemberKickNotification notification = new GroupMemberKickNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setMemberUserIds(new ArrayList<>(memberUserIds));
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MEMBER_KICK.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberInvite(Long groupId, Long operatorUserId, Collection<Long> memberUserIds) {
        GroupMemberInviteNotification notification = new GroupMemberInviteNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setMemberUserIds(new ArrayList<>(memberUserIds));
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MEMBER_INVITE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberEnter(Long groupId, Long entrantUserId, Integer addSource) {
        GroupMemberEnterNotification notification = new GroupMemberEnterNotification();
        notification.setOperatorUserId(entrantUserId);
        notification.setEntrantUserId(entrantUserId);
        notification.setAddSource(addSource);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MEMBER_ENTER.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupDissolve(Long groupId, Long operatorUserId) {
        GroupDissolveNotification notification = new GroupDissolveNotification();
        notification.setOperatorUserId(operatorUserId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_DISSOLVE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberNicknameUpdate(Long groupId, Long operatorUserId, String displayUserName) {
        GroupMemberNicknameUpdateNotification notification = new GroupMemberNicknameUpdateNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setDisplayUserName(displayUserName);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MEMBER_NICKNAME_UPDATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupAdminAdd(Long groupId, Long operatorUserId, Collection<Long> memberUserIds) {
        GroupAdminAddNotification notification = new GroupAdminAddNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setMemberUserIds(new ArrayList<>(memberUserIds));
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_ADMIN_ADD.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupAdminRemove(Long groupId, Long operatorUserId, Collection<Long> memberUserIds) {
        GroupAdminRemoveNotification notification = new GroupAdminRemoveNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setMemberUserIds(new ArrayList<>(memberUserIds));
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_ADMIN_REMOVE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupNoticeUpdate(Long groupId, Long operatorUserId, String oldNotice, String newNotice) {
        GroupNoticeUpdateNotification notification = new GroupNoticeUpdateNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setOldNotice(oldNotice).setNewNotice(newNotice);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_NOTICE_UPDATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupNameUpdate(Long groupId, Long operatorUserId, String oldName, String newName) {
        GroupNameUpdateNotification notification = new GroupNameUpdateNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setOldName(oldName).setNewName(newName);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_NAME_UPDATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberSettingUpdate(Long groupId, Long operatorUserId, Boolean silent, String groupRemark) {
        GroupMemberSettingUpdateNotification notification = new GroupMemberSettingUpdateNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setSilent(silent).setGroupRemark(groupRemark);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MEMBER_SETTING_UPDATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMessagePin(Long groupId, Long operatorUserId, ImGroupMessageDO message) {
        GroupMessagePinNotification notification = new GroupMessagePinNotification();
        GroupMessagePinNotification.PinnedMessage pinnedMessage = new GroupMessagePinNotification.PinnedMessage()
                .setId(message.getId()).setSenderId(message.getSenderId()).setGroupId(message.getGroupId())
                .setType(message.getType()).setContent(message.getContent()).setSendTime(message.getSendTime())
                .setAtUserIds(message.getAtUserIds()).setReceiverUserIds(message.getReceiverUserIds());
        notification.setOperatorUserId(operatorUserId);
        notification.setMessageId(message.getId()).setMessage(pinnedMessage);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MESSAGE_PIN.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMessageUnpin(Long groupId, Long operatorUserId, Long messageId) {
        GroupMessageUnpinNotification notification = new GroupMessageUnpinNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setMessageId(messageId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MESSAGE_UNPIN.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberMuted(Long groupId, Long operatorUserId,
                                                            Long mutedUserId,
                                                            java.time.LocalDateTime muteEndTime) {
        GroupMemberMutedNotification notification = new GroupMemberMutedNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setMutedUserId(mutedUserId);
        notification.setMuteEndTime(muteEndTime);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MEMBER_MUTED.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberCancelMuted(Long groupId, Long operatorUserId, Long mutedUserId) {
        GroupMemberCancelMutedNotification notification = new GroupMemberCancelMutedNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setMutedUserId(mutedUserId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MEMBER_CANCEL_MUTED.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMuted(Long groupId, Long operatorUserId) {
        GroupMutedNotification notification = new GroupMutedNotification();
        notification.setOperatorUserId(operatorUserId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_MUTED.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupCancelMuted(Long groupId, Long operatorUserId) {
        GroupCancelMutedNotification notification = new GroupCancelMutedNotification();
        notification.setOperatorUserId(operatorUserId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_CANCEL_MUTED.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupBanned(Long groupId, Long operatorUserId, boolean banned) {
        GroupBannedNotification notification = new GroupBannedNotification();
        notification.setOperatorUserId(operatorUserId);
        notification.setBanned(banned);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImContentTypeEnum.GROUP_BANNED.getType()).setContent(notification);
    }

}
