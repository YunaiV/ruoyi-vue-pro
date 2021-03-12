package cn.iocoder.dashboard.framework.jackson.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Long类型序列化规则
 * <p>
 * 数值超过2^53-1，在JS会出现精度丢失问题，因此Long自动序列化为字符串类型
 */
public class LongSerializer extends JsonSerializer<Long> {

    private static final LongSerializer LONG_SERIALIZER = new LongSerializer();

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.toString());
    }

    public static LongSerializer getInstance() {
        return LONG_SERIALIZER;
    }
}
