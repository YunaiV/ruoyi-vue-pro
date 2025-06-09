package cn.iocoder.yudao.module.wms.controller.admin.exchange.item.vo;

import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author jisencai
 * @table-fields : exchange_id,product_id,qty,remark,id,from_bin_id,to_bin_id
 */
@Schema(description = "管理后台 - 良次换货详情新增/修改 Request VO")
@Data
public class WmsExchangeItemSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1234")
    @NotNull(message = "主键不能为空", groups = { ValidationGroup.update.class })
    private Long id;

    @Schema(description = "换货单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7120")
    private Long exchangeId;

    @Schema(description = "标准产品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6112")
    @NotNull(message = "标准产品ID不能为空", groups = { ValidationGroup.create.class, ValidationGroup.update.class })
    private Long productId;

    @Schema(description = "源仓位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6726")
    private Long fromBinId;

    @Schema(description = "目的仓位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32001")
    private Long toBinId;

    @Schema(description = "换货量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qty;

    @Schema(description = "备注", example = "随便")
    private String remark;
}
