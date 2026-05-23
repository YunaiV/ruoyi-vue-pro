package cn.iocoder.yudao.framework.common.util.json.databind;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JacksonStdImpl;
import tools.jackson.databind.ser.std.StdScalarSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Long 序列化规则
 *
 * 会将超长 long 值转换为 string，解决前端 JavaScript 最大安全整数是 2^53-1 的问题
 *
 * @author 星语
 */
@JacksonStdImpl
public class NumberSerializer extends StdScalarSerializer<Number> {

    private static final long MAX_SAFE_INTEGER = 9007199254740991L;
    private static final long MIN_SAFE_INTEGER = -9007199254740991L;

    public static final NumberSerializer INSTANCE = new NumberSerializer(Number.class);

    @SuppressWarnings("unchecked")
    public NumberSerializer(Class<? extends Number> rawType) {
        super((Class<Number>) rawType);
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        // 超出范围 序列化位字符串
        if (value.longValue() > MIN_SAFE_INTEGER && value.longValue() < MAX_SAFE_INTEGER) {
            writeNumber(value, gen);
        } else {
            gen.writeString(value.toString());
        }
    }

    private static void writeNumber(Number value, JsonGenerator gen) throws JacksonException {
        if (value instanceof BigDecimal decimal) {
            gen.writeNumber(decimal);
        } else if (value instanceof BigInteger integer) {
            gen.writeNumber(integer);
        } else if (value instanceof Double doubleValue) {
            gen.writeNumber(doubleValue);
        } else if (value instanceof Float floatValue) {
            gen.writeNumber(floatValue);
        } else if (value instanceof Long longValue) {
            gen.writeNumber(longValue);
        } else {
            gen.writeNumber(value.intValue());
        }
    }
}
