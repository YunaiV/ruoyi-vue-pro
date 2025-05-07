package cn.iocoder.yudao.module.pay.api.transfer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 转账单创建 Request DTO
 *
 * @author jason
 */
@Data
public class PayTransferCreateReqDTO {

    /**
     * 应用标识
     */
    @NotNull(message = "应用标识不能为空")
    private String appKey;

    /**
     * 转账渠道
     */
    @NotEmpty(message = "转账渠道不能为空")
    private String channelCode;

    /**
     * 转账渠道的额外参数
     */
    private Map<String, String> channelExtras;

    /**
     * 用户 IP
     */
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

    /**
     * 商户转账单编号
     */
    @NotEmpty(message = "商户转账单编号能为空")
    private String merchantTransferId;

    /**
     * 转账金额，单位：分
     */
    @Min(value = 1, message = "转账金额必须大于零")
    @NotNull(message = "转账金额不能为空")
    private Integer price;

    /**
     * 转账标题
     */
    @NotEmpty(message = "转账标题不能为空")
    private String subject;

    /**
     * 收款人账号
     *
     * 微信场景下：openid
     * 支付宝场景下：支付宝账号
     */
    @NotEmpty(message = "收款人账号不能为空")
    private String userAccount;
    /**
     * 收款人姓名
     */
    private String userName;

}
