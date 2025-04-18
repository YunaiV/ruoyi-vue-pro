package cn.iocoder.yudao.framework.common.util.object;

import cn.hutool.core.bean.BeanUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Bean 工具类
 *
 * 1. 默认使用 {@link cn.hutool.core.bean.BeanUtil} 作为实现类，虽然不同 bean 工具的性能有差别，但是对绝大多数同学的项目，不用在意这点性能
 * 2. 针对复杂的对象转换，可以搜参考 AuthConvert 实现，通过 mapstruct + default 配合实现
 *
 * @author 芋道源码
 */
public class BeanUtils {

    public static <T> T toBean(Object source, Class<T> targetClass) {
        return BeanUtil.toBean(source, targetClass);
    }

    public static <T> T toBean(Object source, Class<T> targetClass, Consumer<T> peek) {
        T target = toBean(source, targetClass);
        if (target != null) {
            peek.accept(target);
        }
        return target;
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        return CollectionUtils.convertList(source, s -> toBean(s, targetType));
    }

    public static <S, T> List<T> toBean(List<S> source, Class<T> targetType, Consumer<T> peek) {
        List<T> list = toBean(source, targetType);
        if (list != null) {
            list.forEach(peek);
        }
        return list;
    }

    public static <S, T> PageResult<T> toBean(PageResult<S> source, Class<T> targetType) {
        return toBean(source, targetType, null);
    }

    public static <S, T> PageResult<T> toBean(PageResult<S> source, Class<T> targetType, Consumer<T> peek) {
        if (source == null) {
            return null;
        }
        List<T> list = toBean(source.getList(), targetType);
        if (peek != null) {
            list.forEach(peek);
        }
        return new PageResult<>(list, source.getTotal());
    }

    /**
     *
     * check if every non-null field of instanceA is already present in classB
     *
     * @param instanceA
     * @param classB
     * @return
     */
    public static boolean areAllNonNullFieldsPresent(Object instanceA, Class<?> classB) {
        Class<?> classA = instanceA.getClass();
        if (instanceA == null) {
            throw new IllegalArgumentException("Instance of class A cannot be null");
        }

        Set<String> classBFieldNames = new HashSet<>();
        for (Field field : ObjectUtils.getFields(classB)) {
            classBFieldNames.add(field.getName());
        }

        for (Field field : ObjectUtils.getFields(classA)) {
            field.setAccessible(true); // Allow access to private fields
            try {
                Object value = field.get(instanceA);
                if (value != null && !classBFieldNames.contains(field.getName())) {
                    return true; // Non-null field in A is not present in B
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }

        return false;
    }

    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target, false);
    }


    /**
     * 将 Map 中的值转换为目标类型
     **/
    public static <K,S, T> Map<K,T> toBean(Map<K,S> source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        Map<K,T> result = new HashMap<>();
        for (Map.Entry<K, S> entry : source.entrySet()) {
            result.put(entry.getKey(), toBean(entry.getValue(), targetType));
        }
        return result;
    }

}