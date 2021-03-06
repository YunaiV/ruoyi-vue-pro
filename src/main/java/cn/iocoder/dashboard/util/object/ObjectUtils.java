package cn.iocoder.dashboard.util.object;

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

}
