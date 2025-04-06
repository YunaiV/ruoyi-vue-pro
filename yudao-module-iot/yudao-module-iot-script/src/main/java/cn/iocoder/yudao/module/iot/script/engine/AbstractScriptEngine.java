package cn.iocoder.yudao.module.iot.script.engine;

import cn.iocoder.yudao.module.iot.script.context.ScriptContext;
import cn.iocoder.yudao.module.iot.script.sandbox.ScriptSandbox;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象脚本引擎，提供脚本引擎的基本框架
 */
@Slf4j
public abstract class AbstractScriptEngine implements ScriptEngine {

    /**
     * 脚本沙箱，用于提供安全执行环境
     */
    protected final ScriptSandbox sandbox;

    /**
     * 构造函数
     *
     * @param sandbox 脚本沙箱
     */
    protected AbstractScriptEngine(ScriptSandbox sandbox) {
        this.sandbox = sandbox;
    }

    @Override
    public Object execute(String script, ScriptContext context) {
        try {
            // 执行前验证脚本安全性
            sandbox.validate(script);
            // 执行脚本
            return doExecute(script, context);
        } catch (Exception e) {
            log.error("执行脚本出错：{}", e.getMessage(), e);
            throw new RuntimeException("脚本执行失败：" + e.getMessage(), e);
        }
    }

    /**
     * 执行脚本的具体实现
     *
     * @param script  脚本内容
     * @param context 脚本上下文
     * @return 脚本执行结果
     * @throws Exception 执行异常
     */
    protected abstract Object doExecute(String script, ScriptContext context) throws Exception;
}