package cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * 支付订单 Response VO
 *
 * @author aquan
 */
@ApiModel("支付订单 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderRespVO extends PayOrderBaseVO {

    @ApiModelProperty(value = "支付订单编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
