package cn.iocoder.yudao.framework.desensitize.core.base.annotation;

import cn.iocoder.yudao.framework.desensitize.core.base.handler.DesensitizationHandler;
import cn.iocoder.yudao.framework.desensitize.serializer.StringDesensitizeSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Desensitize 顶级脱敏注解
 */
@Documented
@Target({ ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = StringDesensitizeSerializer.class)
public @interface Desensitize {

    /**
     * 脱敏处理器
     */
    Class<? extends DesensitizationHandler> desensitizationBy();
}
