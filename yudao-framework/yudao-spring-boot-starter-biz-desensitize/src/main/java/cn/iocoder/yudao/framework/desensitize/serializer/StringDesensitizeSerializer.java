package cn.iocoder.yudao.framework.desensitize.serializer;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.desensitize.annotation.Desensitize;
import cn.iocoder.yudao.framework.desensitize.annotation.RegexDesensitize;
import cn.iocoder.yudao.framework.desensitize.annotation.SliderDesensitize;
import cn.iocoder.yudao.framework.desensitize.handler.DesensitizationHandler;
import cn.iocoder.yudao.framework.desensitize.handler.DesensitizationHandlerHolder;
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

/**
 * 脱敏序列化器
 */
public class StringDesensitizeSerializer extends StdSerializer<String> implements ContextualSerializer {
    private DesensitizationHandler desensitizationHandler;

    protected StringDesensitizeSerializer() {
        super(String.class);
    }

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
        serializer.setDesensitizationHandler(DesensitizationHandlerHolder.getDesensitizationHandler(annotation.desensitizationHandler()));
        return serializer;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        if (StrUtil.isBlank(value)) {
            gen.writeNull();
            return;
        }

        String currentName = gen.getOutputContext().getCurrentName();
        Object currentValue = gen.getCurrentValue();
        Class<?> currentValueClass = currentValue.getClass();
        Field field = ReflectUtil.getField(currentValueClass, currentName);

        // 滑动处理器
        SliderDesensitize sliderDesensitize = ArrayUtil.firstNonNull(AnnotationUtil.getCombinationAnnotations(field, SliderDesensitize.class));
        if (sliderDesensitize != null) {
            value = this.desensitizationHandler.desensitize(value, this.desensitizationHandler.getAnnotationArgs(sliderDesensitize));
            gen.writeString(value);
            return;
        }

        // 正则处理器
        RegexDesensitize regexDesensitize = ArrayUtil.firstNonNull(AnnotationUtil.getCombinationAnnotations(field, RegexDesensitize.class));
        if (regexDesensitize != null) {
            value = this.desensitizationHandler.desensitize(value, this.desensitizationHandler.getAnnotationArgs(regexDesensitize));
            gen.writeString(value);
            return;
        }

        // 自定义处理器
        Desensitize[] annotations = AnnotationUtil.getCombinationAnnotations(field, Desensitize.class);
        if (ArrayUtil.isEmpty(annotations)) {
            gen.writeString(value);
            return;
        }

        for (Annotation annotation : field.getAnnotations()) {
            if (AnnotationUtil.hasAnnotation(annotation.annotationType(), Desensitize.class)) {
                value = this.desensitizationHandler.desensitize(value, this.desensitizationHandler.getAnnotationArgs(annotation));
                gen.writeString(value);
                return;
            }
        }

        gen.writeString(value);
    }

}
