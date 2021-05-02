package cn.iocoder.yudao.framework.test.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
     * 比对两个对象的属性是否一致
     *
     * 注意，如果 expected 存在的属性，actual 不存在的时候，会进行忽略
     *
     * @param expected 期望对象
     * @param actual 实际对象
     * @param ignoreFields 忽略的属性数组
     * @return 是否一致
     */
    public static boolean isPojoEquals(Object expected, Object actual, String... ignoreFields) {
        Field[] expectedFields = ReflectUtil.getFields(expected.getClass());
        return Arrays.stream(expectedFields).allMatch(expectedField -> {
            // 如果是忽略的属性，则不进行比对
            if (ArrayUtil.contains(ignoreFields, expectedField.getName())) {
                return true;
            }
            // 忽略不存在的属性
            Field actualField = ReflectUtil.getField(actual.getClass(), expectedField.getName());
            if (actualField == null) {
                return true;
            }
            return Objects.equals(ReflectUtil.getFieldValue(expected, expectedField),
                    ReflectUtil.getFieldValue(actual, actualField));
        });
    }

    /**
     * 执行方法，校验抛出的 Service 是否符合条件
     *
     * @param executable 业务异常
     * @param errorCode 错误码对象
     * @param messageParams 消息参数
     */
    public static void assertServiceException(Executable executable, ErrorCode errorCode, Object... messageParams) {
        // 调用方法
        ServiceException serviceException = assertThrows(ServiceException.class, executable);
        // 校验错误码
        Assertions.assertEquals(errorCode.getCode(), serviceException.getCode(), "错误码不匹配");
        String message = ServiceExceptionUtil.doFormat(errorCode.getCode(), errorCode.getMsg(), messageParams);
        Assertions.assertEquals(message, serviceException.getMessage(), "错误提示不匹配");
    }

}
