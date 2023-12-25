package cn.iocoder.yudao.module.crm.framework.permission.core.util;

import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionRoleCodeEnum;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;

/**
 * 数据权限工具类
 *
 * @author HUIHUI
 */
public class CrmPermissionUtils {

    // TODO @puhui999：isCrmAdmin。换成这个名字；因为 validate 一般是参数校验，不符合抛出异常；一般是 isXXXValid 才会返回 true false
    /**
     * 校验用户是否是 CRM 管理员
     *
     * @return 是/否
     */
    public static boolean validateAdminUser() {
        return SingletonManager.getPermissionApi().hasAnyRoles(getUserId(), CrmPermissionRoleCodeEnum.CRM_ADMIN.getCode());
    }

    // TODO @puhui999：这个不需要哈，直接用原本的 SecuriyUtils 去拿更方便一些；
    /**
     * 获得用户编号
     *
     * @return 用户编号
     */
    private static Long getUserId() {
        return WebFrameworkUtils.getLoginUserId();
    }

    /**
     * 静态内部类实现单例获取
     *
     * @author HUIHUI
     */
    private static class SingletonManager {

        private static final PermissionApi PERMISSION_API = SpringUtil.getBean(PermissionApi.class);

        public static PermissionApi getPermissionApi() {
            return PERMISSION_API;
        }

    }

}
