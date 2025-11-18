package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
        try {
            Field field = gen.getOutputContext().getCurrentValue().getClass().getDeclaredField(fieldName);
            JsonFormat[] jsonFormats = field.getAnnotationsByType(JsonFormat.class);
            if(jsonFormats.length > 0){
                String pattern = jsonFormats[0].pattern();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                gen.writeString(formatter.format(value));
            }else{
                // 将 LocalDateTime 对象，转换为 Long 时间戳
                gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
