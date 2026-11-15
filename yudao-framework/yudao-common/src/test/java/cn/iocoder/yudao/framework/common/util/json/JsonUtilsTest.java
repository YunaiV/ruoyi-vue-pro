package cn.iocoder.yudao.framework.common.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JsonUtils} 的单元测试
 *
 * @author 芋道源码
 */
public class JsonUtilsTest {

    @Test
    public void testParseTreeQuietly() {
        // 调用，并断言：解析成功
        JsonNode array = JsonUtils.parseTreeQuietly("[\"a\", 1]");
        assertNotNull(array);
        assertTrue(array.isArray());
        assertEquals("a", array.get(0).asText());
        JsonNode object = JsonUtils.parseTreeQuietly(" {\"name\": \"芋道\"} ");
        assertNotNull(object);
        assertEquals("芋道", object.get("name").asText());
        // 调用，并断言：为空时返回 null
        assertNull(JsonUtils.parseTreeQuietly(null));
        assertNull(JsonUtils.parseTreeQuietly(""));
        // 调用，并断言：格式不正确时返回 null，不抛出异常
        assertNull(JsonUtils.parseTreeQuietly("这是几个标题：标题一"));
        assertNull(JsonUtils.parseTreeQuietly("[\"a\""));
    }

}
