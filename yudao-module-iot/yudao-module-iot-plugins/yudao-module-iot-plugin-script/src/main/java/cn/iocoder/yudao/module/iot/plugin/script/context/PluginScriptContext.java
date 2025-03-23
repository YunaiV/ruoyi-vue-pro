package cn.iocoder.yudao.module.iot.plugin.script.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件脚本上下文，提供插件执行脚本的上下文环境
 */
public class PluginScriptContext implements ScriptContext {

    /**
     * 上下文参数
     */
    private final Map<String, Object> parameters = new HashMap<>();

    /**
     * 上下文函数
     */
    private final Map<String, Object> functions = new HashMap<>();

    /**
     * 日志函数接口
     */
    public interface LogFunction {
        void log(String message);
    }

    /**
     * 构建插件脚本上下文
     */
    public PluginScriptContext() {
        // 初始化上下文，注册一些基础函数
        LogFunction logFunction = message -> System.out.println("[Plugin Script] " + message);
        registerFunction("log", logFunction);
    }

    /**
     * 构建插件脚本上下文
     *
     * @param parameters 初始参数
     */
    public PluginScriptContext(Map<String, Object> parameters) {
        this();
        if (parameters != null) {
            this.parameters.putAll(parameters);
        }
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Map<String, Object> getFunctions() {
        return functions;
    }

    @Override
    public void setParameter(String key, Object value) {
        parameters.put(key, value);
    }

    @Override
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    @Override
    public void registerFunction(String name, Object function) {
        functions.put(name, function);
    }

    /**
     * 批量设置参数
     *
     * @param params 参数Map
     * @return 当前上下文对象
     */
    public PluginScriptContext withParameters(Map<String, Object> params) {
        if (params != null) {
            parameters.putAll(params);
        }
        return this;
    }

    /**
     * 添加设备相关的上下文参数
     *
     * @param deviceId   设备ID
     * @param deviceData 设备数据
     * @return 当前上下文对象
     */
    public PluginScriptContext withDeviceContext(String deviceId, Map<String, Object> deviceData) {
        parameters.put("deviceId", deviceId);
        parameters.put("deviceData", deviceData);
        return this;
    }

    /**
     * 添加消息相关的上下文参数
     *
     * @param topic   消息主题
     * @param payload 消息内容
     * @return 当前上下文对象
     */
    public PluginScriptContext withMessageContext(String topic, Object payload) {
        parameters.put("topic", topic);
        parameters.put("payload", payload);
        return this;
    }

    /**
     * 设置单个参数
     *
     * @param key   参数名
     * @param value 参数值
     * @return 当前上下文对象
     */
    public PluginScriptContext withParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }
}