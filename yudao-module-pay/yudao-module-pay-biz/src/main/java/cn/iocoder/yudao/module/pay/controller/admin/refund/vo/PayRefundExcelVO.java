package cn.iocoder.yudao.module.pay.controller.admin.refund.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.pay.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款订单 Excel VO
 *
 * @author aquan
 */
@Data
public class PayRefundExcelVO {

    @ExcelProperty("支付退款编号")
    private Long id;

    @ExcelProperty("商品名称")
    private String subject;

    @ExcelProperty(value = "商户名称")
    private String merchantName;

    @ExcelProperty(value = "应用名称")
    private String appName;

    @ExcelProperty(value = "渠道编号名称")
    private String channelCodeName;

    @ExcelProperty("交易订单号")
    private String tradeNo;

    @ExcelProperty("商户订单编号")
    private String merchantOrderId;

    @ExcelProperty("商户退款订单号")
    private String merchantRefundNo;

    @ExcelProperty("异步通知商户地址")
    private String notifyUrl;

    @DictFormat(DictTypeConstants.ORDER_NOTIFY_STATUS)
    @ExcelProperty(value = "商户退款结果回调状态", converter = DictConvert.class)
    private Integer notifyStatus;

    @DictFormat(DictTypeConstants.REFUND_ORDER_STATUS)
    @ExcelProperty(value = "退款状态", converter = DictConvert.class)
    private Integer status;

    @DictFormat(DictTypeConstants.REFUND_ORDER_TYPE)
    @ExcelProperty(value = "退款类型", converter = DictConvert.class)
    private Integer type;

    @ExcelProperty("支付金额，单位：元")
    private String payAmount;

    @ExcelProperty("退款金额，单位：元")
    private String refundAmount;

    @ExcelProperty("退款原因")
    private String reason;

    @ExcelProperty("用户付款 IP")
    private String userIp;

    @ExcelProperty("渠道订单号")
    private String channelOrderNo;

    @ExcelProperty("渠道退款单号")
    private String channelRefundNo;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("退款成功时间")
    private LocalDateTime successTime;

    @ExcelProperty("退款通知时间")
    private LocalDateTime notifyTime;

    @ExcelProperty("退款失效时间")
    private LocalDateTime expireTime;

}
