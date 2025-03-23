package cn.iocoder.yudao.module.iot.controller.admin.product.vo.script;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 产品脚本测试 Response VO")
@Data
public class IotProductScriptTestRespVO {

    @Schema(description = "测试是否成功", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean success;

    @Schema(description = "测试结果输出")
    private Object output;

    @Schema(description = "错误消息，失败时返回")
    private String errorMessage;

    @Schema(description = "执行耗时（毫秒）")
    private Long executionTimeMs;

    // 静态工厂方法 - 成功
    public static IotProductScriptTestRespVO success(Object output, Long executionTimeMs) {
        IotProductScriptTestRespVO respVO = new IotProductScriptTestRespVO();
        respVO.setSuccess(true);
        respVO.setOutput(output);
        respVO.setExecutionTimeMs(executionTimeMs);
        return respVO;
    }

    // 静态工厂方法 - 失败
    public static IotProductScriptTestRespVO error(String errorMessage, Long executionTimeMs) {
        IotProductScriptTestRespVO respVO = new IotProductScriptTestRespVO();
        respVO.setSuccess(false);
        respVO.setErrorMessage(errorMessage);
        respVO.setExecutionTimeMs(executionTimeMs);
        return respVO;
    }
}