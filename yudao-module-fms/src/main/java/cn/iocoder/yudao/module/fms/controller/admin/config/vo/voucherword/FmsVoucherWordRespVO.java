package cn.iocoder.yudao.module.fms.controller.admin.config.vo.voucherword;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * FMS 凭证字 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 凭证字 Response VO")
@Data
public class FmsVoucherWordRespVO {

    @Schema(description = "凭证字编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "凭证字", requiredMode = Schema.RequiredMode.REQUIRED, example = "记")
    private String name;

    @Schema(description = "打印标题", example = "记账凭证")
    private String printTitle;

    @Schema(description = "是否默认凭证字", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean defaultStatus;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accountSetId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
