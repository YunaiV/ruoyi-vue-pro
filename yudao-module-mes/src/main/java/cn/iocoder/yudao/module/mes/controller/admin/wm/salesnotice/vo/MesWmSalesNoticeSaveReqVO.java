package cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 发货通知单新增/修改 Request VO")
@Data
public class MesWmSalesNoticeSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "通知单编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "SN202603010001")
    @NotEmpty(message = "通知单编码不能为空")
    private String code;

    @Schema(description = "通知单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试发货通知")
    @NotEmpty(message = "通知单名称不能为空")
    private String name;

    @Schema(description = "销售订单编号", example = "SO202603010001")
    private String salesOrderCode;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "客户编号不能为空")
    private Long clientId;

    @Schema(description = "发货日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发货日期不能为空")
    private LocalDateTime salesDate;

    @Schema(description = "收货人", example = "张三")
    private String recipientName;

    @Schema(description = "联系方式", example = "13800138000")
    private String recipientTelephone;

    @Schema(description = "收货地址", example = "北京市朝阳区xxx")
    private String recipientAddress;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
