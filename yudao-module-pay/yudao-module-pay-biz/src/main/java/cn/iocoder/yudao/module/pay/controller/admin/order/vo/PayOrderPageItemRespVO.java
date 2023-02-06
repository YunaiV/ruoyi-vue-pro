package cn.iocoder.yudao.module.pay.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 支付订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderPageItemRespVO extends PayOrderBaseVO {

    @Schema(description = "支付订单编号", required = true)
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "应用名称")
    private String  appName;

    @Schema(description = "渠道名称")
    private String channelCodeName;

    @Schema(description = "支付订单号")
    private String no;


}
