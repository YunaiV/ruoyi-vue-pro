package cn.iocoder.yudao.module.im.service.websocket.dto.notification;

import lombok.Data;

import java.util.List;

/**
 * 涉及成员列表的群事件通知基类：用于 Create / Invite / Kick / AdminAdd / AdminRemove
 */
@Data
public abstract class MembersAffectedNotification extends BaseGroupNotification {

    /**
     * 受影响的成员用户编号列表
     */
    private List<Long> memberUserIds;

}
