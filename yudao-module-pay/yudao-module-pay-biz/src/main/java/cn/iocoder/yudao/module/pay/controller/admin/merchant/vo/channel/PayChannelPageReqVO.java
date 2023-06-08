package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 支付渠道 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelPageReqVO extends PageParam {

    @Schema(description = "渠道编码")
    private String code;

    @Schema(description = "开启状态")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "渠道费率，单位：百分比")
    private Double feeRate;

    @Schema(description = "商户编号")
    private Long merchantId;

    @Schema(description = "应用编号")
    private Long appId;

    @Schema(description = "支付渠道配置")
    private String config;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
