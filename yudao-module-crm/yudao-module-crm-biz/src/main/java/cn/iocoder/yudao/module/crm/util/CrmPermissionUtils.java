package cn.iocoder.yudao.module.crm.util;

import cn.hutool.core.util.StrUtil;
import com.github.yulichang.wrapper.MPJLambdaWrapper;

// TODO @puhui999：这个类还要哇？
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
    public static void builderRightJoinQuery(MPJLambdaWrapper<?> mpjLambdaWrapper, Integer bizTyp, Long userId) {
        String querySql = "(SELECT t1.biz_id FROM crm_permission t1 WHERE (t1.biz_type = {} AND t1.user_id = {})) t2 on t.id = t2.biz_id";
        // 默认主表别名是 t
        mpjLambdaWrapper.rightJoin(StrUtil.format(querySql, bizTyp, userId));
    }

}
