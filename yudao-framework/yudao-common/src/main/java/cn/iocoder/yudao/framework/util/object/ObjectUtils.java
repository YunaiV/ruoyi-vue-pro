package cn.iocoder.yudao.framework.util.object;

import cn.hutool.core.util.ObjectUtil;

import java.util.function.Consumer;

/**
 * Object 工具类
 *
 * @author 芋道源码
 */
public class ObjectUtils {

    public static <T> T clone(T object, Consumer<T> consumer) {
        T result = ObjectUtil.clone(object);
        if (result != null) {
            consumer.accept(result);
        }
        return result;
    }

    public static <T extends Comparable<T>> T max(T obj1, T obj2) {
        if (obj1 == null) {
            return obj2;
        }
        if (obj2 == null) {
            return obj1;
        }
        return obj1.compareTo(obj2) > 0 ? obj1 : obj2;
    }

}
