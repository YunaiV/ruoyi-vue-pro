package cn.iocoder.yudao.module.wms.controller.admin.exchange.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 换货单新增/修改 Request VO")
@Data
public class WmsExchangeSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "20006")
    private Long id;

    @Schema(description = "单据号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "单据号不能为空")
    private String code;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "类型不能为空")
    private Integer type;

    @Schema(description = "调出仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27440")
    @NotNull(message = "调出仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态不能为空")
    private String auditStatus;

    @Schema(description = "特别说明", example = "你猜")
    private String remark;

}