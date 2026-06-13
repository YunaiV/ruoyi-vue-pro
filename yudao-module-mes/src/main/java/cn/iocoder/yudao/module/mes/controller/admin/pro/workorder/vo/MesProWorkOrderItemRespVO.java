package cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 工单物料需求 Response VO")
@Data
public class MesProWorkOrderItemRespVO {

    @Schema(description = "物料编号", example = "100")
    private Long itemId;

    @Schema(description = "物料编码", example = "M-001")
    private String itemCode;

    @Schema(description = "物料名称", example = "电阻 10K")
    private String itemName;

    @Schema(description = "规格型号", example = "0603")
    private String itemSpecification;

    @Schema(description = "单位名称", example = "个")
    private String unitMeasureName;

    @Schema(description = "需求数量", example = "1000.00")
    private BigDecimal quantity;

    @Schema(description = "物料/产品标识", example = "ITEM")
    private String itemOrProduct;

}
