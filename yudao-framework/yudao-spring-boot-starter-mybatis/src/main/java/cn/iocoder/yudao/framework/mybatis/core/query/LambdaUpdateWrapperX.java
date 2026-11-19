package cn.iocoder.yudao.framework.mybatis.core.query;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.util.Objects;

/**
 * 拓展 MyBatis Plus LambdaUpdateWrapper 类，主要增加如下功能：
 * <p>
 * 1. 增加 setNullIfAbsent 方法，在字段值为 null 时，显式更新为空；空字符串、空集合不视为缺失。
 * 2. 支持传入实体和多个字段，统一处理空值更新；实体不加入 WHERE 条件。
 *
 * @param <T> 数据类型
 */
public class LambdaUpdateWrapperX<T> extends LambdaUpdateWrapper<T> {

    public LambdaUpdateWrapperX<T> setNullIfAbsent(SFunction<T, ?> column, Object value) {
        super.set(value == null, column, null);
        return this;
    }

    /**
     * 根据实体中的值，统一处理指定字段的空值更新
     *
     * @param entity 提供字段值的实体，不加入 WHERE 条件
     * @param columns 允许清空的字段
     * @return 更新条件
     */
    @SafeVarargs
    public final LambdaUpdateWrapperX<T> setNullIfAbsent(T entity, SFunction<T, ?>... columns) {
        Objects.requireNonNull(entity, "entity");
        for (SFunction<T, ?> column : columns) {
            setNullIfAbsent(column, column.apply(entity));
        }
        return this;
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public LambdaUpdateWrapperX<T> eq(boolean condition, SFunction<T, ?> column, Object value) {
        super.eq(condition, column, value);
        return this;
    }

    @Override
    public LambdaUpdateWrapperX<T> eq(SFunction<T, ?> column, Object value) {
        super.eq(column, value);
        return this;
    }

}
