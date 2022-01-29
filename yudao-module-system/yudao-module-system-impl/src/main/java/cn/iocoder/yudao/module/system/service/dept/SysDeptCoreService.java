package cn.iocoder.yudao.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface SysDeptCoreService {
    /**
     * 获得部门信息数组
     *
     * @param ids 部门编号数组
     * @return 部门信息数组
     */
    List<SysDeptDO> getDepts(Collection<Long> ids);

    /**
     * 获得部门信息
     *
     * @param id 部门编号
     * @return 部门信息
     */
    SysDeptDO getDept(Long id);

    /**
     * 校验部门们是否有效。如下情况，视为无效：
     * 1. 部门编号不存在
     * 2. 部门被禁用
     *
     * @param ids 角色编号数组
     */
    void validDepts(Collection<Long> ids);

    /**
     * 获得指定编号的部门列表
     *
     * @param ids 部门编号数组
     * @return 部门列表
     */
    List<SysDeptDO> getSimpleDepts(Collection<Long> ids);

    /**
     * 获得指定编号的部门 Map
     *
     * @param ids 部门编号数组
     * @return 部门 Map
     */
    default Map<Long, SysDeptDO> getDeptMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<SysDeptDO> list = getSimpleDepts(ids);
        return CollectionUtils.convertMap(list, SysDeptDO::getId);
    }
}
