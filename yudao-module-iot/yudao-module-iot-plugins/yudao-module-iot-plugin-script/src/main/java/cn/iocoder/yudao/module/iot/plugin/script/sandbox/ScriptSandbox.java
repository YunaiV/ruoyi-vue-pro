package cn.iocoder.yudao.module.iot.plugin.script.sandbox;

/**
 * 脚本沙箱接口，提供脚本执行的安全限制
 */
public interface ScriptSandbox {

    /**
     * 应用沙箱限制到脚本执行环境
     *
     * @param engineContext 引擎上下文
     * @param script        要执行的脚本内容
     */
    void applySandbox(Object engineContext, String script);

    /**
     * 检查脚本是否符合安全规则
     *
     * @param script 要检查的脚本内容
     * @return 是否安全
     */
    boolean validateScript(String script);
} 