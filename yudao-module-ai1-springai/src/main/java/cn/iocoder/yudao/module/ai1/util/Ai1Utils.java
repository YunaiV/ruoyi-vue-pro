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

}
