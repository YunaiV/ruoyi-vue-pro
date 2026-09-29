package cn.iocoder.yudao.module.ai1.framework.ai.core.config;

import cn.hutool.core.util.StrUtil;
import org.springframework.core.env.Environment;

import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai1.enums.Ai1ErrorCodeConstants.CONFIG_PLACEHOLDER_NOT_RESOLVED;

// TODO @AI：叫这个名字，有点怪；要不抽成 utils？/Users/yunai/Java/ruoyi-vue-pro-sb4/yudao-module-ai/src/main/java/cn/iocoder/yudao/module/ai/util/AiUtils.java 类似这个；
/**
 * 解析数据库配置中的 Spring 占位符，例如 ${OPENAI_API_KEY}
 *
 * 库里只保存占位符，真实密钥留在环境变量或配置文件；仅在发起调用前解析，响应里仍返回占位符本身
 *
 * @author 芋道源码
 */
public final class Ai1ConfigPlaceholders {

    private Ai1ConfigPlaceholders() {
    }

    /**
     * 解析配置值。不含占位符时原样返回
     *
     * @param environment 环境
     * @param value       数据库中的配置值
     * @return 解析后的配置值
     */
    public static String resolve(Environment environment, String value) {
        if (StrUtil.isBlank(value) || !StrUtil.contains(value, "${")) {
            return value;
        }
        try {
            return environment.resolveRequiredPlaceholders(value);
        } catch (IllegalArgumentException ex) {
            throw exception(CONFIG_PLACEHOLDER_NOT_RESOLVED, value);
        }
    }

    /**
     * 解析 Map 中的每个值，键保持不变
     *
     * @param environment 环境
     * @param values      待解析的值
     * @return 解析后的 Map；入参为 null 时返回 null
     */
    public static Map<String, String> resolveValues(Environment environment, Map<String, String> values) {
        if (values == null) {
            return null;
        }
        Map<String, String> resolved = new LinkedHashMap<>();
        values.forEach((key, value) -> resolved.put(key, resolve(environment, value)));
        return resolved;
    }

}
