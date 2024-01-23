package cn.iocoder.yudao.module.crm.framework.permission.core.util;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionRoleCodeEnum;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;

import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 数据权限工具类
 *
 * @author HUIHUI
 */
public class CrmPermissionUtils {

    /**
     * 校验用户是否是 CRM 管理员
     *
     * @return 是/否
     */
    public static boolean isCrmAdmin() {
        return SingletonManager.getPermissionApi().hasAnyRoles(getLoginUserId(), CrmPermissionRoleCodeEnum.CRM_ADMIN.getCode());
    }

    /**
     * 校验权限
     *
     * @param bizType   数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizId     数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @param userId    用户编号
     * @param levelEnum 权限级别
     * @return boolean
     */
    public static boolean hasPermission(Integer bizType, Long bizId, Long userId, CrmPermissionLevelEnum levelEnum) {
        List<CrmPermissionDO> permissionList = SingletonManager.getCrmPermissionService().getPermissionListByBiz(bizType, bizId);
        return anyMatch(permissionList, permission ->
                ObjUtil.equal(permission.getUserId(), userId) && ObjUtil.equal(permission.getLevel(), levelEnum.getLevel()));
    }

    /**
     * 静态内部类实现单例获取
     *
     * @author HUIHUI
     */
    private static class SingletonManager {

        private static final PermissionApi PERMISSION_API = SpringUtil.getBean(PermissionApi.class);
        private static final CrmPermissionService CRM_PERMISSION_SERVICE = SpringUtil.getBean(CrmPermissionService.class);

        public static PermissionApi getPermissionApi() {
            return PERMISSION_API;
        }

        public static CrmPermissionService getCrmPermissionService() {
            return CRM_PERMISSION_SERVICE;
        }

    }

}
