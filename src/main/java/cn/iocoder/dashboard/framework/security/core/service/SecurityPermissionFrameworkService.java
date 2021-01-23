package cn.iocoder.dashboard.framework.security.core.service;

/**
 * Security 框架 Permission Service 接口，定义 security 组件需要的功能
 *
 * @author 芋道源码
 */
public interface SecurityPermissionFrameworkService {

    /**
     * 判断是否有权限
     *
     * @param permission 权限
     * @return 是否
     */
    boolean hasPermission(String permission);

    /**
     * 判断是否有权限，任一一个即可
     *
     * @param permissions 权限
     * @return 是否
     */
    boolean hasAnyPermissions(String... permissions);

}
