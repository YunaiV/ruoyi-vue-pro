package cn.iocoder.yudao.module.crm.framework.utils;

import java.util.Collection;

// TODO @puhui999：改成 CrmPermissionUtils；
/**
 * 数据读写权限校验工具类
 *
 * @author HUIHUI
 */
public class AuthUtil {

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
        // TODO @puhui999：从代码角度来说，最好使用 CollUtil.contains
        return roUserIds.contains(userId);
    }

    /**
     * 判断当前数据对用户来说是否是可读写的
     *
     * @param rwUserIds 当前操作数据的读写权限的用户编号数组
     * @param userId    当前操作数据的用户编号
     * @return boolean 是/否
     */
    public static boolean isReadAndWrite(Collection<Long> rwUserIds, Long userId) {
        return rwUserIds.contains(userId);
    }

}
