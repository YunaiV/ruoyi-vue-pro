package cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES 装箱单新增/修改 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 装箱单新增/修改 Request VO")
@Data
public class MesWmPackageSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "装箱单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PKG20260307001")
    @NotEmpty(message = "装箱单编号不能为空")
    private String code;

    @Schema(description = "装箱日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "装箱日期不能为空")
    private LocalDateTime packageDate;

    @Schema(description = "销售订单编号", example = "SO20260301")
    private String salesOrderCode;

    @Schema(description = "发票编号", example = "INV20260301")
    private String invoiceCode;

    @Schema(description = "客户 ID", example = "1")
    private Long clientId;

    @Schema(description = "箱长度", example = "60.00")
    private BigDecimal length;

    @Schema(description = "箱宽度", example = "40.00")
    private BigDecimal width;

    @Schema(description = "箱高度", example = "30.00")
    private BigDecimal height;

    @Schema(description = "尺寸单位 ID", example = "1")
    private Long sizeUnitId;

    @Schema(description = "净重", example = "10.00")
    private BigDecimal netWeight;

    @Schema(description = "毛重", example = "12.00")
    private BigDecimal grossWeight;

    @Schema(description = "重量单位 ID", example = "2")
    private Long weightUnitId;

    @Schema(description = "检查员用户 ID", example = "1")
    private Long inspectorUserId;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
