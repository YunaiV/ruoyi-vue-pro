package cn.iocoder.yudao.module.im.service.websocket.notification.friend;

import lombok.Data;

/**
 * 好友信息批量更新通知（备注 / 免打扰 / 联系人置顶等单边属性）
 * <p>
 * A 改了对 B 的备注 / 免打扰 / 置顶等单边属性后仅推 A 多端做同步；
 * 一次 update 涉及多个字段时合并为单条通知，避免多通知顺序竞争。
 */
@Data
public class FriendUpdateNotification extends BaseFriendNotification {

    /**
     * 备注；不为空则更新（空串表示清空）
     */
    private String displayName;
    /**
     * 免打扰；不为空则更新
     */
    private Boolean silent;
    /**
     * 联系人置顶；不为空则更新
     */
    private Boolean pinned;

}
