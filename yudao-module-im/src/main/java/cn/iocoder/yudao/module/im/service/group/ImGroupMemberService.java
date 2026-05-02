package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberUpdateReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 群成员 Service 接口
 *
 * @author 芋道源码
 */
public interface ImGroupMemberService {

    /**
     * 获得群成员
     *
     * @param id 编号
     * @return 群成员
     */
    ImGroupMemberDO getGroupMember(Long id);

    /**
     * 获得群成员
     *
     * @param groupId 群编号
     * @param userId  用户编号
     * @return 群成员
     */
    ImGroupMemberDO getGroupMember(Long groupId, Long userId);

    /**
     * 批量查询群成员（包含所有状态）
     *
     * @param groupId 群编号
     * @param userIds 用户编号集合
     * @return 群成员列表
     */
    List<ImGroupMemberDO> getGroupMembers(Long groupId, Collection<Long> userIds);

    /**
     * 根据群组 id 查询群成员（包含所有状态）
     *
     * @param groupId 群组id
     * @return 群成员列表
     */
    List<ImGroupMemberDO> getGroupMemberListByGroupId(Long groupId);

    /**
     * 根据群编号查询有效成员列表（仅 ENABLE 状态）
     *
     * @param groupId 群编号
     * @return 有效群成员列表
     */
    List<ImGroupMemberDO> getActiveGroupMemberListByGroupId(Long groupId);

    /**
     * 根据群编号查询有效成员的 userId 列表（仅 ENABLE 状态）
     * <p>
     * 相比 {@link #getActiveGroupMemberListByGroupId(Long)}，只返回 userId，结果体积小，并带有 Redis 缓存。
     * 适用于"群消息推送目标"等只需要 userId 的场景；
     * 需要 joinTime/quitTime/status 等字段做历史消息可见性判断时，仍应使用完整列表方法。
     *
     * @param groupId 群编号
     * @return 有效群成员 userId 列表
     */
    List<Long> getActiveGroupMemberUserIdsByGroupId(Long groupId);

    /**
     * 查询用户所在的所有群的有效成员记录（仅 ENABLE 状态）
     *
     * @param userId 用户编号
     * @return 有效群成员记录列表
     */
    List<ImGroupMemberDO> getActiveGroupMemberListByUserId(Long userId);

    /**
     * 查询用户已退群的群成员记录（DISABLE 状态）
     *
     * @param userId      用户编号，必传
     * @param minQuitTime 最早退群时间（含），可空
     * @return 已退群成员记录列表
     */
    List<ImGroupMemberDO> getQuitGroupMemberListByUserId(Long userId, LocalDateTime minQuitTime);

    /**
     * 添加群成员（入群），角色默认 MEMBER
     *
     * @param groupId 群编号
     * @param userId  用户编号
     * @return 群成员记录
     */
    @SuppressWarnings("UnusedReturnValue")
    ImGroupMemberDO addGroupMember(Long groupId, Long userId);

    /**
     * 添加群成员（入群），并指定角色
     * <p>
     * 重置旧成员行也会强制重置 role，避免离群期间残留管理员身份被复用。
     *
     * @param groupId 群编号
     * @param userId  用户编号
     * @param role    成员角色，见 {@link cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum}
     * @return 群成员记录
     */
    @SuppressWarnings("UnusedReturnValue")
    ImGroupMemberDO addGroupMember(Long groupId, Long userId, Integer role);

    /**
     * 批量添加群成员（入群）
     *
     * @param groupId 群编号
     * @param userIds 用户编号集合
     */
    void addGroupMembers(Long groupId, Collection<Long> userIds);

    /**
     * 校验用户是否为群的有效成员
     *
     * @param groupId 群编号
     * @param userId  用户编号
     * @return 群成员记录
     */
    ImGroupMemberDO validateMemberInGroup(Long groupId, Long userId);

    /**
     * 更新群成员信息（群内昵称、群名备注、免打扰等）
     * <p>
     * 内部会校验用户是否为群的有效成员
     *
     * @param userId      当前登录用户编号
     * @param updateReqVO 更新信息
     */
    void updateGroupMember(Long userId, @Valid ImGroupMemberUpdateReqVO updateReqVO);

    /**
     * 批量更新群成员角色
     *
     * @param groupId 群编号
     * @param userIds 用户编号集合
     * @param role    新角色，见 {@link cn.iocoder.yudao.module.im.enums.group.ImGroupMemberRoleEnum}
     */
    void updateGroupMemberRole(Long groupId, Collection<Long> userIds, Integer role);

    /**
     * 统计群内指定 role 的活跃成员数量
     *
     * @param groupId 群编号
     * @param role    成员角色
     * @return 数量
     */
    Long getGroupMemberCountByRole(Long groupId, Integer role);

    /**
     * 移除指定群成员（设置为 DISABLE 状态）
     * <p>
     * 用于退群、踢出场景
     *
     * @param groupId 群编号
     * @param userId  用户编号
     */
    void removeGroupMember(Long groupId, Long userId);

    /**
     * 批量移除指定群成员（设置为 DISABLE 状态）
     * <p>
     * 用于批量踢出场景
     *
     * @param groupId 群编号
     * @param userIds 用户编号集合
     */
    void removeGroupMembers(Long groupId, Collection<Long> userIds);

    /**
     * 移除群的全部成员（设置为 DISABLE 状态）
     * <p>
     * 用于群解散场景
     *
     * @param groupId 群编号
     */
    void removeGroupMembersByGroupId(Long groupId);

    /**
     * 批量按 group 统计活跃成员数：(group_id → count)
     *
     * @param groupIds 群编号集合
     * @return 群成员数 Map
     */
    Map<Long, Long> getActiveMemberCountMap(Collection<Long> groupIds);

}
