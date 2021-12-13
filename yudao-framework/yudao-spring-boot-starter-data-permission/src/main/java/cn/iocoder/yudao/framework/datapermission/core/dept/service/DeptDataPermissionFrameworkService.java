package cn.iocoder.yudao.framework.datapermission.core.dept.service;

import cn.iocoder.yudao.framework.datapermission.core.dept.service.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.security.core.LoginUser;

/**
 * 基于部门的数据权限 Framework Service 接口
 * 目前的实现类是 SysPermissionServiceImpl 类
 *
 * @author 芋道源码
 */
public interface DeptDataPermissionFrameworkService {

    /**
     * 获得登陆用户的部门数据权限
     *
     * @param loginUser 登陆用户
     * @return 部门数据权限
     */
    DeptDataPermissionRespDTO getDeptDataPermission(LoginUser loginUser);

}
