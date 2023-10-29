package cn.iocoder.yudao.module.crm.framework.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.framework.core.aop.CrmPermissionAspect;

import java.util.Collection;

/**
 * 数据读写权限校验工具类
 *
 * @author HUIHUI
 */
public class CrmPermissionUtils {

    // TODO @puhui999：负责人是单独的字段哈；
    // TODO @puhui999：额外校验，如果是管理员，可以查看所有；看着要做成有状态的了，可能要搞个 CrmPermissionService 咧；

    /**
     * 判断当前数据对用户来说是否是只读的
     *
     * @param roUserIds 当前操作数据的只读权限的用户编号数组
     * @param userId    当前操作数据的用户编号
     * @return boolean 是/否
     */
    public static boolean isReadOnly(Collection<Long> roUserIds, Long userId) {
        return CollUtil.contains(roUserIds, id -> ObjUtil.equal(id, userId));
    }

    /**
     * 判断当前数据对用户来说是否是可读写的
     *
     * @param rwUserIds 当前操作数据的读写权限的用户编号数组
     * @param userId    当前操作数据的用户编号
     * @return boolean 是/否
     */
    public static boolean isReadAndWrite(Collection<Long> rwUserIds, Long userId) {
        return CollUtil.contains(rwUserIds, id -> ObjUtil.equal(id, userId));
    }

    public static void setCrmTransferInfo(Long userId, Integer userType, Object crmTransferBaseVO) {
        CrmPermissionAspect.setCrmTransferInfo(userId, userType, crmTransferBaseVO);
    }

    public static void setCrmTransferInfo(Long userId, Integer userType) {
        CrmPermissionAspect.setCrmTransferInfo(userId, userType);
    }

}
