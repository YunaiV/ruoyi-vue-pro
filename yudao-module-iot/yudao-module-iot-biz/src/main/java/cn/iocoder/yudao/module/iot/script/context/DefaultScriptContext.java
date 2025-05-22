package cn.iocoder.yudao.module.iot.script.context;

import cn.hutool.core.map.MapUtil;

import java.util.Map;

/**
 * 默认脚本上下文实现
 */
public class DefaultScriptContext implements ScriptContext {

    /**
     * 上下文参数
     */
    private final Map<String, Object> parameters = MapUtil.newHashMap();

    /**
     * 上下文函数
     */
    private final Map<String, Object> functions = MapUtil.newHashMap();

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
}