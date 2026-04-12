package cn.iocoder.yudao.module.im.service.group;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.member.ImGroupMemberSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupMemberDO;
import jakarta.validation.Valid;

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
     * 根据群组id查询群成员
     *
     * @param groupId 群组id
     * @return 群成员列表
     */
    List<ImGroupMemberDO> selectByGroupId(Long groupId);

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
    List<ImGroupMemberDO> getGroupMembersByUserId(Long userId);
}
