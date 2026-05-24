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
     * 参见 {@link ImFriendStateEnum} 枚举类
     */
    Integer getFriendState(Long userId, Long friendUserId);

    /**
     * 校验「能否对 peerUserId 发起私聊语义动作」（消息发送 ／ RTC 邀请）
     * <p>
     * 好友 ／ 黑名单校验：和私聊消息发送同一套语义；NONE 已删 ／ 未加，BLOCKED 被对方拉黑
     *
     * @param userId     当前用户编号
     * @param peerUserId 对方用户编号
     */
    void validateFriend(Long userId, Long peerUserId);

    /**
     * 获得当前用户的好友列表（含已删除状态）
     */
    List<ImFriendDO> getFriendList(Long userId);

    /**
     * 获得当前用户的有效好友列表（仅 ENABLE 状态）
     */
    List<ImFriendDO> getEnableFriendList(Long userId);

    /**
     * 获得当前用户的双向有效好友列表（双方均 ENABLE 状态）
     */
    List<ImFriendDO> getMutualEnableFriendList(Long userId);

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

    /**
     * 单向静默重新建立好友关系
     * <p>
     * 仅用于 {@link ImFriendRequestService#applyFriend} 在「我已删除 + 对方仍把我当好友」场景：
     * 直接恢复 userId 这边的 friend 记录，不走申请审批；不下发 TIP / 不通知对方，仅 FRIEND_ADD 给 userId 多端，避免对方感知我曾删除。
     *
     * @param userId       当前用户编号
     * @param friendUserId 对方用户编号
     * @param displayName  备注（取自申请 VO）
     * @param addSource    添加来源（取自申请 VO）
     */
    void silentReAddFriend(Long userId, Long friendUserId, String displayName, Integer addSource);

    // ==================== 用户端 ====================

    /**
     * 删除好友（单向软删除）
     * <p>
     * 仅删除 userId 视角下的好友关系；对端 friendUserId 的视角不受影响（单边删除语义）
     *
     * @param clear 是否级联清理本端相关数据（当前包含私聊会话；通过 FRIEND_DELETE 通知透传给多端）
     */
    void deleteFriend(Long userId, Long friendUserId, Boolean clear);

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
