package cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - MES 销售退货单新增/修改 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - MES 销售退货单新增/修改 Request VO")
@Data
public class MesWmReturnSalesSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "退货单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "RS202603010001")
    @NotBlank(message = "退货单编号不能为空")
    private String code;

    @Schema(description = "退货单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "销售退货")
    @NotBlank(message = "退货单名称不能为空")
    private String name;

    @Schema(description = "销售订单编号", example = "SO202603010001")
    private String salesOrderCode;

    @Schema(description = "客户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "客户 ID 不能为空")
    private Long clientId;

    @Schema(description = "退货日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "退货日期不能为空")
    private LocalDateTime returnDate;

    @Schema(description = "退货原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "质量问题")
    @NotBlank(message = "退货原因不能为空")
    private String returnReason;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
