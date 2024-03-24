package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group.BpmUserGroupPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.group.BpmUserGroupSaveReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmUserGroupDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 用户组 Service 接口
 *
 * @author 芋道源码
 */
public interface BpmUserGroupService {

    /**
     * 创建用户组
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createUserGroup(@Valid BpmUserGroupSaveReqVO createReqVO);

    /**
     * 更新用户组
     *
     * @param updateReqVO 更新信息
     */
    void updateUserGroup(@Valid BpmUserGroupSaveReqVO updateReqVO);

    /**
     * 删除用户组
     *
     * @param id 编号
     */
    void deleteUserGroup(Long id);

    /**
     * 获得用户组
     *
     * @param id 编号
     * @return 用户组
     */
    BpmUserGroupDO getUserGroup(Long id);

    /**
     * 获得用户组列表
     *
     * @param ids 编号
     * @return 用户组列表
     */
    List<BpmUserGroupDO> getUserGroupList(Collection<Long> ids);

    /**
     * 获得指定状态的用户组列表
     *
     * @param status 状态
     * @return 用户组列表
     */
    List<BpmUserGroupDO> getUserGroupListByStatus(Integer status);

    /**
     * 获得用户组分页
     *
     * @param pageReqVO 分页查询
     * @return 用户组分页
     */
    PageResult<BpmUserGroupDO> getUserGroupPage(BpmUserGroupPageReqVO pageReqVO);

    /**
     * 校验用户组们是否有效。如下情况，视为无效：
     * 1. 用户组编号不存在
     * 2. 用户组被禁用
     *
     * @param ids 用户组编号数组
     */
    void validUserGroups(Collection<Long> ids);

}
