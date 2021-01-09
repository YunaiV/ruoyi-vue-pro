package com.ruoyi.system.service;

import java.util.List;
import java.util.Set;

import com.ruoyi.common.core.domain.entity.SysRole;

/**
 * 角色业务层
 *
 * @author ruoyi
 */
public interface ISysRoleService {

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public Set<String> selectRolePermissionByUserId(Long userId);

    /**
     * 修改数据权限信息
     *
     * @param role 角色信息
     * @return 结果
     */
    public int authDataScope(SysRole role);

}
