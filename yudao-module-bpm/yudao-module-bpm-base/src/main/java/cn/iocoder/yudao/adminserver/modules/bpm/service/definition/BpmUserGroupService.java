package cn.iocoder.yudao.adminserver.modules.bpm.service.definition;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

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
    Long createUserGroup(@Valid BpmUserGroupCreateReqVO createReqVO);

    /**
     * 更新用户组
     *
     * @param updateReqVO 更新信息
     */
    void updateUserGroup(@Valid BpmUserGroupUpdateReqVO updateReqVO);

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
}
