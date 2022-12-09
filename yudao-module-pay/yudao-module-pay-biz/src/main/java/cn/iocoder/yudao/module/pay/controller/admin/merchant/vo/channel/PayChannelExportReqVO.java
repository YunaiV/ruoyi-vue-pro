package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(title = "管理后台 - 支付渠道 Excel 导出 Request VO", description = "参数和 PayChannelPageReqVO 是一致的")
@Data
public class PayChannelExportReqVO {

    @Schema(title = "渠道编码")
    private String code;

    @Schema(title = "开启状态")
    private Integer status;

    @Schema(title = "备注")
    private String remark;

    @Schema(title = "渠道费率，单位：百分比")
    private Double feeRate;

    @Schema(title = "商户编号")
    private Long merchantId;

    @Schema(title = "应用编号")
    private Long appId;

    @Schema(title = "支付渠道配置")
    private String config;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(title = "创建时间")
    private LocalDateTime[] createTime;

}
