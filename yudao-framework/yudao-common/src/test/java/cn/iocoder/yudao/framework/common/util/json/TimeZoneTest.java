package cn.iocoder.yudao.framework.common.util.json;


import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;

public class TimeZoneTest {

    public static void main(String[] args) throws Exception {
        test1();
        System.out.println("-----------分割线-----------");
        test2();
        System.out.println("-----------分割线-----------");
//        test3();

    }

    /**
     * 时区转换测试
     */
    public static void test1() {

        // 假设这是从数据库/API 获取的 UTC 时间（LocalDateTime 无时区）
        LocalDateTime utcLocalDateTime = LocalDateTime.parse("2025-06-01 08:00:00", NORM_DATETIME_FORMATTER);

        // Step 1: 将 LocalDateTime 转为 ZonedDateTime（附加 UTC 时区）
        ZonedDateTime utcZonedDateTime = utcLocalDateTime.atZone(ZoneId.of("UTC"));

        // Step 2: 转换为 UTC-8 时区（如北京时间）
        ZonedDateTime utcMinus8ZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.of("UTC-8"));

        // Step 3: 如果需要，转回 LocalDateTime（去掉时区信息）
        LocalDateTime resultLocalDateTime = utcMinus8ZonedDateTime.toLocalDateTime();

        System.out.println("原始 UTC 时间: " + utcLocalDateTime);
        System.out.println("转换为 UTC-8 时区: " + utcMinus8ZonedDateTime);
        System.out.println("去掉时区信息后: " + resultLocalDateTime);
    }
    /**
     * 时间戳的对比测试
     */
    private static void test2() {
        // 示例 LocalDateTime（无时区）
        LocalDateTime localDateTime = LocalDateTime.parse("2025-06-01 08:00:00", NORM_DATETIME_FORMATTER);

        // 方法 1：假设 LocalDateTime 是 UTC 时间
        long timestampUtc = localDateTime
                .atZone(ZoneId.of("UTC"))          // 附加 UTC 时区
                .toInstant()                        // 转为 Instant
                .toEpochMilli();                   // 转为毫秒时间戳
        System.out.println("UTC 时间戳: " + timestampUtc);

        // 方法 2：假设 LocalDateTime 是北京时间（UTC+8）
        long timestampShanghai = localDateTime
                .atZone(ZoneId.of("UTC+8")) // 附加北京时间时区
                .toInstant()
                .toEpochMilli();
        System.out.println("北京时间戳: " + timestampShanghai);

        System.out.println(timestampUtc - timestampShanghai);
        System.out.println((timestampUtc - timestampShanghai) / (1000 * 60 * 60));
    }

    /**
     * 测试自定义时间戳转换
     * @throws JsonProcessingException
     */
    private static void test3() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();


        //JSON 序列化
        javaTimeModule.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            public static final ZoneId FROM_ZONE_ID = ZoneId.of("UTC");

            @Override
            public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                //TODO动态获取
                ZoneId toZoneId = ZoneId.of("UTC-5");
                // 转换为 UTC 时间
                String utcTime = localDateTime
                        .atZone(FROM_ZONE_ID)
                        .withZoneSameInstant(toZoneId)
                        .toLocalDateTime()
                        .format(NORM_DATETIME_FORMATTER);
                jsonGenerator.writeString(utcTime);
            }
        });

        //JSON 反序列化
        javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {

            public static final ZoneId TO_ZONE_ID = ZoneId.of("UTC");

            @Override
            public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
                //TODO动态获取
                String valueAsString = jsonParser.getValueAsString();
                ZoneId formZoneId = ZoneId.of("UTC-5");
                return LocalDateTime.parse(valueAsString, NORM_DATETIME_FORMATTER)
                        .atZone(formZoneId)
                        .withZoneSameInstant(TO_ZONE_ID)
                        .toLocalDateTime();
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
