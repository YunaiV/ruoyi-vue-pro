package cn.iocoder.yudao.framework.pay.core.client.dto.transfer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.util.Map;

/**
 * 统一转账 Request DTO
 *
 * @author jason
 */
@Data
public class PayTransferUnifiedReqDTO {

    /**
     * 用户 IP
     */
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

    /**
     * 外部转账单编号
     */
    @NotEmpty(message = "外部转账单编号不能为空")
    private String outTransferNo;

    /**
     * 转账金额，单位：分
     */
    @NotNull(message = "转账金额不能为空")
    @Min(value = 1, message = "转账金额必须大于零")
    private Integer price;

    /**
     * 转账标题
     */
    @NotEmpty(message = "转账标题不能为空")
    @Length(max = 128, message = "转账标题不能超过 128")
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

    /**
     * 支付渠道的额外参数
     */
    private Map<String, String> channelExtras;

    /**
     * 转账结果的 notify 回调地址
     */
    @NotEmpty(message = "转账结果的回调地址不能为空")
    @URL(message = "转账结果的 notify 回调地址必须是 URL 格式")
    private String notifyUrl;

}
