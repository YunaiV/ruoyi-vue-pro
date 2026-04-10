package cn.iocoder.yudao.module.mes.controller.admin.wm.productproduce.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产入库单行 Response VO")
@Data
public class MesWmProductProduceLineRespVO {

    @Schema(description = "行ID", example = "1")
    private Long id;

    @Schema(description = "入库单ID", example = "1")
    private Long produceId;

    @Schema(description = "报工单 ID", example = "1")
    private Long feedbackId;

    @Schema(description = "物料ID", example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "钢板")
    private String itemName;

    @Schema(description = "规格型号", example = "10mm*100mm")
    private String specification;

    @Schema(description = "计量单位名称", example = "千克")
    private String unitMeasureName;

    @Schema(description = "入库数量", example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "BATCH20260101001")
    private String batchCode;

    @Schema(description = "有效期")
    private LocalDateTime expireDate;

    @Schema(description = "批号", example = "LOT001")
    private String lotNumber;

    @Schema(description = "质检状态", example = "1")
    private Integer qualityStatus;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
