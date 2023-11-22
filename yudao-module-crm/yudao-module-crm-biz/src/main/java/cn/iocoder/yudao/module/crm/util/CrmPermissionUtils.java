package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.util.StrUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

/**
 * 数据权限工具类
 *
 * @author HUIHUI
 */
public class CrmPermissionUtils {

    /**
     * 构建用户可查看数据连表条件
     *
     * @param mpjLambdaWrapper 多表查询 wrapper
     * @param bizTyp           模块类型
     * @param userId           用户
     */
    public static void builderLeftJoinQuery(MPJLambdaWrapper<?> mpjLambdaWrapper, Integer bizTyp, Long userId) {
        // 默认主表别名是 t
        mpjLambdaWrapper.leftJoin(StrUtil.format("(" +
                "select p.biz_id from crm_permission p" +
                " where p.biz_type = {} and p.user_id = {}" +
                ") t2" +
                " on t.id = t2.biz_id", bizTyp, userId));
    }

}
