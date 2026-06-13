package cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 销售出库单行 Response VO")
@Data
public class MesWmProductSalesLineRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "出库单ID", example = "1")
    private Long salesId;

    @Schema(description = "发货通知单行ID", example = "1")
    private Long noticeLineId;

    @Schema(description = "物料ID", example = "1")
    private Long itemId;

    @Schema(description = "物料编码", example = "M001")
    private String itemCode;

    @Schema(description = "物料名称", example = "产品A")
    private String itemName;

    @Schema(description = "规格型号", example = "100g")
    private String specification;

    @Schema(description = "单位名称", example = "个")
    private String unitMeasureName;

    @Schema(description = "出库数量", example = "100")
    private BigDecimal quantity;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "批次号", example = "B20260301001")
    private String batchCode;

    @Schema(description = "是否出厂检验", example = "true")
    private Boolean oqcCheckFlag;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
