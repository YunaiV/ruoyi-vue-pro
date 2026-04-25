package cn.iocoder.yudao.module.deepay.service.tool;

/**
 * 工具执行异常 — 携带可返回给 LLM 的错误描述。
 */
public class ToolExecutionException extends RuntimeException {

    private final String toolName;
    private final String errorCode;

    public ToolExecutionException(String toolName, String errorCode, String message) {
        super(message);
        this.toolName  = toolName;
        this.errorCode = errorCode;
    }

    public ToolExecutionException(String toolName, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.toolName  = toolName;
        this.errorCode = errorCode;
    }

    public String getToolName()  { return toolName; }
    public String getErrorCode() { return errorCode; }

}
