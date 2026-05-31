package cn.iocoder.yudao.module.im.service.websocket.dto.notification.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自由进群事件通知
 * <p>
 * 用户经搜索 / 二维码 / 分享链接自由进群（FREE 模式或审批通过后），全员广播；
 * 进群者前端按 entrantUserId === self 自判，初次拉取 fetchGroupInfo + fetchGroupMembers；其余成员局部插入新成员
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupMemberEnterNotification extends BaseGroupNotification {

    /**
     * 进群者用户编号
     */
    private Long entrantUserId;
    /**
     * 加入来源
     */
    private Integer addSource;

}
