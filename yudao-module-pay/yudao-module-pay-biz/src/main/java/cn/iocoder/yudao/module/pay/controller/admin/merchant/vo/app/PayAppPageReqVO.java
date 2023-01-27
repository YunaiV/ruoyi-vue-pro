package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;

import lombok.*;

import java.time.LocalDateTime;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 支付应用信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppPageReqVO extends PageParam {

    @ApiModelProperty(value = "应用名")
    private String name;

    @ApiModelProperty(value = "开启状态")
    private Integer status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "支付结果的回调地址")
    private String payNotifyUrl;

    @ApiModelProperty(value = "退款结果的回调地址")
    private String refundNotifyUrl;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime[] createTime;

}
