package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 交易订单源站下单 Request VO")
@Data
public class TradeOrderSourceOrderReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "订单编号不能为空")
    private Long id;

    @Schema(description = "来源平台", example = "1688")
    private String sourcePlatform;

    @Schema(description = "源站订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "168812345678")
    @NotNull(message = "源站订单号不能为空")
    private String sourceOrderNo;

    @Schema(description = "源站下单时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime sourceOrderTime;

    @Schema(description = "采购备注", example = "已下单，等待发货")
    private String purchaseRemark;

}
