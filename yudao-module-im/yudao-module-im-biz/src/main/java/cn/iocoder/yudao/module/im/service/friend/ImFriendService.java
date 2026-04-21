package cn.iocoder.yudao.module.im.service.friend;

import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendRespVO;
import cn.iocoder.yudao.module.im.controller.admin.friend.vo.ImFriendUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.friend.ImFriendDO;

import java.util.List;

/**
 * IM 好友关系 Service 接口
 *
 * @author 芋道源码
 */
public interface ImFriendService {

    /**
     * 校验两个用户是否是好友关系（未删除状态）
     *
     * @param userId       用户编号
     * @param friendUserId 好友用户编号
     */
    void validateFriendExists(Long userId, Long friendUserId);

    /**
     * 获得当前用户的好友列表（含昵称/头像聚合）
     * <p>
     * 返回的列表里**包含已删除状态**，前端按 status 区分；
     * 原因是本地已经建立会话的好友被单向删除后，前端仍需要展示头像/昵称。
     *
     * @param userId 用户编号
     * @return 好友列表（含聚合后的用户信息）
     */
    List<ImFriendRespVO> getMyFriendList(Long userId);

    /**
     * 查询一个好友（仅限本人视角的好友关系）
     * <p>
     * 只返回"本人→对方"的关系记录；
     * 对方不是本人好友时抛 {@link cn.iocoder.yudao.module.im.enums.ErrorCodeConstants#FRIEND_NOT_FRIEND}。
     *
     * @param userId       当前登录用户编号
     * @param friendUserId 目标用户编号
     * @return 好友信息（含聚合后的用户信息）
     */
    ImFriendRespVO getFriend(Long userId, Long friendUserId);

    /**
     * 添加好友（双向绑定）
     * <p>
     * 核心流程：
     * 1. 校验：不允许添加自己；对方必须存在
     * 2. 双向插入/更新关系（若已存在则刷新为未删除）
     * 3. 推送"你们已成为好友"系统提示（TODO）
     *
     * @param userId       当前登录用户编号
     * @param friendUserId 好友的用户编号
     */
    void addFriend(Long userId, Long friendUserId);

    /**
     * 删除好友（双向软删除）
     * <p>
     * 两边关系都标记为已删除状态。
     *
     * @param userId       当前登录用户编号
     * @param friendUserId 好友的用户编号
     */
    void deleteFriend(Long userId, Long friendUserId);

    /**
     * 更新好友信息（仅更新本人→对方的单边关系属性）
     * <p>
     * 目前支持：免打扰（muted）；后续可扩展备注等字段。
     *
     * @param userId  当前登录用户编号
     * @param reqVO   更新请求
     */
    void updateFriend(Long userId, ImFriendUpdateReqVO reqVO);

    /**
     * 查询某个用户视角下的好友关系记录（仅单边）
     *
     * @param userId       用户编号
     * @param friendUserId 好友用户编号
     * @return 关系记录，不存在返回 null
     */
    ImFriendDO getFriendDO(Long userId, Long friendUserId);

}
