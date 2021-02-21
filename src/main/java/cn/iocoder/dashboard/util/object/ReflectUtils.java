package cn.iocoder.dashboard.util.object;

import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 反射 Util 工具类，解决 {@link cn.hutool.core.util.ReflectUtil} 无法满足的情况
 *
 * @author 芋道源码
 */
public class ReflectUtils {

    public static void setFinalFieldValue(Object obj, String fieldName, Object value) {
        // 获得 Field
        if (obj == null) {
            return;
        }
        Field field = ReflectUtil.getField(obj.getClass(), fieldName);
        if (field == null) {
            return;
        }

        // 获得该 Field 的 modifiers 属性，为非 final
        ReflectUtil.setFieldValue(field, "modifiers", field.getModifiers() & ~Modifier.FINAL);
        // 真正，设置值
        ReflectUtil.setFieldValue(obj, field, value);
    }

}
