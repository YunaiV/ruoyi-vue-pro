package cn.iocoder.yudao.module.iot.plugin.script.context;

import java.util.Map;

/**
 * 脚本上下文接口，定义脚本执行所需的上下文环境
 */
public interface ScriptContext {

    /**
     * 获取上下文参数
     *
     * @return 上下文参数
     */
    Map<String, Object> getParameters();

    /**
     * 获取上下文函数
     *
     * @return 上下文函数
     */
    Map<String, Object> getFunctions();

    /**
     * 设置上下文参数
     *
     * @param key   参数名
     * @param value 参数值
     */
    void setParameter(String key, Object value);

    /**
     * 获取上下文参数
     *
     * @param key 参数名
     * @return 参数值
     */
    Object getParameter(String key);

    /**
     * 注册函数
     *
     * @param name     函数名称
     * @param function 函数对象
     */
    void registerFunction(String name, Object function);
} 