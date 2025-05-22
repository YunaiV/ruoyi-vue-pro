package cn.iocoder.yudao.module.iot.script.sandbox;

/**
 * 脚本沙箱接口，提供脚本安全性验证
 */
public interface ScriptSandbox {

    /**
     * 验证脚本内容是否安全
     *
     * @param script 脚本内容
     * @return 脚本是否安全
     */
    boolean validate(String script);

    /**
     * 获取沙箱类型
     *
     * @return 沙箱类型
     */
    String getType();
}