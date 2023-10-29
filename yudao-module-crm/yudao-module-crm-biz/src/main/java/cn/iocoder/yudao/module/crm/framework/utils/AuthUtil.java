package cn.iocoder.yudao.module.crm.framework.utils;

import java.util.Collection;

/**
 * 数据读写权限校验工具类
 *
 * @author HUIHUI
 */
public class AuthUtil {

    /**
     * 判断当前数据对用户来说是否是只读的
     *
     * @param roUserIds 当前操作数据的只读权限的用户编号数组
     * @param userId    当前操作数据的用户编号
     * @return boolean 是/否
     */
    public static boolean isReadOnly(Collection<Long> roUserIds, Long userId) {
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
