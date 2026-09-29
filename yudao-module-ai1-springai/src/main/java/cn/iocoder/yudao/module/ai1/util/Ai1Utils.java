package cn.iocoder.yudao.module.ai1.util;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.springframework.core.env.Environment;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.CONFIG_PLACEHOLDER_NOT_RESOLVED;

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
            throw exception(CONFIG_PLACEHOLDER_NOT_RESOLVED, value);
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
     * 解析供应商的请求附属 Header：JSON 数组，格式为 [{"key":"...","value":"..."}]
     *
     * @param headersJson JSON 字符串
     * @return Header 列表，每项包含 key、value；为空或格式错误时返回 null
     */
    public static List<Map<String, String>> parseHeaders(String headersJson) {
        if (StrUtil.isBlank(headersJson)) {
            return null;
        }
        List<Map<String, Object>> items = JsonUtils.parseObjectQuietly(headersJson, new TypeReference<List<Map<String, Object>>>() {});
        if (items == null) {
            return null;
        }
        List<Map<String, String>> headers = new ArrayList<>(items.size());
        for (Map<String, Object> item : items) {
            // 每项必须是对象，且 key 为标量
            if (item == null || !isSimpleValue(item.get("key"))) {
                return null;
            }
            headers.add(MapUtil.builder("key", MapUtil.getStr(item, "key"))
                    .put("value", isSimpleValue(item.get("value")) ? MapUtil.getStr(item, "value") : "").build());
        }
        return headers;
    }

    /**
     * 是否为 JSON 标量值：字符串、数字、布尔
     */
    private static boolean isSimpleValue(Object value) {
        return value != null && ClassUtil.isSimpleValueType(value.getClass());
    }

}
