package cn.iocoder.dashboard.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.dashboard.common.exception.ErrorCode;
import cn.iocoder.dashboard.common.exception.ServiceException;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * 单元测试，assert 断言工具类
 *
 * @author 芋道源码
 */
public class AssertUtils {

    /**
     * 比对两个对象的属性是否一致
     *
     * 注意，如果 expected 存在的属性，actual 不存在的时候，会进行忽略
     *
     * @param expected 期望对象
     * @param actual 实际对象
     * @param ignoreFields 忽略的属性数组
     */
    public static void assertPojoEquals(Object expected, Object actual, String... ignoreFields) {
        Field[] expectedFields = ReflectUtil.getFields(expected.getClass());
        Arrays.stream(expectedFields).forEach(expectedField -> {
            // 如果是忽略的属性，则不进行比对
            if (ArrayUtil.contains(ignoreFields, expectedField.getName())) {
                return;
            }
            // 忽略不存在的属性
            Field actualField = ReflectUtil.getField(actual.getClass(), expectedField.getName());
            if (actualField == null) {
                return;
            }
            // 比对
            Assertions.assertEquals(
                    ReflectUtil.getFieldValue(expected, expectedField),
                    ReflectUtil.getFieldValue(actual, actualField),
                    String.format("Field(%s) 不匹配", expectedField.getName())
            );
        });
    }

    /**
     * 比对抛出的 ServiceException 是否匹配
     *
     * @param errorCode 错误码对象
     * @param serviceException 业务异常
     */
    public static void assertExceptionEquals(ErrorCode errorCode, ServiceException serviceException) {
        Assertions.assertEquals(errorCode.getCode(), serviceException.getCode(), "错误码不匹配");
        Assertions.assertEquals(errorCode.getMessage(), serviceException.getMessage(), "错误提示不匹配");
    }

}
