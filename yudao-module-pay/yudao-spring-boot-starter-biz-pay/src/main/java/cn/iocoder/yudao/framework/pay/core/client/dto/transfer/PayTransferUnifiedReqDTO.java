package cn.iocoder.yudao.framework.pay.core.client.dto.transfer;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.util.Map;

import static cn.iocoder.yudao.framework.pay.core.enums.transfer.PayTransferTypeEnum.*;

/**
 * 统一转账 Request DTO
 *
 * @author jason
 */
@Data
public class PayTransferUnifiedReqDTO {

    /**
     * 转账类型
     *
     * 关联 {@link PayTransferTypeEnum#getType()}
     */
    @NotNull(message = "转账类型不能为空")
    @InEnum(PayTransferTypeEnum.class)
    private Integer type;

    /**
     * 用户 IP
     */
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

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
     * 收款人姓名
     */
    @NotBlank(message = "收款人姓名不能为空", groups = {Alipay.class})
    private String userName;

    /**
     * 支付宝登录号
     */
    @NotBlank(message = "支付宝登录号不能为空", groups = {Alipay.class})
    private String alipayLogonId;

    /**
     * 微信 openId
     */
    @NotBlank(message = "微信 openId 不能为空", groups = {WxPay.class})
    private String openid;

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
