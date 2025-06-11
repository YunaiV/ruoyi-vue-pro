package cn.iocoder.yudao.framework.common.util.json;


import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;

public class TimeZoneTest {

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {

            public static final ZoneId TO_ZONE_ID= ZoneId.of("UTC");
            @Override
            public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
                //TODO动态获取
                String valueAsString = jsonParser.getValueAsString();
                ZoneId formZoneId = ZoneId.of("UTC+8");
                return LocalDateTime.parse(valueAsString, NORM_DATETIME_FORMATTER)
                        .atZone(formZoneId)
                        .withZoneSameInstant(TO_ZONE_ID)
                        .toLocalDateTime();
            }
        });

        javaTimeModule.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            public static final ZoneId FROM_ZONE_ID= ZoneId.of("UTC");
            @Override
            public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                //TODO动态获取
                ZoneId toZoneId = ZoneId.of("UTC+8");
                // 转换为 UTC 时间
                String utcTime = localDateTime
                        .atZone(FROM_ZONE_ID)
                        .withZoneSameInstant(toZoneId)
                        .format(NORM_DATETIME_FORMATTER);
                jsonGenerator.writeString(utcTime);
            }
        });

        objectMapper.registerModule(javaTimeModule);
        String json = "{\"createTime\":\"2025-06-01 08:00:00\"}";
        System.out.println("1、请求原始参数：\n" + json);
        MyObject obj = objectMapper.readValue(json, MyObject.class);
        System.out.println("2、反序列化后的JAVA对象：\n" + obj);
        System.out.println("3、正序列化后的json字符串：\n" + objectMapper.writeValueAsString(obj));

    }

    static class MyObject {
        private LocalDateTime createTime;
        // getter/setter 省略

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        @Override
        public String toString() {
            return "MyObject{" +
                    "createDate=" + createTime.format(NORM_DATETIME_FORMATTER) +
                    '}';
        }
    }
}
