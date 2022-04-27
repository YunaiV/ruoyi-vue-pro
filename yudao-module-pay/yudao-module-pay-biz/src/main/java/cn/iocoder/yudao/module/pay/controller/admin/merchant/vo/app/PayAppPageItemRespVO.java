package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.Set;

@ApiModel(value = "管理后台 - 支付应用信息分页查询 Response VO", description = "相比于支付信息，还会多出应用渠道的开关信息")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppPageItemRespVO extends PayAppBaseVO {

    @ApiModelProperty(value = "应用编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

    /**
     * 所属商户
     */
    private PayMerchant payMerchant;

    @ApiModel("商户")
    @Data
    public static class PayMerchant {

        @ApiModelProperty(value = "商户编号", required = true, example = "1")
        private Long id;

        @ApiModelProperty(value = "商户名称", required = true, example = "研发部")
        private String name;

    }

    @ApiModelProperty(value = "渠道编码集合", required = true, example = "alipay_pc,alipay_wap...")
    private Set<String> channelCodes;


}
