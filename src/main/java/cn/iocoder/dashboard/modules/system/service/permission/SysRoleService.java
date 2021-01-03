package cn.iocoder.dashboard.modules.system.service.permission;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.permission.SysRoleDO;

import java.util.Collection;
import java.util.List;

/**
 * 角色 Service 接口
 *
 * @author 芋道源码
 */
public interface SysRoleService {

    /**
     * 初始化
     */
    void init();

    /**
     * 获得角色数组，从缓存中
     *
     * @param roleIds 角色编号数组
     * @return 角色数组
     */
    List<SysRoleDO> listRolesFromCache(Collection<Long> roleIds);

    /**
     * 判断角色数组中，是否有管理员
     *
     * @param roleList 角色数组
     * @return 是否有管理员
     */
    boolean hasAnyAdmin(Collection<SysRoleDO> roleList);

}
