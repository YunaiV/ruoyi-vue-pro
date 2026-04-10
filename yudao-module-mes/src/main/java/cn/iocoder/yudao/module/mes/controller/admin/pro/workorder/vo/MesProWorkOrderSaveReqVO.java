package cn.iocoder.yudao.module.mes.controller.admin.pro.workorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 生产工单新增/修改 Request VO")
@Data
public class MesProWorkOrderSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "工单编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WO-001")
    @NotEmpty(message = "工单编码不能为空")
    private String code;

    @Schema(description = "工单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "生产工单-A")
    @NotEmpty(message = "工单名称不能为空")
    private String name;

    @Schema(description = "工单类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工单类型不能为空")
    private Integer type;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源类型不能为空")
    private Integer orderSourceType;

    @Schema(description = "来源单据编号", example = "SO-001")
    private String orderSourceCode;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "产品不能为空")
    private Long productId;

    @Schema(description = "生产数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "生产数量不能为空")
    private BigDecimal quantity;

    @Schema(description = "已生产数量", example = "0")
    private BigDecimal quantityProduced;

    @Schema(description = "调整数量", example = "0")
    private BigDecimal quantityChanged;

    @Schema(description = "已排产数量", example = "0")
    private BigDecimal quantityScheduled;

    @Schema(description = "客户编号", example = "300")
    private Long clientId;

    @Schema(description = "供应商编号", example = "400")
    private Long vendorId;

    @Schema(description = "批次号", example = "BATCH-001")
    private String batchCode;

    @Schema(description = "需求日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "需求日期不能为空")
    private LocalDateTime requestDate;

    @Schema(description = "父工单编号", example = "0")
    private Long parentId;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
