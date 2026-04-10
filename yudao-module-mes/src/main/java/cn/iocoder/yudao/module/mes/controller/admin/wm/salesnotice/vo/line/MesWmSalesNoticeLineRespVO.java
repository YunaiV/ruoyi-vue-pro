package cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 发货通知单行 Response VO")
@Data
public class MesWmSalesNoticeLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "发货通知单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long noticeId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "产品A")
    private String itemName;

    @Schema(description = "规格型号", example = "10mm*100mm")
    private String specification;

    @Schema(description = "计量单位名称", example = "个")
    private String unitMeasureName;

    @Schema(description = "批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "BATCH001")
    private String batchCode;

    @Schema(description = "发货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "是否检验", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean oqcCheckFlag;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
