package cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - MES 库存台账分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesWmMaterialStockPageReqVO extends PageParam {

    @Schema(description = "物料分类编号", example = "1")
    private Long itemTypeId;

    @Schema(description = "物料编号", example = "1")
    private Long itemId;

    @Schema(description = "批次号", example = "B20260101")
    private String batchCode;

    @Schema(description = "仓库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

    @Schema(description = "供应商编号", example = "1")
    private Long vendorId;

    @Schema(description = "是否冻结", example = "false")
    private Boolean frozen;

}
