package cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel("支付商户信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayMerchantRespVO extends PayMerchantBaseVO {

    @ApiModelProperty(value = "商户编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    /**
     * 商户号
     * 例如说，M233666999
     * 只有新增时插入，不允许修改
     */
    private String no;

}
