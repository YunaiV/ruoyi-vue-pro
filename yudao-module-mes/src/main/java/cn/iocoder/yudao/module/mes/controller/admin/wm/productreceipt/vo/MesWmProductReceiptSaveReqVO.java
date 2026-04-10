package cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 产品收货单新增/修改 Request VO")
@Data
public class MesWmProductReceiptSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "收货单编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PR202603010001")
    @NotEmpty(message = "收货单编码不能为空")
    private String code;

    @Schema(description = "收货单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "产品收货单-工单001")
    @NotEmpty(message = "收货单名称不能为空")
    private String name;

    @Schema(description = "生产工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "收货日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "收货日期不能为空")
    private LocalDateTime receiptDate;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
