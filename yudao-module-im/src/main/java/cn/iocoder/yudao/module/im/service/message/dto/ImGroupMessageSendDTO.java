package cn.iocoder.yudao.module.im.service.message.dto;

import cn.iocoder.yudao.module.im.enums.message.ImMessageTypeEnum;
import cn.iocoder.yudao.module.im.service.websocket.dto.notification.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

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
     * 枚举 {@link ImMessageTypeEnum}
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

    // ========== 群广播事件静态工厂（对应 ImMessageTypeEnum 群事件） ==========

    public static ImGroupMessageSendDTO ofGroupCreate(Long groupId, Long operatorUserId, List<Long> memberUserIds) {
        GroupCreateNotification notification = new GroupCreateNotification()
                .setOperatorUserId(operatorUserId).setMemberUserIds(memberUserIds);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_CREATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupInfoUpdate(Long groupId, Long operatorUserId, String oldAvatar, String newAvatar) {
        GroupInfoUpdateNotification notification = new GroupInfoUpdateNotification()
                .setOperatorUserId(operatorUserId).setOldAvatar(oldAvatar).setNewAvatar(newAvatar);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_INFO_UPDATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberQuit(Long groupId, Long operatorUserId) {
        GroupMemberQuitNotification notification = new GroupMemberQuitNotification()
                .setOperatorUserId(operatorUserId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_MEMBER_QUIT.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupOwnerTransfer(Long groupId, Long operatorUserId, Long newOwnerUserId) {
        GroupOwnerTransferNotification notification = new GroupOwnerTransferNotification()
                .setOperatorUserId(operatorUserId).setNewOwnerUserId(newOwnerUserId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_OWNER_TRANSFER.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberKick(Long groupId, Long operatorUserId, List<Long> memberUserIds) {
        GroupMemberKickNotification notification = new GroupMemberKickNotification()
                .setOperatorUserId(operatorUserId).setMemberUserIds(memberUserIds);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_MEMBER_KICK.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberInvite(Long groupId, Long operatorUserId, List<Long> memberUserIds) {
        GroupMemberInviteNotification notification = new GroupMemberInviteNotification()
                .setOperatorUserId(operatorUserId).setMemberUserIds(memberUserIds);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_MEMBER_INVITE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupDissolve(Long groupId, Long operatorUserId) {
        GroupDissolveNotification notification = new GroupDissolveNotification()
                .setOperatorUserId(operatorUserId);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_DISSOLVE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberNicknameUpdate(Long groupId, Long operatorUserId, String displayUserName) {
        GroupMemberNicknameUpdateNotification notification = new GroupMemberNicknameUpdateNotification()
                .setOperatorUserId(operatorUserId).setDisplayUserName(displayUserName);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_MEMBER_NICKNAME_UPDATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupAdminAdd(Long groupId, Long operatorUserId, List<Long> memberUserIds) {
        GroupAdminAddNotification notification = new GroupAdminAddNotification()
                .setOperatorUserId(operatorUserId).setMemberUserIds(memberUserIds);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_ADMIN_ADD.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupAdminRemove(Long groupId, Long operatorUserId, List<Long> memberUserIds) {
        GroupAdminRemoveNotification notification = new GroupAdminRemoveNotification()
                .setOperatorUserId(operatorUserId).setMemberUserIds(memberUserIds);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_ADMIN_REMOVE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupNoticeUpdate(Long groupId, Long operatorUserId, String oldNotice, String newNotice) {
        GroupNoticeUpdateNotification notification = new GroupNoticeUpdateNotification()
                .setOperatorUserId(operatorUserId).setOldNotice(oldNotice).setNewNotice(newNotice);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_NOTICE_UPDATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupNameUpdate(Long groupId, Long operatorUserId, String oldName, String newName) {
        GroupNameUpdateNotification notification = new GroupNameUpdateNotification()
                .setOperatorUserId(operatorUserId).setOldName(oldName).setNewName(newName);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_NAME_UPDATE.getType()).setContent(notification);
    }

    public static ImGroupMessageSendDTO ofGroupMemberSettingUpdate(Long groupId, Long operatorUserId, Boolean muted, String groupRemark) {
        GroupMemberSettingUpdateNotification notification = new GroupMemberSettingUpdateNotification()
                .setOperatorUserId(operatorUserId).setMuted(muted).setGroupRemark(groupRemark);
        return new ImGroupMessageSendDTO().setGroupId(groupId)
                .setType(ImMessageTypeEnum.GROUP_MEMBER_SETTING_UPDATE.getType()).setContent(notification);
    }

}
