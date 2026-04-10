package cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.line;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理后台 - MES 销售退货相关
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 销售退货单行 Response VO")
@Data
public class MesWmReturnSalesLineRespVO {

    @Schema(description = "行ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "退货单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long returnId;

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

    @Schema(description = "退货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "退货检验单 ID", example = "400")
    private Long rqcId;

    @Schema(description = "是否需要质检", example = "true")
    private Boolean rqcCheckFlag;

    @Schema(description = "批次号", example = "B001")
    private String batchCode;

    @Schema(description = "质量状态", example = "合格")
    private Integer qualityStatus;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
