package cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 到货通知单行 Response VO")
@Data
public class MesWmArrivalNoticeLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "到货通知单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long noticeId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "钢板")
    private String itemName;

    @Schema(description = "规格型号", example = "10mm*100mm")
    private String specification;

    @Schema(description = "计量单位名称", example = "千克")
    private String unitMeasureName;

    @Schema(description = "到货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "500.00")
    private BigDecimal arrivalQuantity;

    @Schema(description = "合格数量", example = "500.00")
    private BigDecimal qualifiedQuantity;

    @Schema(description = "是否需要来料检验", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean iqcCheckFlag;

    @Schema(description = "来料检验单编号", example = "1")
    private Long iqcId;

    @Schema(description = "来料检验单编码", example = "IQC20260201")
    private String iqcCode;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
