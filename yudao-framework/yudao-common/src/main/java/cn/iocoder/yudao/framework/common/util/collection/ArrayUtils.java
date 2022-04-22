package cn.iocoder.yudao.framework.common.util.collection;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.TypeUtil;
import org.springframework.cglib.core.TypeUtils;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * Array 工具类
 *
 * @author 芋道源码
 */
public class ArrayUtils {

    /**
     * 将 object 和 newElements 合并成一个数组
     *
     * @param object 对象
     * @param newElements 数组
     * @param <T> 泛型
     * @return 结果数组
     */
    @SafeVarargs
    public static <T> Consumer<T>[] append(Consumer<T> object, Consumer<T>... newElements) {
        if (object == null) {
            return newElements;
        }
        Consumer<T>[] result = ArrayUtil.newArray(Consumer.class, 1 + newElements.length);
        result[0] = object;
        System.arraycopy(newElements, 0, result, 1, newElements.length);
        return result;
    }

    public static <T, V> V[] toArray(Collection<T> from, Function<T, V> mapper) {
        return toArray(convertList(from, mapper));
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> from) {
        if (CollectionUtil.isEmpty(from)) {
            return (T[]) (new Object[0]);
        }
        return ArrayUtil.toArray(from, (Class<T>) CollectionUtil.getElementType(from.iterator()));
    }

}
