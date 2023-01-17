package cn.iocoder.yudao.framework.desensitize.serializer;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.desensitize.core.base.annotation.Desensitize;
import cn.iocoder.yudao.framework.desensitize.core.base.handler.DesensitizationHandler;
import cn.iocoder.yudao.framework.desensitize.core.base.DesensitizationHandlerHolder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

// TODO @城：挪到 base/serializer 包下
/**
 * 脱敏序列化器
 */
public class StringDesensitizeSerializer extends StdSerializer<String> implements ContextualSerializer {

    private DesensitizationHandler desensitizationHandler;

    protected StringDesensitizeSerializer() {
        super(String.class);
    }

    // TODO @城：get 和 set 方法是必须的么？如果是的话，可以换成 lombok 注解哈，简洁一点~
    public DesensitizationHandler getDesensitizationHandler() {
        return desensitizationHandler;
    }

    public void setDesensitizationHandler(DesensitizationHandler desensitizationHandler) {
        this.desensitizationHandler = desensitizationHandler;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        Desensitize annotation = beanProperty.getAnnotation(Desensitize.class);
        if (annotation == null) {
            return this;
        }
        StringDesensitizeSerializer serializer = new StringDesensitizeSerializer();
        serializer.setDesensitizationHandler(DesensitizationHandlerHolder.getDesensitizationHandler(annotation.desensitizationBy()));
        return serializer;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        if (StrUtil.isBlank(value)) {
            gen.writeNull();
            return;
        }

        // TODO @城：抽个 private getField 方法。让这个方法的逻辑主干，更清晰
        String currentName = gen.getOutputContext().getCurrentName();
        Object currentValue = gen.getCurrentValue();
        Class<?> currentValueClass = currentValue.getClass();
        Field field = ReflectUtil.getField(currentValueClass, currentName);

        // 自定义处理器
        Desensitize[] annotations = AnnotationUtil.getCombinationAnnotations(field, Desensitize.class);
        if (ArrayUtil.isEmpty(annotations)) {
            gen.writeString(value);
            return;
        }
        for (Annotation annotation : field.getAnnotations()) {
            if (AnnotationUtil.hasAnnotation(annotation.annotationType(), Desensitize.class)) {
                value = this.desensitizationHandler.desensitize(value, annotation);
                gen.writeString(value);
                return;
            }
        }
        gen.writeString(value);
    }

}
