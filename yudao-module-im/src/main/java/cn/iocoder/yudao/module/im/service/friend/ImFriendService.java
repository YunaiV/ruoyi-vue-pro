package cn.iocoder.yudao.module.im.service.friend;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo.ImFriendManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendRequestDO;
import cn.iocoder.yudao.module.im.enums.friend.ImFriendStateEnum;

import java.util.Collection;
import java.util.List;

/**
 * IM 好友关系 Service 接口
 * <p>
 * 注意：用户端「加好友」走 {@link ImFriendRequestService#applyFriend} 申请-审批流程，
 * 不再开放直接 add 接口；只有 {@link #becomeFriends} 是内部入口（被 agree 同意 / 管理员 import 触发）。
 *
 * @author 芋道源码
 */
public interface ImFriendService {

    /**
     * 获取 userId 视角下与 friendUserId 的好友关系状态（私聊发送热点路径）
     * <p>
     * 合并「是否好友」+「是否被自己拉黑」两态，供调用方一次缓存查询完成判定，详见 {@link ImFriendStateEnum}
     */
    ImFriendStateEnum getFriendState(Long userId, Long friendUserId);

    /**
     * 获得当前用户的好友列表（含已删除状态）
     */
    List<ImFriendDO> getFriendList(Long userId);

    /**
     * 获得当前用户与指定用户之间的有效好友列表（仅 ENABLE 状态）
     */
    List<ImFriendDO> getActiveFriendList(Long userId, Collection<Long> friendUserIds);

    /**
     * 查询一个好友关系记录
     */
    ImFriendDO getFriend(Long userId, Long friendUserId);

    // ==================== 内部入口 ====================

    /**
     * 双向建立好友关系（内部入口）
     * <p>
     * 由 {@link ImFriendRequestService#agreeFriendRequest} 同意申请 / 管理后台导入触发；
     * A 侧 displayName / addSource 取自申请记录；B 侧 displayName 为空、addSource 同来源。
     * 写库后推送 FRIEND_ADD 通知给 A、B 双方多端，并下发 TIP 系统消息。
     *
     * @param request 已同意的申请记录（决定 fromUserId / toUserId / addSource / displayName）
     */
    void becomeFriends(ImFriendRequestDO request);

    // ==================== 用户端 ====================

    /**
     * 删除好友（单向软删除）
     * <p>
     * 仅删除 userId 视角下的好友关系；对端 friendUserId 的视角不受影响（与 OpenIM 单边删除语义对齐）
     */
    void deleteFriend(Long userId, Long friendUserId);

    /**
     * 更新好友单边属性（备注 / 免打扰 / 联系人置顶）
     */
    void updateFriend(Long userId, ImFriendUpdateReqVO reqVO);

    /**
     * 拉黑好友（必须先是好友）
     */
    void blockFriend(Long userId, Long friendUserId);

    /**
     * 移出黑名单
     */
    void unblockFriend(Long userId, Long friendUserId);

    // ==================== 管理后台 ====================

    /**
     * 【管理后台】分页查询好友关系
     */
    PageResult<ImFriendDO> getFriendPage(ImFriendManagerPageReqVO reqVO);

}
