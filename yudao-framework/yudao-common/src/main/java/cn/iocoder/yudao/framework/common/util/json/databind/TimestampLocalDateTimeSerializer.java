package cn.iocoder.yudao.framework.common.util.json.databind;

import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 基于时间戳的 LocalDateTime 序列化器
 *
 * @author 老五
 */
public class TimestampLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    public static final TimestampLocalDateTimeSerializer INSTANCE = new TimestampLocalDateTimeSerializer();

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String fieldName = gen.getOutputContext().getCurrentName();
        Class<?> clazz = gen.getOutputContext().getCurrentValue().getClass();
        Field field = ReflectUtil.getField(clazz, fieldName);
        // 情况一：有 JsonFormat 自定义注解，则使用它。https://github.com/YunaiV/ruoyi-vue-pro/pull/1019
        JsonFormat[] jsonFormats = field.getAnnotationsByType(JsonFormat.class);
        if (jsonFormats.length > 0) {
            String pattern = jsonFormats[0].pattern();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            gen.writeString(formatter.format(value));
            return;
        }

        // 情况二：默认将 LocalDateTime 对象，转换为 Long 时间戳
        gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

}
