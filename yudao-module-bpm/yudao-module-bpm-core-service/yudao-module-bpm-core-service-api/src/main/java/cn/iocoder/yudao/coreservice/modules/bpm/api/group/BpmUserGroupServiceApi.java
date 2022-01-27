package cn.iocoder.yudao.coreservice.modules.bpm.api.group;

import cn.iocoder.yudao.coreservice.modules.bpm.api.group.dto.BpmUserGroupDTO;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Bpm 用户组 API 接口
 *
 * @author jason
 */
public interface BpmUserGroupServiceApi {

    /**
     * 获得用户组列表
     *
     * @param ids 编号
     * @return 用户组列表
     */
    List<BpmUserGroupDTO> getUserGroupList(Collection<Long> ids);

    /**
     * 校验用户组们是否有效。如下情况，视为无效：
     * 1. 用户组编号不存在
     * 2. 用户组被禁用
     *
     * @param ids 用户组编号数组
     */
    void validUserGroups(Set<Long> ids);
}
