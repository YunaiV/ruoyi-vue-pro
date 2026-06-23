package cn.iocoder.yudao.module.im.service.websocket.notification.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收到新的入群申请通知
 * <p>
 * 定向推送给群主 + 全部管理员（多端同步）；payload 已携带申请方昵称 / 头像，前端按 requestId 直接 push 进列表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GroupRequestReceivedNotification extends BaseGroupNotification {

    /**
     * 申请记录编号
     */
    private Long requestId;
    /**
     * 群编号
     */
    private Long groupId;
    /**
     * 申请人 / 被邀请人用户编号
     */
    private Long userId;
    /**
     * 邀请人用户编号；NULL 表示用户主动申请
     */
    private Long inviterUserId;
    /**
     * 申请理由
     */
    private String applyContent;
    /**
     * 加入来源
     */
    private Integer addSource;

    // ========== 聚合自 AdminUser，避免前端再调 system 接口 ==========

    /**
     * 申请方 / 被邀请人昵称
     */
    private String userNickname;
    /**
     * 申请方 / 被邀请人头像
     */
    private String userAvatar;

}
