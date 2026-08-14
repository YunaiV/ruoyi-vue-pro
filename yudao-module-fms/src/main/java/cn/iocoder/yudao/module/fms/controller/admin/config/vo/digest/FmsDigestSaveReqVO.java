package cn.iocoder.yudao.module.fms.controller.admin.config.vo.digest;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * FMS 常用摘要保存 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 常用摘要保存 Request VO")
@Data
public class FmsDigestSaveReqVO {

    @Schema(description = "常用摘要编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "摘要内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "购买办公用品")
    @NotBlank(message = "摘要内容不能为空")
    @Size(max = 500, message = "摘要内容长度不能超过 500 个字符")
    private String content;

}
