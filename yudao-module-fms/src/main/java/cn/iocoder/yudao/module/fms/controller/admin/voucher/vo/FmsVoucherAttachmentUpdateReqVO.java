package cn.iocoder.yudao.module.fms.controller.admin.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - FMS 凭证附件更新 Request VO")
@Data
public class FmsVoucherAttachmentUpdateReqVO {

    @Schema(description = "凭证编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "凭证不能为空")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "附件地址数组")
    @Size(max = 100, message = "凭证附件不能超过 100 个")
    private List<String> attachmentUrls;

}
