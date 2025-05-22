package cn.iocoder.yudao.module.iot.script.engine;

import cn.iocoder.yudao.module.iot.script.context.ScriptContext;

/**
 * 脚本引擎接口，定义脚本执行的核心功能
 */
public interface ScriptEngine {

    /**
     * 执行脚本
     *
     * @param script  脚本内容
     * @param context 脚本上下文
     * @return 脚本执行结果
     */
    Object execute(String script, ScriptContext context);

    /**
     * 获取脚本引擎类型
     *
     * @return 脚本引擎类型
     */
    String getType();
}