package cn.iocoder.yudao.module.iot.plugin.script.engine;

import cn.iocoder.yudao.module.iot.plugin.script.context.ScriptContext;
import cn.iocoder.yudao.module.iot.plugin.script.sandbox.ScriptSandbox;

import java.util.Map;

/**
 * 抽象脚本引擎基类，定义脚本引擎的基本功能
 */
public abstract class AbstractScriptEngine {

    protected ScriptSandbox sandbox;

    /**
     * 初始化脚本引擎
     */
    public abstract void init();

    /**
     * 执行脚本
     *
     * @param script  脚本内容
     * @param context 脚本上下文
     * @return 脚本执行结果
     */
    public abstract Object execute(String script, ScriptContext context);

    /**
     * 执行脚本
     *
     * @param script 脚本内容
     * @param params 脚本参数
     * @return 脚本执行结果
     */
    public abstract Object execute(String script, Map<String, Object> params);

    /**
     * 销毁脚本引擎，释放资源
     */
    public abstract void destroy();

    /**
     * 设置脚本沙箱
     *
     * @param sandbox 脚本沙箱
     */
    public void setSandbox(ScriptSandbox sandbox) {
        this.sandbox = sandbox;
    }
}