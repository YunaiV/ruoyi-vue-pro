package cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer;

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
     *
     * 微信支付：sceneId 和 scene_report_infos 字段，必须传递；参考 <a href="https://pay.weixin.qq.com/doc/v3/merchant/4012711988#%EF%BC%883%EF%BC%89%E6%8C%89%E8%BD%AC%E8%B4%A6%E5%9C%BA%E6%99%AF%E6%8A%A5%E5%A4%87%E8%83%8C%E6%99%AF%E4%BF%A1%E6%81%AF">按转账场景报备背景信息</>
     */
    private Map<String, String> channelExtras;

    /**
     * 转账结果的 notify 回调地址
     */
    @NotEmpty(message = "转账结果的回调地址不能为空")
    @URL(message = "转账结果的 notify 回调地址必须是 URL 格式")
    private String notifyUrl;

}
