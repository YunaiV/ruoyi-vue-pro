package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 支付应用信息 Excel 导出 Request VO,参数和 PayAppPageReqVO 是一致的")
@Data
public class PayAppExportReqVO {

    @Schema(description = "应用名")
    private String name;

    @Schema(description = "开启状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "支付结果的回调地址")
    private String payNotifyUrl;

    @Schema(description = "退款结果的回调地址")
    private String refundNotifyUrl;

    @Schema(description = "商户名称")
    private String merchantName;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
