package cn.iocoder.yudao.module.wms.controller.admin.inventory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - WMS 库存统计列表 Request VO")
@Data
public class WmsInventoryListReqVO {

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "仓库编号不能为空")
    private Long warehouseId;

}
