package cn.iocoder.yudao.framework.tenant.core.redis;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;

import java.time.Duration;

/**
 * 多租户拓展的 RedisKeyDefine 实现类
 *
 * 由于 Redis 不同于 MySQL 有 column 字段，无法通过类似 WHERE tenant_id = ? 的方式过滤
 * 所以需要通过在 Redis Key 上增加后缀的方式，进行租户之间的隔离。具体的步骤是：
 * 1. 假设 Redis Key 是 user:%d，示例是 user:1；对应到多租户的 Redis Key 是 user:%d:%d，
 * 2. 在 Redis DAO 中，需要使用 {@link #formatKey(Object...)} 方法，进行 Redis Key 的格式化
 *
 * 注意，大多数情况下，并不用使用 TenantRedisKeyDefine 实现。主要的使用场景，还是 Redis Key 可能存在冲突的情况。
 * 例如说，租户 1 和 2 都有一个手机号作为 Key，则他们会存在冲突的问题
 *
 * @author 芋道源码
 */
public class TenantRedisKeyDefine extends RedisKeyDefine {

    /**
     * 多租户的 KEY 模板
     */
    private static final String KEY_TEMPLATE_SUFFIX = ":%d";

    public TenantRedisKeyDefine(String memo, String keyTemplate, KeyTypeEnum keyType, Class<?> valueType, Duration timeout) {
        super(memo, buildKeyTemplate(keyTemplate), keyType, valueType, timeout);
    }

    public TenantRedisKeyDefine(String memo, String keyTemplate, KeyTypeEnum keyType, Class<?> valueType, TimeoutTypeEnum timeoutType) {
        super(memo, buildKeyTemplate(keyTemplate), keyType, valueType, timeoutType);
    }

    private static String buildKeyTemplate(String keyTemplate) {
        return keyTemplate + KEY_TEMPLATE_SUFFIX;
    }

    @Override
    public String formatKey(Object... args) {
        args = ArrayUtil.append(args, TenantContextHolder.getRequiredTenantId());
        return super.formatKey(args);
    }

}
