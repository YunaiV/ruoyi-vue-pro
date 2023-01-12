package cn.iocoder.yudao.framework.desensitize.handler;

import java.lang.annotation.Annotation;

/**
 * 脱敏处理器接口
 */
public interface DesensitizationHandler<T extends Annotation> {

    /**
     * 脱敏
     *
     * @param origin 原始字符串
     * @param arg    参数
     * @return 脱敏后的字符串
     */
    String desensitize(String origin, Object... arg);

    /**
     * 获取注解参数
     *
     * @param anno 注解
     * @return 注解参数
     */
    default Object[] getAnnotationArgs(T anno) {
        return new Object[0];
    }
}
