package cn.iocoder.yudao.module.iot.plugin.script.context;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件脚本上下文，提供插件执行脚本的上下文环境
 */
public class PluginScriptContext implements ScriptContext {

    /**
     * 上下文参数
     */
    @Getter
    private final Map<String, Object> parameters = new HashMap<>();

    /**
     * 上下文函数
     */
    @Getter
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

    // TODO @haohao：setParameters？这样的话，with 都是一些比较个性的参数
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
     * @param deviceId   设备 ID
     * @param deviceData 设备数据
     * @return 当前上下文对象
     */
    // TODO @haohao：是不是加个 (String productKey, String deviceName, Map<String, Object> deviceData) {
    public PluginScriptContext withDeviceContext(String deviceId, Map<String, Object> deviceData) {
        // TODO @haohao：deviceId 一般是分开，还是合并哈？
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

    // TODO @haohao：setParameter 可以融合哈？
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