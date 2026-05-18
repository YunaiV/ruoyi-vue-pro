package cn.iocoder.yudao.module.im.util;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.im.service.websocket.dto.message.QuoteMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IM 消息内容工具类单元测试
 *
 * @author 芋道源码
 */
public class ImMessageUtilsTest {

    // ========== parseQuoteMessageId ==========

    @Test
    public void testParseQuoteMessageId_valid() {
        // 准备：content 含合法 quote 字段
        String content = "{\"text\":\"你好\",\"quote\":{\"messageId\":1001,\"senderId\":2,\"type\":1}}";

        // 调用 + 断言
        assertEquals(1001L, ImMessageUtils.parseQuoteMessageId(content));
    }

    @Test
    public void testParseQuoteMessageId_invalidJson() {
        // 准备：content 非合法 JSON
        // 调用 + 断言：解析失败返回 null
        assertNull(ImMessageUtils.parseQuoteMessageId("not a json"));
    }

    @Test
    public void testParseQuoteMessageId_noQuoteField() {
        // 准备：content 无 quote 字段
        String content = "{\"text\":\"你好\"}";

        // 调用 + 断言
        assertNull(ImMessageUtils.parseQuoteMessageId(content));
    }

    @Test
    public void testParseQuoteMessageId_quoteIsNotMap() {
        // 准备：quote 字段不是对象
        String content = "{\"text\":\"你好\",\"quote\":\"oops\"}";

        // 调用 + 断言
        assertNull(ImMessageUtils.parseQuoteMessageId(content));
    }

    @Test
    public void testParseQuoteMessageId_messageIdMissing() {
        // 准备：quote 对象内无 messageId
        String content = "{\"quote\":{\"senderId\":2}}";

        // 调用 + 断言
        assertNull(ImMessageUtils.parseQuoteMessageId(content));
    }

    // ========== appendQuote ==========

    @Test
    public void testAppendQuote_existingContent() {
        // 准备：已有 text 字段的 content
        String content = "{\"text\":\"你好\"}";
        QuoteMessage quote = new QuoteMessage().setMessageId(1001L).setSenderId(2L).setType(1).setContent("{}");

        // 调用
        String result = ImMessageUtils.appendQuote(content, quote);

        // 断言：保留原字段，注入 quote
        Map<String, Object> map = JsonUtils.parseMap(result);
        assertEquals("你好", map.get("text"));
        assertTrue(map.get("quote") instanceof Map);
        assertEquals(1001, ((Map<?, ?>) map.get("quote")).get("messageId"));
    }

    @Test
    public void testAppendQuote_blankContent() {
        // 准备：content 为空字符串，无法解析为 map
        QuoteMessage quote = new QuoteMessage().setMessageId(1001L);

        // 调用
        String result = ImMessageUtils.appendQuote("", quote);

        // 断言：内部新建 map，仅含 quote
        Map<String, Object> map = JsonUtils.parseMap(result);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("quote"));
    }

    @Test
    public void testAppendQuote_overwriteExistingQuote() {
        // 准备：content 已经有一个旧 quote，期望被新 quote 覆盖
        String content = "{\"text\":\"hi\",\"quote\":{\"messageId\":1}}";
        QuoteMessage quote = new QuoteMessage().setMessageId(9999L).setSenderId(2L).setType(1);

        // 调用
        String result = ImMessageUtils.appendQuote(content, quote);

        // 断言：quote.messageId 被覆盖为新值
        Map<String, Object> map = JsonUtils.parseMap(result);
        assertEquals(9999, ((Map<?, ?>) map.get("quote")).get("messageId"));
    }

    // ========== removeQuote ==========

    @Test
    public void testRemoveQuote_blankContent() {
        // 调用 + 断言：空内容原样返回
        assertEquals("", ImMessageUtils.removeQuote(""));
        assertNull(ImMessageUtils.removeQuote(null));
    }

    @Test
    public void testRemoveQuote_noQuoteField() {
        // 准备：content 字符串里没有 quote 关键字，提前返回原值
        String content = "{\"text\":\"你好\"}";

        // 调用 + 断言：同一引用直接返回
        assertEquals(content, ImMessageUtils.removeQuote(content));
    }

    @Test
    public void testRemoveQuote_withQuote() {
        // 准备：含 quote 的 content
        String content = "{\"text\":\"你好\",\"quote\":{\"messageId\":1}}";

        // 调用
        String result = ImMessageUtils.removeQuote(content);

        // 断言：quote 被移除，保留其它字段
        Map<String, Object> map = JsonUtils.parseMap(result);
        assertFalse(map.containsKey("quote"));
        assertEquals("你好", map.get("text"));
    }

    @Test
    public void testRemoveQuote_invalidJsonContainingQuoteToken() {
        // 准备：字符串包含 quote 字面量但解析失败
        String content = "not-a-json-but-has-\"quote\"-token";

        // 调用 + 断言：解析失败返回原值
        assertEquals(content, ImMessageUtils.removeQuote(content));
    }

    // ========== buildQuote ==========

    @Test
    public void testBuildQuote_stripsNestedQuote() {
        // 准备：被引用消息本身也含有 quote 字段；防止嵌套
        String originalContent = "{\"text\":\"被引用\",\"quote\":{\"messageId\":777}}";

        // 调用
        QuoteMessage quote = ImMessageUtils.buildQuote(1001L, 2L, 1, originalContent);

        // 断言：基础字段透传，content 已被剥离 quote
        assertEquals(1001L, quote.getMessageId());
        assertEquals(2L, quote.getSenderId());
        assertEquals(1, quote.getType());
        Map<String, Object> contentMap = JsonUtils.parseMap(quote.getContent());
        assertFalse(contentMap.containsKey("quote"));
        assertEquals("被引用", contentMap.get("text"));
    }

    @Test
    public void testBuildQuote_originalWithoutQuote() {
        // 准备：被引用消息原本就没 quote 字段
        String originalContent = "{\"text\":\"hi\"}";

        // 调用
        QuoteMessage quote = ImMessageUtils.buildQuote(1001L, 2L, 1, originalContent);

        // 断言：content 原样保留
        assertEquals(originalContent, quote.getContent());
    }

}
