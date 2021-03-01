package cn.iocoder.dashboard.util;

import cn.hutool.core.util.*;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;

import java.lang.reflect.Field;

/**
 * 随机工具类
 *
 * @author 芋道源码
 */
public class RandomUtils {

    private static final int RANDOM_STRING_LENGTH = 10;

    public static String randomString() {
        return RandomUtil.randomString(RANDOM_STRING_LENGTH);
    }

    public static Long randomLong() {
        return RandomUtil.randomLong(0, Long.MAX_VALUE);
    }

    public static Integer randomInteger() {
        return RandomUtil.randomInt(0, Integer.MAX_VALUE);
    }

    public static Short randomShort() {
        return (short) RandomUtil.randomInt(0, Short.MAX_VALUE);
    }

    public static SysUserDO randomUserDO() {
        SysUserDO user = randomObject(SysUserDO.class);
        return user;
    }

    private static <T> T randomObject(Class<T> clazz) {
        // 创建对象
        T object = ReflectUtil.newInstance(clazz);
        // 遍历属性，设置随机值
        for (Field field : ReflectUtil.getFields(clazz)) {
            // 数字类型
            if (field.getType() == Long.class) {
                ReflectUtil.setFieldValue(object, field, randomLong());
                continue;
            }
            if (field.getType() == Integer.class) {
                ReflectUtil.setFieldValue(object, field, randomInteger());
                continue;
            }
            if (field.getType() == Short.class) {
                ReflectUtil.setFieldValue(object, field, randomShort());
                continue;
            }
            // 字符串类型
            if (field.getType() == String.class) {
                ReflectUtil.setFieldValue(object, field, randomString());
                continue;
            }
//            System.out.println();
        }
        return object;
    }

}
