package cn.iocoder.yudao.adminserver.framework.datapermission.core.service;

import cn.iocoder.yudao.adminserver.framework.datapermission.core.service.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.security.core.LoginUser;

/**
 * 基于部门的数据权限 Service 接口
 *
 * @author 芋道源码
 */
public interface DeptDataPermissionService {

    /**
     * 获得登陆用户的部门数据权限
     *
     * @param loginUser 登陆用户
     * @return 部门数据权限
     */
    DeptDataPermissionRespDTO getDeptDataPermission(LoginUser loginUser);

}
