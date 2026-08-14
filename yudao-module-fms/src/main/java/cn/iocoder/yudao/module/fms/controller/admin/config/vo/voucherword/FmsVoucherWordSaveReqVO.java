package cn.iocoder.yudao.module.fms.controller.admin.config.vo.voucherword;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * FMS 凭证字保存 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 凭证字保存 Request VO")
@Data
public class FmsVoucherWordSaveReqVO {

    @Schema(description = "凭证字编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "凭证字", requiredMode = Schema.RequiredMode.REQUIRED, example = "记")
    @NotBlank(message = "凭证字不能为空")
    @Size(max = 255, message = "凭证字长度不能超过 255 个字符")
    private String name;

    @Schema(description = "打印标题", example = "记账凭证")
    @Size(max = 255, message = "打印标题长度不能超过 255 个字符")
    private String printTitle;

    @Schema(description = "是否默认凭证字", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否默认凭证字不能为空")
    private Boolean defaultStatus;

}
