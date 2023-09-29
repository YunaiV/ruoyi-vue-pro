package cn.iocoder.yudao.module.pay.api.transfer.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author jason
 */
@Data
public class PayTransferCreateReqDTO {

    /**
     * 应用编号
     */
    @NotNull(message = "应用编号不能为空")
    private Long appId;

    /**
     * 类型
     */
    @NotNull(message = "转账类型不能为空")
    private Integer type;

    /**
     * 商户订单编号
     */
    @NotEmpty(message = "商户订单编号不能为空")
    private String merchantOrderId;

    /**
     * 转账金额，单位：分
     */
    @Min(value = 1, message = "转账金额必须大于零")
    private Integer price;

    /**
     * 转账标题
     */
    private String title;

    @NotEmpty(message = "收款方信息不能为空")
    private Map<String, String> payeeInfo;
}
