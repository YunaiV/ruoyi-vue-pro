package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 群成员 Service 接口
 *
 * @author 芋道源码
 */
public interface ImGroupMemberService {

    /**
     * 创建群成员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createGroupMember(@Valid ImGroupMemberSaveReqVO createReqVO);

    /**
     * 更新群成员
     *
     * @param updateReqVO 更新信息
     */
    void updateGroupMember(@Valid ImGroupMemberSaveReqVO updateReqVO);

    /**
     * 删除群成员
     *
     * @param id 编号
     */
    void deleteGroupMember(Long id);

    /**
     * 获得群成员
     *
     * @param id 编号
     * @return 群成员
     */
    ImGroupMemberDO getGroupMember(Long id);

    /**
     * 获得群成员分页
     *
     * @param pageReqVO 分页查询
     * @return 群成员分页
     */
    PageResult<ImGroupMemberDO> getGroupMemberPage(ImGroupMemberPageReqVO pageReqVO);

    /**
     * 根据群组 id 查询群成员（包含所有状态）
     *
     * @param groupId 群组id
     * @return 群成员列表
     */
    List<ImGroupMemberDO> getGroupMemberListByGroupId(Long groupId);

    /**
     * 根据群编号和用户编号查询群成员
     *
     * @param groupId 群编号
     * @param userId  用户编号
     * @return 群成员
     */
    ImGroupMemberDO getGroupMember(Long groupId, Long userId);

    /**
     * 查询用户所在的所有群的成员记录
     *
     * @param userId 用户编号
     * @return 群成员记录列表
     */
    List<ImGroupMemberDO> getGroupMemberListByUserId(Long userId);

    /**
     * 根据群编号查询有效成员列表（仅 ENABLE 状态）
     *
     * @param groupId 群编号
     * @return 有效群成员列表
     */
    List<ImGroupMemberDO> getActiveGroupMemberListByGroupId(Long groupId);

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
     * 校验用户是否为群的有效成员
     *
     * @param groupId 群编号
     * @param userId  用户编号
     * @return 群成员记录
     */
    ImGroupMemberDO validateMemberInGroup(Long groupId, Long userId);
}
