package cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 装箱明细 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 装箱明细 Response VO")
@Data
@Accessors(chain = true)
public class MesWmPackageLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "装箱单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long packageId;

    @Schema(description = "库存记录 ID", example = "1")
    private Long materialStockId;

    @Schema(description = "产品物料 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "螺栓")
    private String itemName;

    @Schema(description = "规格型号", example = "M10*50")
    private String specification;

    @Schema(description = "计量单位名称", example = "个")
    private String unitMeasureName;

    @Schema(description = "装箱数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "生产工单 ID", example = "1")
    private Long workOrderId;

    @Schema(description = "生产工单编号", example = "WO20260301")
    private String workOrderCode;

    @Schema(description = "批次号", example = "BATCH001")
    private String batchCode;

    @Schema(description = "有效期")
    private LocalDateTime expireDate;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
