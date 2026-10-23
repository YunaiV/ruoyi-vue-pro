package cn.iocoder.yudao.module.ai1.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import org.springframework.core.env.Environment;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.CONFIG_PLACEHOLDER_NOT_RESOLVED;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.PROVIDER_HEADERS_INVALID;

/**
 * AI1 工具类
 *
 * @author 芋道源码
 */
public class Ai1Utils {

    /**
     * 解析 DB 等动态配置里的 Spring 占位符，例如 ${OPENAI_API_KEY}
     *
     * 库里只保存占位符，真实密钥留在环境变量或配置文件；仅在发起调用前解析，响应里仍返回占位符本身
     *
     * @param value 待解析的配置值
     * @return 解析后的配置值；不含占位符时原样返回
     */
    public static String resolveSpringPlaceholders(String value) {
        if (StrUtil.isBlank(value) || !StrUtil.contains(value, "${")) {
            return value;
        }
        try {
            return SpringUtil.getBean(Environment.class).resolveRequiredPlaceholders(value);
        } catch (IllegalArgumentException ex) {
            throw exception(CONFIG_PLACEHOLDER_NOT_RESOLVED, "动态配置项");
        }
    }

    /**
     * 解析 Map 中每个值里的 Spring 占位符，键保持不变
     *
     * @param values 待解析的值
     * @return 解析后的 Map；入参为 null 时返回 null
     */
    public static Map<String, String> resolveSpringPlaceholders(Map<String, String> values) {
        if (values == null) {
            return null;
        }
        Map<String, String> resolved = new LinkedHashMap<>();
        values.forEach((key, value) -> resolved.put(key, resolveSpringPlaceholders(value)));
        return resolved;
    }

    /**
     * 规范化请求 Header，保留输入顺序；不解析环境占位符
     */
    public static Map<String, String> normalizeHeaders(Map<String, String> values) {
        if (values == null) {
            return null;
        }
        Map<String, String> headers = new LinkedHashMap<>();
        Set<String> names = new HashSet<>();
        values.forEach((key, value) -> {
            if (StrUtil.isBlank(key) || value == null) {
                throw exception(PROVIDER_HEADERS_INVALID);
            }
            String name = key.trim();
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                throw exception(PROVIDER_HEADERS_INVALID);
            }
            headers.put(name, value);
        });
        if (JsonUtils.toJsonString(headers).length() > 2000) {
            throw exception(PROVIDER_HEADERS_INVALID);
        }
        return headers;
    }

    public static boolean isHeadersValid(Map<String, String> headers) {
        try {
            normalizeHeaders(headers);
            return true;
        } catch (ServiceException ex) {
            return false;
        }
    }

    /**
     * 规范化模型生成的长期记忆 Markdown：修正标题、列表标记，并保证维度标题与内容不在同一行
     *
     * 只按长期记忆的固定维度标题拆行，不拆开 C#、#123、URL 锚点，不把中文破折号视为列表标记
     *
     * @param content  模型输出的记忆
     * @param sections 记忆维度标题
     * @return 规范化后的记忆；为空时原样返回
     */
    public static String normalizeMemoryMarkdown(String content, Collection<String> sections) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        String text = content.replace("\r\n", "\n").replace("\r", "\n").trim();
        // 1. 行内出现的维度标题另起一行：…文字## 用户画像 → …文字\n## 用户画像
        text = text.replaceAll("([^#\\n])(#{1,6}[ \\t]*(?:" + String.join("|", sections) + "))", "$1\n$2");
        // 2. 标题标记后补空格：#标题 → # 标题（# 后为非 #、非空白，避免把 ## 拆成 # #）
        text = text.replaceAll("(?m)^(#{1,6})(?=[^#\\s])", "$1 ");
        // 3. 维度标题与紧随其后的内容拆行：## 偏好习惯-xxx → ## 偏好习惯\n-xxx
        for (String section : sections) {
            text = text.replaceAll("(?m)(#{1,6} " + section + ")[ \\t]*(?=\\S)", "$1\n");
        }
        // 4. 行内列表项另起一行，仅当前一字符为非 ASCII：…口吻-开场 → …口吻\n- 开场（不把中文破折号 —、– 视为列表标记）
        text = text.replaceAll("([^\\x00-\\x7F])[-*+\\u2022][ \\t]*(?=\\S)", "$1\n- ");
        // 5. 行首列表标记后补空格：-xxx → - xxx（同样不处理中文破折号）
        text = text.replaceAll("(?m)^([-*+\\u2022])(?=\\S)", "$1 ");
        // 6. 不同维度之间空一行
        text = text.replaceAll("(?m)^(#{1,6} )", "\n$1");
        // 7. 合并多余空行
        text = text.replaceAll("\\n{3,}", "\n\n");
        return text.trim();
    }

    /**
     * 去除首尾空白后按字符数截断，不拆开 UTF-16 代理对
     *
     * @param content   内容
     * @param maxLength 最大长度（字符），需大于 0
     * @return 截断后的内容；为空时原样返回
     */
    public static String limitLength(String content, int maxLength) {
        if (StrUtil.isBlank(content)) {
            return content;
        }
        String text = content.trim();
        if (text.length() <= maxLength) {
            return text;
        }
        int end = Character.isHighSurrogate(text.charAt(maxLength - 1)) ? maxLength - 1 : maxLength;
        return text.substring(0, end);
    }

}
