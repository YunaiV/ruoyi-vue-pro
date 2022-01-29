package cn.iocoder.yudao.module.system.service.permission;

import java.util.Collection;
import java.util.Set;
/**
 * 权限 Core Service 接口
 *
 * 提供用户-角色、角色-菜单、角色-部门的关联权限处理
 *
 * @author 芋道源码
 */
public interface SysPermissionCoreService {
    /**
     * 获得拥有多个角色的用户编号集合
     *
     * @param roleIds 角色编号集合
     * @return 用户编号集合
     */
    Set<Long> getUserRoleIdListByRoleIds(Collection<Long> roleIds);
}
