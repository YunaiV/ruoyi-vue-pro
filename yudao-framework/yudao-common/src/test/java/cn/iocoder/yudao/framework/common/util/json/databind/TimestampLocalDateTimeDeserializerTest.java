package cn.iocoder.yudao.framework.common.util.json.databind;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link TimestampLocalDateTimeDeserializer} 的单元测试
 *
 * 覆盖全部在用格式（epoch 毫秒 / 常见字符串 / ISO / 纯日期），
 * 以及非法输入抛清晰错误（不再静默落 1970-01-01）。
 *
 * @author 芋道源码
 */
public class TimestampLocalDateTimeDeserializerTest {

    private static final ObjectMapper MAPPER;

    static {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDateTime.class, TimestampLocalDateTimeDeserializer.INSTANCE);
        mapper.registerModule(module);
        MAPPER = mapper;
    }

    private LocalDateTime parse(String json) throws Exception {
        return MAPPER.readValue(json, LocalDateTime.class);
    }

    @Test
    public void testEpochMilliNumber() throws Exception {
        // 1700000000000 = 2023-11-14T22:13:20Z（本地时区换算后）
        LocalDateTime expected = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(1700000000000L), java.time.ZoneId.systemDefault());
        assertEquals(expected, parse("1700000000000"));
    }

    @Test
    public void testEpochMilliString() throws Exception {
        LocalDateTime expected = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(1700000000000L), java.time.ZoneId.systemDefault());
        assertEquals(expected, parse("\"1700000000000\""));
    }

    @Test
    public void testSpaceFormat() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 8, 0, 0), parse("\"2026-09-11 08:00:00\""));
    }

    @Test
    public void testSpaceFormatWithMillis() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 8, 0, 0, 123000000), parse("\"2026-09-11 08:00:00.123\""));
    }

    @Test
    public void testIsoLocal() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 8, 0, 0), parse("\"2026-09-11T08:00:00\""));
    }

    @Test
    public void testIsoWithOffset() throws Exception {
        // 前端 Date 对象序列化：带 Z（UTC）。2026-09-11T00:00:00Z 换算到系统时区
        LocalDateTime expected = java.time.OffsetDateTime.parse("2026-09-11T00:00:00Z")
                .atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(expected, parse("\"2026-09-11T00:00:00Z\""));
    }

    @Test
    public void testIsoWithOffsetMillis() throws Exception {
        // JSON.stringify(Date) 实际产物：带毫秒 + Z（无 value-format 的 Date 对象控件提交格式）
        LocalDateTime expected = java.time.OffsetDateTime.parse("2026-09-11T00:00:00.000Z")
                .atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(expected, parse("\"2026-09-11T00:00:00.000Z\""));
    }

    @Test
    public void testIsoWithOffsetPlus() throws Exception {
        // 带 +08:00 时区偏移的形式
        LocalDateTime expected = java.time.OffsetDateTime.parse("2026-09-11T08:00:00.000+08:00")
                .atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(expected, parse("\"2026-09-11T08:00:00.000+08:00\""));
    }

    @Test
    public void testDateOnly() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 11, 0, 0, 0), parse("\"2026-09-11\""));
    }

    @Test
    public void testNullAndEmpty() throws Exception {
        assertNull(parse("null"));
        assertNull(parse("\"\""));
    }

    @Test
    public void testInvalidThrows() {
        // 非法输入抛清晰错误，不再静默落 1970
        assertThrows(Exception.class, () -> parse("\"not-a-date\""));
        assertThrows(Exception.class, () -> parse("\"2026/09/11\""));
    }

}
