package cn.iocoder.dashboard.util.collection;

import cn.hutool.core.util.ArrayUtil;

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
    public static <T> T[] append(T object, T... newElements) {
        if (object == null) {
            return newElements;
        }
        T[] result = ArrayUtil.newArray(object.getClass(), 1 + newElements.length);
        result[0] = object;
        System.arraycopy(newElements, 0, result, 1, newElements.length);
        return result;
    }

}
