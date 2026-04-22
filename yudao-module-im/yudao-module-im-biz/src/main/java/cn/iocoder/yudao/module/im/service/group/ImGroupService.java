package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupCreateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.ImGroupUpdateReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberInviteReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 群 Service 接口
 *
 * @author 芋道源码
 */
public interface ImGroupService {

    // ==================== 群的写操作 ====================

    /**
     * 创建群
     * <p>
     * 同时将当前登录用户设置为群主，并插入群主的群成员记录
     *
     * @param createReqVO 创建信息
     * @param userId      当前登录用户编号（群主）
     * @return 创建后的群信息
     */
    ImGroupDO createGroup(@Valid ImGroupCreateReqVO createReqVO, Long userId);

    /**
     * 更新群信息
     *
     * @param updateReqVO 更新信息
     * @param userId      当前登录用户编号
     * @return 更新后的群信息
     */
    ImGroupDO updateGroup(@Valid ImGroupUpdateReqVO updateReqVO, Long userId);

    /**
     * 解散群
     * <p>
     * 仅群主可执行
     *
     * @param id     群编号
     * @param userId 当前登录用户编号
     */
    void dissolveGroup(Long id, Long userId);

    // ==================== 群的读操作 ====================

    /**
     * 获得群
     *
     * @param id 编号
     * @return 群
     */
    ImGroupDO getGroup(Long id);

    /**
     * 校验群存在且未封禁、未解散
     *
     * @param groupId 群编号
     * @return 群信息
     */
    ImGroupDO validateGroupExists(Long groupId);

    /**
     * 校验当前用户是否为群主
     *
     * @param groupId 群编号
     * @param userId  用户编号
     * @return 群信息
     */
    @SuppressWarnings("UnusedReturnValue")
    ImGroupDO validateGroupOwner(Long groupId, Long userId);

    /**
     * 获取指定用户加入的所有"有效"群（未退群且未封禁/解散）
     * <p>
     * 返回当前登录用户在群内身份仍有效的所有群。
     *
     * @param userId 用户编号
     * @return 群列表
     */
    List<ImGroupDO> getActiveGroupList(Long userId);

    // ==================== 群成员的写操作 ====================
    // 说明：群成员的写操作统一放在 ImGroupService，而非 ImGroupMemberService，
    //       保持 ImGroupMemberService 无 WebSocket 推送等外部依赖，职责更单一。

    /**
     * 邀请用户加入群
     * <p>
     * 仅群主可执行
     *
     * @param userId      当前登录用户编号（群主）
     * @param inviteReqVO 邀请信息
     * @return 群成员编号
     */
    Long inviteGroupMember(Long userId, @Valid ImGroupMemberInviteReqVO inviteReqVO);

    /**
     * 退群
     * <p>
     * 群主不可退群（只能解散）
     *
     * @param groupId 群编号
     * @param userId  当前登录用户编号
     */
    void quitGroup(Long groupId, Long userId);

    /**
     * 移除群成员（踢人）
     * <p>
     * 仅群主可执行，且不能移除自己
     *
     * @param groupId      群编号
     * @param memberUserId 被移除的用户编号
     * @param userId       当前登录用户编号（群主）
     */
    void removeGroupMember(Long groupId, Long memberUserId, Long userId);

}
