package cn.iocoder.yudao.module.system.controller.admin.errorcode.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 错误码创建/修改 Request VO")
@Data
public class ErrorCodeSaveReqVO {

    @Schema(description = "错误码编号", example = "1024")
    private Long id;

    @Schema(description = "应用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "dashboard")
    @NotNull(message = "应用名不能为空")
    private String applicationName;

    @Schema(description = "错误码编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234")
    @NotNull(message = "错误码编码不能为空")
    private Integer code;

    @Schema(description = "错误码错误提示", requiredMode = Schema.RequiredMode.REQUIRED, example = "帅气")
    @NotNull(message = "错误码错误提示不能为空")
    private String message;

    @Schema(description = "备注", example = "哈哈哈")
    private String memo;

}
