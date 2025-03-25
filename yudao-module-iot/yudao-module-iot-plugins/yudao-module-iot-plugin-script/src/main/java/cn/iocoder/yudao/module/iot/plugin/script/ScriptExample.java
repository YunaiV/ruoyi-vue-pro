package cn.iocoder.yudao.module.iot.plugin.script;

import cn.iocoder.yudao.module.iot.plugin.script.context.PluginScriptContext;
import cn.iocoder.yudao.module.iot.plugin.script.service.ScriptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// TODO @haohao：写到单测类里；
/**
 * 脚本使用示例类
 */
@Component
@Slf4j
public class ScriptExample {

    @Autowired
    private ScriptService scriptService;

    /**
     * 示例：执行简单的JavaScript脚本
     */
    public void executeSimpleScript() {
        String script = "var result = a + b; result;";

        Map<String, Object> params = new HashMap<>();
        params.put("a", 10);
        params.put("b", 20);

        Object result = scriptService.executeJavaScript(script, params);
        log.info("脚本执行结果: {}", result);
    }

    /**
     * 示例：使用脚本处理设备数据
     *
     * @param deviceId 设备ID
     * @param payload  设备原始数据
     * @return 处理后的数据
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> processDeviceData(String deviceId, String payload) {
        // 设备数据处理脚本
        String script = "function process() {\n" +
                "    var data = JSON.parse(payload);\n" +
                "    var result = {};\n" +
                "    // 提取温度信息\n" +
                "    if (data.temp) {\n" +
                "        result.temperature = data.temp;\n" +
                "    }\n" +
                "    // 提取湿度信息\n" +
                "    if (data.hum) {\n" +
                "        result.humidity = data.hum;\n" +
                "    }\n" +
                "    // 计算额外信息\n" +
                "    if (data.temp && data.temp > 30) {\n" +
                "        result.alert = true;\n" +
                "        result.alertMessage = '温度过高警告';\n" +
                "    }\n" +
                "    return result;\n" +
                "}\n" +
                "process();";

        // 创建脚本上下文
        PluginScriptContext context = new PluginScriptContext();
        context.withDeviceContext(deviceId, null);
        context.withParameter("payload", payload);

        try {
            Object result = scriptService.executeJavaScript(script, context);
            if (result != null) {
                // 处理结果
                log.info("设备数据处理结果: {}", result);

                // 安全地将结果转换为Map
                if (result instanceof Map) {
                    return (Map<String, Object>) result;
                } else {
                    log.warn("脚本返回结果类型不是Map: {}", result.getClass().getName());
                }
            }
        } catch (Exception e) {
            log.error("处理设备数据失败: {}", e.getMessage());
        }

        return new HashMap<>();
    }

    /**
     * 示例：生成设备命令
     *
     * @param deviceId 设备ID
     * @param command  命令名称
     * @param params   命令参数
     * @return 格式化的命令字符串
     */
    public String generateDeviceCommand(String deviceId, String command, Map<String, Object> params) {
        // 命令生成脚本
        String script = "function generateCommand(cmd, params) {\n" +
                "    var result = { 'cmd': cmd };\n" +
                "    if (params) {\n" +
                "        result.params = params;\n" +
                "    }\n" +
                "    result.timestamp = new Date().getTime();\n" +
                "    result.deviceId = deviceId;\n" +
                "    return JSON.stringify(result);\n" +
                "}\n" +
                "generateCommand(command, commandParams);";

        // 创建脚本上下文
        PluginScriptContext context = new PluginScriptContext();
        context.setParameter("deviceId", deviceId);
        context.setParameter("command", command);
        context.setParameter("commandParams", params);

        try {
            Object result = scriptService.executeJavaScript(script, context);
            if (result instanceof String) {
                return (String) result;
            } else if (result != null) {
                log.warn("脚本返回结果类型不是String: {}", result.getClass().getName());
            }
        } catch (Exception e) {
            log.error("生成设备命令失败: {}", e.getMessage());
        }

        return null;
    }
}