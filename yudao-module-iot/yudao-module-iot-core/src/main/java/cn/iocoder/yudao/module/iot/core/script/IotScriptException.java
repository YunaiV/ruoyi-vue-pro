package cn.iocoder.yudao.module.iot.core.script;

import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import lombok.Getter;

/**
 * IoT 数据解析脚本异常：脚本执行错误（{@link IotScriptResult.Status#ERROR}），或输出不合法（{@link IotScriptResult.Status#OUTPUT_INVALID}）
 *
 * @author 芋道源码
 */
@Getter
public class IotScriptException extends Exception {

    private final IotScriptResult.Status status;
    /**
     * 出错行号；未知时为 -1
     */
    private final int lineNumber;

    public IotScriptException(IotScriptResult.Status status, String message, int lineNumber) {
        super(message, null, false, false);
        this.status = status;
        this.lineNumber = lineNumber;
    }

    /**
     * 创建输出不合法的异常
     *
     * @param message 错误信息
     * @return 异常
     */
    public static IotScriptException outputInvalid(String message) {
        return new IotScriptException(IotScriptResult.Status.OUTPUT_INVALID, message, -1);
    }

}
