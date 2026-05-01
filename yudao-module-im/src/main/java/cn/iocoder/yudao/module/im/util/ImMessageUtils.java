package cn.iocoder.yudao.module.im.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.im.service.websocket.dto.message.QuoteMessage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * IM 消息内容相关工具类
 */
public class ImMessageUtils {

    /**
     * 从 content 解析客户端写入的 quote.messageId
     *
     * @param content 原 content(JSON)
     * @return messageId，不存在 / 非法返回 null
     */
    public static Long parseQuoteMessageId(String content) {
        Map<String, Object> map = JsonUtils.parseMap(content);
        if (map == null) {
            return null;
        }
        Object quoteRaw = map.get(QuoteMessage.FIELD_NAME);
        if (!(quoteRaw instanceof Map)) {
            return null;
        }
        return Convert.toLong(((Map<?, ?>) quoteRaw).get("messageId"));
    }

    /**
     * 把 quote 写入 content 的 quote 字段
     *
     * @param content 原 content(JSON)
     * @param quote QuoteMessage 对象
     * @return 注入 quote 后的 content(JSON)
     */
    public static String appendQuote(String content, QuoteMessage quote) {
        Map<String, Object> map = JsonUtils.parseMap(content);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        map.put(QuoteMessage.FIELD_NAME, quote);
        return JsonUtils.toJsonString(map);
    }

    /**
     * 移除 content 里的 quote 字段
     *
     * @param content 原 content(JSON)
     * @return remove quote 后的 content(JSON)
     */
    public static String removeQuote(String content) {
        if (StrUtil.isBlank(content) || !StrUtil.contains(content, "\"" + QuoteMessage.FIELD_NAME + "\"")) {
            return content;
        }
        Map<String, Object> map = JsonUtils.parseMap(content);
        if (map == null || !map.containsKey(QuoteMessage.FIELD_NAME)) {
            return content;
        }
        map.remove(QuoteMessage.FIELD_NAME);
        return JsonUtils.toJsonString(map);
    }

    /**
     * 构建 QuoteMessage 对象
     *
     * @param messageId 被引用消息编号
     * @param senderId 被引用消息发送人编号
     * @param type 被引用消息类型
     * @param originalContent 被引用消息原 content(JSON)，服务端会 removeQuote 防止嵌套
     * @return QuoteMessage 对象
     */
    public static QuoteMessage buildQuote(Long messageId, Long senderId, Integer type, String originalContent) {
        return new QuoteMessage().setMessageId(messageId).setSenderId(senderId).setType(type)
                .setContent(removeQuote(originalContent));
    }

}
