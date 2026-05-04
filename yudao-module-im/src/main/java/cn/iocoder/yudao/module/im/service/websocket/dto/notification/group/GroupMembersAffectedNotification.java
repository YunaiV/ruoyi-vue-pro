package cn.iocoder.yudao.module.im.service.websocket.dto.notification.group;

import lombok.Data;

import java.util.List;

// TODO DONE @AI：本身已是 BaseGroupNotification 的子类，名字不带 Group 前缀是因为是抽象基类、被多种 Group* 通知共用
// TODO @AI：它目前只被 group 使用。别过度设计，名字按照我说的调整下；
/**
 * 涉及成员列表的群事件通知基类：用于 Create / Invite / Kick / AdminAdd / AdminRemove
 */
@Data
public abstract class GroupMembersAffectedNotification extends BaseGroupNotification {

    /**
     * 受影响的成员用户编号列表
     */
    private List<Long> memberUserIds;

}
