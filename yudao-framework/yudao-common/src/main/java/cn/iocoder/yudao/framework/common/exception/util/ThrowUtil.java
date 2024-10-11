package cn.iocoder.yudao.framework.common.exception.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @className: ThrowUtil
 * @author: Wqh
 * @date: 2024/10/10 10:43
 * @Version: 1.0
 * @description: 条件异常工具类
 */
public class ThrowUtil {
    private ThrowUtil() {

    }

    public static void ifThrow(boolean condition, ErrorCode message) {
        if (condition) {
            throw exception(message);
        }
    }
    public static void ifThrow(boolean condition, ErrorCode message,Object... params) {
        if (condition) {
            throw exception(message,params);
        }
    }
    public static void ifNotEqualsThrow(Object firstValue, Object secondValue, ErrorCode message) {
        if (Objects.isNull(firstValue) || Objects.isNull(secondValue) ||
                !Objects.equals(firstValue, secondValue)) {
            throw exception(message);
        }
    }

    public static void ifEmptyThrow(Object data, ErrorCode message) {
        if (ObjectUtil.isEmpty(data)){
            throw exception(message);
        }
    }

    public static void ifBlankThrow(String s, ErrorCode message) {
        if (CharSequenceUtil.isBlank(s)){
            throw exception(message);
        }
    }

    public static void ifGreater(Long no, long l, ErrorCode message) {
        if (no > l) {
            throw exception(message);
        }
    }


    /*public static void ifNullThrow(Object data, String code) {
        if (Objects.isNull(data)) {
            throw ExceptionUtil.exception(code);
        }
    }

    public static void ifNotNullThrow(Object data, String code) {
        if (Objects.nonNull(data)) {
            throw ExceptionUtil.exception(code);
        }
    }

    public static void ifEmptyThrow(Object data, String code) {
        boolean result = (data instanceof String && StrUtil.isBlank((CharSequence) data)) ||
                (data instanceof Collection && CollUtil.isEmpty((Collection<?>) data)) ||
                (data instanceof Map && MapUtil.isEmpty((Map<?, ?>) data));
        if (result) {
            throw ExceptionUtil.exception(code);
        }
    }

    public static void ifNotEmptyThrow(Object data, String code) {
        boolean result = (data instanceof String && StrUtil.isNotBlank((CharSequence) data)) ||
                (data instanceof Collection && CollUtil.isNotEmpty((Collection<?>) data)) ||
                (data instanceof Map && MapUtil.isNotEmpty((Map<?, ?>) data));
        if (result) {
            throw ExceptionUtil.exception(code);
        }
    }

    public static void ifEqualsThrow(Object firstValue, Object secondValue, String code) {
        if (Objects.isNull(firstValue) || Objects.isNull(secondValue) || firstValue.equals(secondValue)) {
            throw ExceptionUtil.exception(code);
        }
    }

    public static void ifNotEqualsThrow(Object firstValue, Object secondValue, String code) {
        if (Objects.isNull(firstValue) || Objects.isNull(secondValue) || !firstValue.equals(secondValue)) {
            throw ExceptionUtil.exception(code);
        }
    }

    public static void ifThrowWithObject(boolean condition, String code, Object... objects) {
        if (condition) {
            throw ExceptionUtil.objectException(code, objects);
        }
    }

    public static void ifNullThrowWithObject(Object data, String code, Object... objects) {
        if (Objects.isNull(data)) {
            throw ExceptionUtil.objectException(code, objects);
        }
    }

    public static void ifNotNullThrowWithObject(Object data, String code, Object... objects) {
        if (Objects.nonNull(data)) {
            throw ExceptionUtil.objectException(code, objects);
        }
    }

    public static void ifEmptyThrowWithObject(Object data, String code, Object... objects) {
        boolean result = (data instanceof String && StrUtil.isBlank((CharSequence) data)) ||
                (data instanceof Collection && CollUtil.isEmpty((Collection<?>) data)) ||
                (data instanceof Map && MapUtil.isEmpty((Map<?, ?>) data));
        if (result) {
            throw ExceptionUtil.objectException(code, objects);
        }
    }

    public static void ifNotEmptyThrowWithObject(Object data, String code, Object... objects) {
        boolean result = (data instanceof String && StrUtil.isNotBlank((CharSequence) data)) ||
                (data instanceof Collection && CollUtil.isNotEmpty((Collection<?>) data)) ||
                (data instanceof Map && MapUtil.isNotEmpty((Map<?, ?>) data));
        if (result) {
            throw ExceptionUtil.objectException(code, objects);
        }
    }

    public static void ifEqualsThrowWithObject(Object firstValue, Object secondValue, String code, Object... objects) {
        if (Objects.isNull(firstValue) || Objects.isNull(secondValue) || firstValue.equals(secondValue)) {
            throw ExceptionUtil.objectException(code, objects);
        }
    }

    public static void ifNotEqualsThrowWithObject(Object firstValue, Object secondValue, String code, Object... objects) {
        if (Objects.isNull(firstValue) || Objects.isNull(secondValue) || !firstValue.equals(secondValue)) {
            throw ExceptionUtil.objectException(code, objects);
        }
    }

    public static void ifThrowWithParam(boolean condition, String code, Object... params) {
        if (condition) {
            throw ExceptionUtil.paramException(code, params);
        }
    }

    public static void ifNullThrowWithParam(Object data, String code, Object... params) {
        if (Objects.isNull(data)) {
            throw ExceptionUtil.paramException(code, params);
        }
    }

    public static void ifNotNullThrowWithParam(Object data, String code, Object... params) {
        if (Objects.nonNull(data)) {
            throw ExceptionUtil.paramException(code, params);
        }
    }

    public static void ifEmptyThrowWithParam(Object data, String code, Object... params) {
        boolean result = (data instanceof String && StrUtil.isBlank((CharSequence) data)) ||
                (data instanceof Collection && CollUtil.isEmpty((Collection<?>) data)) ||
                (data instanceof Map && MapUtil.isEmpty((Map<?, ?>) data));
        if (result) {
            throw ExceptionUtil.paramException(code, params);
        }
    }

    public static void ifNotEmptyThrowWithParam(Object data, String code, Object... params) {
        boolean result = (data instanceof String && StrUtil.isNotBlank((CharSequence) data)) ||
                (data instanceof Collection && CollUtil.isNotEmpty((Collection<?>) data)) ||
                (data instanceof Map && MapUtil.isNotEmpty((Map<?, ?>) data));
        if (result) {
            throw ExceptionUtil.paramException(code, params);
        }
    }

    public static void ifEqualsThrowWithParam(Object firstValue, Object secondValue, String code, Object... params) {
        if (Objects.isNull(firstValue) || Objects.isNull(secondValue) || firstValue.equals(secondValue)) {
            throw ExceptionUtil.paramException(code, params);
        }
    }

    public static void ifNotEqualsThrowWithParam(Object firstValue, Object secondValue, String code, Object... params) {
        if (Objects.isNull(firstValue) || Objects.isNull(secondValue) || !firstValue.equals(secondValue)) {
            throw ExceptionUtil.paramException(code, params);
        }
    }

    public static void ifThrowForMessage(boolean condition, String code, String message) {
        if (condition) {
            throw ExceptionUtil.messageException(code, message);
        }
    }

    public static void ifNullThrowForMessage(Object data, String code, String message) {
        if (Objects.isNull(data)) {
            throw ExceptionUtil.messageException(code, message);
        }
    }

    public static void ifNotNullThrowForMessage(Object data, String code, String message) {
        if (Objects.nonNull(data)) {
            throw ExceptionUtil.messageException(code, message);
        }
    }

    public static void ifNotEqualsThrowForMessage(Object firstValue, Object secondValue, String code, String message) {
        if (Objects.isNull(firstValue) || Objects.isNull(secondValue) || !firstValue.equals(secondValue)) {
            throw ExceptionUtil.messageException(code, message);
        }
    }*/
}
