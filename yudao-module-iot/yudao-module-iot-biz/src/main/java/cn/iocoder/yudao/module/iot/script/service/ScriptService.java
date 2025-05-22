package cn.iocoder.yudao.module.iot.script.service;

import cn.iocoder.yudao.module.iot.script.context.ScriptContext;

import java.util.Map;

/**
 * 脚本服务接口，定义脚本执行的核心功能
 */
public interface ScriptService {

    /**
     * 执行脚本
     *
     * @param scriptType 脚本类型（如 js、groovy 等）
     * @param script     脚本内容
     * @param context    脚本上下文
     * @return 脚本执行结果
     */
    Object executeScript(String scriptType, String script, ScriptContext context);

    /**
     * 执行脚本
     *
     * @param scriptType 脚本类型（如 js、groovy 等）
     * @param script     脚本内容
     * @param params     脚本参数
     * @return 脚本执行结果
     */
    Object executeScript(String scriptType, String script, Map<String, Object> params);

    /**
     * 执行 JavaScript 脚本
     *
     * @param script  脚本内容
     * @param context 脚本上下文
     * @return 脚本执行结果
     */
    Object executeJavaScript(String script, ScriptContext context);

    /**
     * 执行 JavaScript 脚本
     *
     * @param script 脚本内容
     * @param params 脚本参数
     * @return 脚本执行结果
     */
    Object executeJavaScript(String script, Map<String, Object> params);

    /**
     * 验证脚本内容是否安全
     *
     * @param scriptType 脚本类型
     * @param script     脚本内容
     * @return 脚本是否安全
     */
    boolean validateScript(String scriptType, String script);
}