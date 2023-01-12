package cn.iocoder.yudao.framework.desensitize.serializer;

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
import java.lang.reflect.Field;

/**
 * 脱敏序列化器
 */
public class StringDesensitizeSerializer extends StdSerializer<String> implements ContextualSerializer {
    private final DesensitizationHandler desensitizationHandler;

    protected StringDesensitizeSerializer(DesensitizationHandler desensitizationHandler) {
        super(String.class);
        this.desensitizationHandler = desensitizationHandler;
    }


    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        Desensitize annotation = beanProperty.getAnnotation(Desensitize.class);
        if (annotation == null) {
            return this;
        }

        return new StringDesensitizeSerializer(DesensitizationHandlerHolder.getDesensitizationHandler(annotation.desensitizationHandler()));
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
        SliderDesensitize sliderDesensitize = field.getAnnotation(SliderDesensitize.class);
        if (sliderDesensitize != null) {
            value = this.desensitizationHandler.desensitize(value, sliderDesensitize.prefixKeep(), sliderDesensitize.suffixKeep(), sliderDesensitize.replacer());
        }

        // 正则处理器
        RegexDesensitize regexDesensitize = field.getAnnotation(RegexDesensitize.class);
        if (regexDesensitize != null) {
            value = this.desensitizationHandler.desensitize(value, regexDesensitize.regex(), regexDesensitize.replacer());
        }

        // 自定义处理器
        Desensitize desensitize = field.getAnnotation(Desensitize.class);
        if (desensitize != null) {
            value = this.desensitizationHandler.desensitize(value);
        }

        gen.writeString(value);
    }

}
