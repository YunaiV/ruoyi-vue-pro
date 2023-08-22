package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 订单修改地址 Request VO")
@Data
public class TradeOrderUpdateAddressReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "订单编号不能为空")
    private Long id;

    @Schema(description = "收件人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "z张三")
    @NotEmpty(message = "收件人名称不能为空")
    private String receiverName;

    @Schema(description = "收件人手机", requiredMode = Schema.RequiredMode.REQUIRED, example = "19988188888")
    @NotEmpty(message = "收件人手机不能为空")
    private String receiverMobile;

    @Schema(description = "收件人地区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7310")
    @NotNull(message = "收件人地区编号不能为空")
    private Integer receiverAreaId;

    @Schema(description = "收件人详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "昆明市五华区xxx小区xxx")
    @NotEmpty(message = "收件人详细地址不能为空")
    private String receiverDetailAddress;

}
