package cn.iocoder.yudao.module.trade.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.Mobile;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 交易订单的分页 Request VO")
@Data
public class TradeOrderPageReqVO extends PageParam {

    @ApiModelProperty(value = "订单号", example = "88888888", notes = "模糊匹配")
    private String no;

    @ApiModelProperty(value = "用户编号", example = "1024")
    private Long userId;

    @ApiModelProperty(value = "用户昵称", example = "小王", notes = "模糊匹配")
    private String userNickname;

    @ApiModelProperty(value = "用户手机号", example = "小王", notes = "精准匹配")
    @Mobile
    private String userMobile;

    @ApiModelProperty(value = "收件人名称", example = "小红", notes = "模糊匹配")
    private String receiverName;

    @ApiModelProperty(value = "收件人手机", example = "1560", notes = "模糊匹配")
    @Mobile
    private String receiverMobile;

    @ApiModelProperty(value = "订单类型", example = "1", notes = "参见 TradeOrderTypeEnum 枚举")
    private Integer type;

    @ApiModelProperty(value = "订单状态", example = "1", notes = "参见 TradeOrderStatusEnum 枚举")
    @InEnum(value = TradeOrderStatusEnum.class, message = "订单状态必须是 {value}")
    private Integer status;

    @ApiModelProperty(value = "支付渠道", example = "wx_lite")
    private String payChannelCode;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
