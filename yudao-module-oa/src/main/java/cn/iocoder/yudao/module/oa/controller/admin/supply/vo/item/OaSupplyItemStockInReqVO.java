package cn.iocoder.yudao.module.oa.controller.admin.supply.vo.item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 办公用品入库 Request VO")
@Data
public class OaSupplyItemStockInReqVO {

    @Schema(description = "办公用品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "办公用品编号不能为空")
    private Long id;

    @Schema(description = "入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量不能小于 {value}")
    private Integer quantity;

}
