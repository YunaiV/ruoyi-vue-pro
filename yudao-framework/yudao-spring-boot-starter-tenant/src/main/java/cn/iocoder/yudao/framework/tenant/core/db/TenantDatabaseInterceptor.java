package cn.iocoder.yudao.framework.tenant.core.db;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.AllArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * 基于 MyBatis Plus 多租户的功能，实现 DB 层面的多租户的功能
 *
 * @author 芋道源码
 */
@AllArgsConstructor
public class TenantDatabaseInterceptor implements TenantLineHandler {

    private final TenantProperties properties;

    @Override
    public Expression getTenantId() {
        return new StringValue(TenantContextHolder.getRequiredTenantId().toString());
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 如果实体类继承 TenantBaseDO 类，则是多租户表，不进行忽略
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        if (tableInfo != null && TenantBaseDO.class.isAssignableFrom(tableInfo.getEntityType())) {
            return false;
        }
        // 不包含，说明要过滤
        return !CollUtil.contains(properties.getTables(), tableName);
    }

}
