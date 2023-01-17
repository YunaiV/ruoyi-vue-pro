package cn.iocoder.yudao.framework.desensitize.core.base.handler;

import java.lang.annotation.Annotation;

/**
 * 脱敏处理器接口
 */
public interface DesensitizationHandler<T extends Annotation> {

    /**
     * 脱敏
     *
     * @param origin 原始字符串
     * @param anno   注解信息 // TODO 不要这样的缩写哈，anno -> annotation
     * @return 脱敏后的字符串
     */
    String desensitize(String origin, T anno);

}
