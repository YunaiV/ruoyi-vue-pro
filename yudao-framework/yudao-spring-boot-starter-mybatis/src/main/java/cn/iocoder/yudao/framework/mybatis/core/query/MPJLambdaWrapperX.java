package cn.iocoder.yudao.framework.mybatis.core.query;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.github.yulichang.toolkit.MPJWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 拓展 MyBatis Plus Join QueryWrapper 类，主要增加如下功能：
 * <p>
 * 1. 拼接条件的方法，增加 xxxIfPresent 方法，用于判断值不存在的时候，不要拼接到条件中。
 *
 * @param <T> 数据类型
 */
public class MPJLambdaWrapperX<T> extends MPJLambdaWrapper<T> {

    public MPJLambdaWrapperX<T> likeIfPresent(SFunction<T, ?> column, String val) {
        MPJWrappers.lambdaJoin().like(column, val);
        if (StringUtils.hasText(val)) {
            return (MPJLambdaWrapperX<T>) super.like(column, val);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> inIfPresent(SFunction<T, ?> column, Collection<?> values) {
        if (ObjectUtil.isAllNotEmpty(values) && !ArrayUtil.isEmpty(values)) {
            return (MPJLambdaWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> inIfPresent(SFunction<T, ?> column, Object... values) {
        if (ObjectUtil.isAllNotEmpty(values) && !ArrayUtil.isEmpty(values)) {
            return (MPJLambdaWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> eqIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (MPJLambdaWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> neIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (MPJLambdaWrapperX<T>) super.ne(column, val);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> gtIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (MPJLambdaWrapperX<T>) super.gt(column, val);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> geIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (MPJLambdaWrapperX<T>) super.ge(column, val);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> ltIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (MPJLambdaWrapperX<T>) super.lt(column, val);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> leIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (MPJLambdaWrapperX<T>) super.le(column, val);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            return (MPJLambdaWrapperX<T>) super.between(column, val1, val2);
        }
        if (val1 != null) {
            return (MPJLambdaWrapperX<T>) ge(column, val1);
        }
        if (val2 != null) {
            return (MPJLambdaWrapperX<T>) le(column, val2);
        }
        return this;
    }

    public MPJLambdaWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object[] values) {
        Object val1 = ArrayUtils.get(values, 0);
        Object val2 = ArrayUtils.get(values, 1);
        return betweenIfPresent(column, val1, val2);
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public <X> MPJLambdaWrapperX<T> eq(boolean condition, SFunction<X, ?> column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> eq(SFunction<X, ?> column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> orderByDesc(SFunction<X, ?> column) {
        //noinspection unchecked
        super.orderByDesc(true, column);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> last(String lastSql) {
        super.last(lastSql);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> in(SFunction<X, ?> column, Collection<?> coll) {
        super.in(column, coll);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> selectAll(Class<?> clazz) {
        super.selectAll(clazz);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> selectAll(Class<?> clazz, String prefix) {
        super.selectAll(clazz, prefix);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectAs(SFunction<S, ?> column, String alias) {
        super.selectAs(column, alias);
        return this;
    }

    @Override
    public <E> MPJLambdaWrapperX<T> selectAs(String column, SFunction<E, ?> alias) {
        super.selectAs(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectAs(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectAs(column, alias);
        return this;
    }

    @Override
    public <E, X> MPJLambdaWrapperX<T> selectAs(String index, SFunction<E, ?> column, SFunction<X, ?> alias) {
        super.selectAs(index, column, alias);
        return this;
    }

    @Override
    public <E> MPJLambdaWrapperX<T> selectAsClass(Class<E> source, Class<?> tag) {
        super.selectAsClass(source, tag);
        return this;
    }

    @Override
    public <E, F> MPJLambdaWrapperX<T> selectSub(Class<E> clazz, Consumer<MPJLambdaWrapper<E>> consumer, SFunction<F, ?> alias) {
        super.selectSub(clazz, consumer, alias);
        return this;
    }

    @Override
    public <E, F> MPJLambdaWrapperX<T> selectSub(Class<E> clazz, String st, Consumer<MPJLambdaWrapper<E>> consumer, SFunction<F, ?> alias) {
        super.selectSub(clazz, st, consumer, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectCount(SFunction<S, ?> column) {
        super.selectCount(column);
        return this;
    }

    @Override
    public MPJLambdaWrapperX<T> selectCount(Object column, String alias) {
        super.selectCount(column, alias);
        return this;
    }

    @Override
    public <X> MPJLambdaWrapperX<T> selectCount(Object column, SFunction<X, ?> alias) {
        super.selectCount(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectCount(SFunction<S, ?> column, String alias) {
        super.selectCount(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectCount(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectCount(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectSum(SFunction<S, ?> column) {
        super.selectSum(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectSum(SFunction<S, ?> column, String alias) {
        super.selectSum(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectSum(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectSum(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectMax(SFunction<S, ?> column) {
        super.selectMax(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectMax(SFunction<S, ?> column, String alias) {
        super.selectMax(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectMax(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectMax(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectMin(SFunction<S, ?> column) {
        super.selectMin(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectMin(SFunction<S, ?> column, String alias) {
        super.selectMin(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectMin(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectMin(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectAvg(SFunction<S, ?> column) {
        super.selectAvg(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectAvg(SFunction<S, ?> column, String alias) {
        super.selectAvg(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectAvg(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectAvg(column, alias);
        return this;
    }

    @Override
    public <S> MPJLambdaWrapperX<T> selectLen(SFunction<S, ?> column) {
        super.selectLen(column);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectLen(SFunction<S, ?> column, String alias) {
        super.selectLen(column, alias);
        return this;
    }

    @Override
    public <S, X> MPJLambdaWrapperX<T> selectLen(SFunction<S, ?> column, SFunction<X, ?> alias) {
        super.selectLen(column, alias);
        return this;
    }

}
