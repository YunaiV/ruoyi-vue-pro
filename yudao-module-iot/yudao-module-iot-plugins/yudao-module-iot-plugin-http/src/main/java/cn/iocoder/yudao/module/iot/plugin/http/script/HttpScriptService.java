package cn.iocoder.yudao.module.iot.plugin.http.script;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.plugin.script.context.PluginScriptContext;
import cn.iocoder.yudao.module.iot.plugin.script.service.ScriptService;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP 协议脚本处理服务
 * 用于管理和执行设备数据解析脚本
 *
 * @author haohao
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HttpScriptService {

    private final ScriptService scriptService;

    // TODO @haohao：后续可以考虑放到 guava 缓存
    // TODO @haohao：可能要抽一个 script factory 之类的？方便多个 emqx、http 之类复用？
    /**
     * 脚本缓存，按产品 Key 缓存脚本内容
     */
    private final Map<String, String> scriptCache = new ConcurrentHashMap<>();

    /**
     * 解析设备属性数据
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @param payload    设备上报的原始数据
     * @return 解析后的属性数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parsePropertyData(String productKey, String deviceName, JsonObject payload) {
        // 如果没有脚本，直接返回原始数据
        String script = getScriptByProductKey(productKey);
        if (StrUtil.isBlank(script)) {
            if (payload != null && payload.containsKey("params")) {
                return payload.getJsonObject("params").getMap();
            }
            return new HashMap<>();
        }

        try {
            // 创建脚本上下文
            PluginScriptContext context = new PluginScriptContext();
            context.withDeviceContext(productKey + ":" + deviceName, null);
            context.withParameter("payload", payload.toString());
            context.withParameter("method", "property");

            // 执行脚本
            Object result = scriptService.executeJavaScript(script, context);
            log.debug("[parsePropertyData][产品:{} 设备:{} 原始数据:{} 解析结果:{}]",
                    productKey, deviceName, payload, result);

            // 处理结果
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            } else if (result instanceof String) {
                try {
                    return new JsonObject((String) result).getMap();
                } catch (Exception e) {
                    log.warn("[parsePropertyData][脚本返回的字符串不是有效的JSON] result:{}", result);
                }
            }
        } catch (Exception e) {
            log.error("[parsePropertyData][执行脚本解析属性数据异常] productKey:{} deviceName:{}",
                    productKey, deviceName, e);
        }

        // TODO @芋艿：解析失败，是不是不能返回空？！
        // 解析失败，返回空数据
        return new HashMap<>();
    }

    /**
     * 解析设备事件数据
     *
     * @param productKey 产品Key
     * @param deviceName 设备名称
     * @param identifier 事件标识符
     * @param payload    设备上报的原始数据
     * @return 解析后的事件数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseEventData(String productKey, String deviceName, String identifier,
                                              JsonObject payload) {
        // 如果没有脚本，直接返回原始数据
        String script = getScriptByProductKey(productKey);
        if (StrUtil.isBlank(script)) {
            if (payload != null && payload.containsKey("params")) {
                return payload.getJsonObject("params").getMap();
            }
            return new HashMap<>();
        }

        try {
            // 创建脚本上下文
            PluginScriptContext context = new PluginScriptContext();
            context.withDeviceContext(productKey + ":" + deviceName, null);
            context.withParameter("payload", payload.toString());
            context.withParameter("method", "event");
            context.withParameter("identifier", identifier);

            // 执行脚本
            Object result = scriptService.executeJavaScript(script, context);
            log.debug("[parseEventData][产品:{} 设备:{} 事件:{} 原始数据:{} 解析结果:{}]",
                    productKey, deviceName, identifier, payload, result);

            // 处理结果
            // TODO @haohao：处理结果，可以复用么？
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            } else if (result instanceof String) {
                try {
                    return new JsonObject((String) result).getMap();
                } catch (Exception e) {
                    log.warn("[parseEventData][脚本返回的字符串不是有效的 JSON] result:{}", result);
                }
            }
        } catch (Exception e) {
            log.error("[parseEventData][执行脚本解析事件数据异常] productKey:{} deviceName:{} identifier:{}",
                    productKey, deviceName, identifier, e);
        }

        // TODO @芋艿：解析失败，是不是不能返回空？！
        // 解析失败，返回空数据
        return new HashMap<>();
    }

    /**
     * 根据产品Key获取脚本
     *
     * @param productKey 产品Key
     * @return 脚本内容
     */
    private String getScriptByProductKey(String productKey) {
        // 从缓存中获取脚本
        String script = scriptCache.get(productKey);
        if (script != null) {
            return script;
        }

        // TODO: 实际应用中，这里应从数据库或配置中心获取产品对应的脚本
        // 此处仅为示例，提供一个默认脚本
        if ("example_product".equals(productKey)) {
            script = "/**\n" +
                    " * 设备数据解析脚本示例\n" +
                    " * @param payload 设备上报的原始数据\n" +
                    " * @param method 方法类型：property(属性)或event(事件)\n" +
                    " * @param identifier 事件标识符（仅当method为event时有值）\n" +
                    " * @return 解析后的数据\n" +
                    " */\n" +
                    "function parse() {\n" +
                    "    // 解析JSON数据\n" +
                    "    var data = JSON.parse(payload);\n" +
                    "    var result = {};\n" +
                    "    \n" +
                    "    // 根据方法类型处理\n" +
                    "    if (method === 'property') {\n" +
                    "        // 属性数据解析\n" +
                    "        if (data.params) {\n" +
                    "            // 直接返回params中的数据\n" +
                    "            return data.params;\n" +
                    "        }\n" +
                    "    } else if (method === 'event') {\n" +
                    "        // 事件数据解析\n" +
                    "        if (data.params) {\n" +
                    "            return data.params;\n" +
                    "        }\n" +
                    "    }\n" +
                    "    \n" +
                    "    return result;\n" +
                    "}\n" +
                    "\n" +
                    "// 执行解析\n" +
                    "parse();";

            // 缓存脚本
            scriptCache.put(productKey, script);
        }

        return script;
    }

    /**
     * 设置产品解析脚本
     *
     * @param productKey 产品 Key
     * @param script     脚本内容
     */
    public void setScript(String productKey, String script) {
        // TODO @haohao：if return 会好点哈
        if (StrUtil.isNotBlank(productKey) && StrUtil.isNotBlank(script)) {
            // 验证脚本是否有效
            if (scriptService.validateScript("js", script)) {
                scriptCache.put(productKey, script);
                log.info("[setScript][设置产品:{}的解析脚本成功]", productKey);
            } else {
                log.warn("[setScript][脚本验证失败，不更新缓存] productKey:{}", productKey);
            }
        }
    }

    /**
     * 清除产品解析脚本
     *
     * @param productKey 产品 Key
     */
    public void clearScript(String productKey) {
        if (StrUtil.isBlank(productKey)) {
           return;
        }
        scriptCache.remove(productKey);
        log.info("[clearScript][清除产品({})的解析脚本]", productKey);
    }

    /**
     * 清除所有脚本缓存
     */
    public void clearAllScripts() {
        scriptCache.clear();
        log.info("[clearAllScripts][清除所有脚本缓存]");
    }

}