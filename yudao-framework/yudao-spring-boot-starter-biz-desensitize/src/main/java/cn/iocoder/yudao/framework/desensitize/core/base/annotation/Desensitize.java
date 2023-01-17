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

// TODO @城：每个接口上，author 写下哈。
// TODO @城：Desensitize = 》DesensitizeBy
// TODO @城：Desensitize 类注释，方便读者阅读理解
/**
 * Desensitize 顶级脱敏注解
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside // TODO @城：这个的作用，也可以写下
@JsonSerialize(using = StringDesensitizeSerializer.class) // TODO @城：这个的作用，也可以写下
public @interface Desensitize {

    /**
     * 脱敏处理器
     */
    Class<? extends DesensitizationHandler> desensitizationBy(); // TODO @城：desensitizationBy -> handler
}
