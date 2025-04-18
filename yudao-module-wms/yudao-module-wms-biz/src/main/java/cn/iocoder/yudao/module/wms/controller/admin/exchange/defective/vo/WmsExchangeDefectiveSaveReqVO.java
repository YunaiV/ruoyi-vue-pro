package cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 良次换货详情新增/修改 Request VO")
@Data
public class WmsExchangeDefectiveSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234")
    private Long id;

    @Schema(description = "换货单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7120")
    @NotNull(message = "换货单ID不能为空")
    private Long exchangeId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6112")
    @NotNull(message = "标准产品ID不能为空")
    private Long productId;

    @Schema(description = "源仓位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6726")
    @NotNull(message = "源仓位ID不能为空")
    private Long fromBinId;

    @Schema(description = "目的仓位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32001")
    @NotNull(message = "目的仓位ID不能为空")
    private Long toBinId;

    @Schema(description = "换货量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "换货量不能为空")
    private Integer qty;

    @Schema(description = "备注", example = "随便")
    private String remark;

}