package cn.iocoder.yudao.framework.tenant.core.db;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.toolkit.SqlParserUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.HashSet;
import java.util.Set;

/**
 * 基于 MyBatis Plus 多租户的功能，实现 DB 层面的多租户的功能
 *
 * @author 芋道源码
 */
public class TenantDatabaseInterceptor implements TenantLineHandler {

    private final Set<String> ignoreTables = new HashSet<>();

    public TenantDatabaseInterceptor(TenantProperties properties) {
        // 不同 DB 下，大小写的习惯不同，所以需要都添加进去
        properties.getIgnoreTables().forEach(table -> {
            ignoreTables.add(table.toLowerCase());
            ignoreTables.add(table.toUpperCase());
        });
        // 在 OracleKeyGenerator 中，生成主键时，会查询这个表，查询这个表后，会自动拼接 TENANT_ID 导致报错
        ignoreTables.add("DUAL");
    }

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContextHolder.getRequiredTenantId());
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return TenantContextHolder.isIgnore() // 情况一，全局忽略多租户
                || CollUtil.contains(ignoreTables, SqlParserUtils.removeWrapperSymbol(tableName)); // 情况二，忽略多租户的表
    }

}
