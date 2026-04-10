package cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.detail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理后台 - MES 销售退货相关
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 销售退货明细 Response VO")
@Data
public class MesWmReturnSalesDetailRespVO {

    @Schema(description = "明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "退货单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long returnId;

    @Schema(description = "行ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long lineId;

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "物料A")
    private String itemName;

    @Schema(description = "物料规格", example = "100*200")
    private String itemSpecification;

    @Schema(description = "计量单位", example = "个")
    private String itemUnit;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "B001")
    private String batchCode;

    @Schema(description = "仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long warehouseId;

    @Schema(description = "仓库编码", example = "WH001")
    private String warehouseCode;

    @Schema(description = "仓库名称", example = "仓库A")
    private String warehouseName;

    @Schema(description = "库区ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long locationId;

    @Schema(description = "库区编码", example = "LOC001")
    private String locationCode;

    @Schema(description = "库区名称", example = "库区A")
    private String locationName;

    @Schema(description = "库位ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long areaId;

    @Schema(description = "库位编码", example = "AREA001")
    private String areaCode;

    @Schema(description = "库位名称", example = "库位A")
    private String areaName;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
