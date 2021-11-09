package cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel(value = "支付应用信息分页查询 Response VO", description = "相比于支付信息，还会多出应用渠道的开关信息")
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

    /**
     * 支付渠道
     */
    private PayChannel payChannel;

    /**
     * 支付渠道开通情况
     * 1默认为未开通当前支付渠道，0为已开通支付渠道
     */
    @Data
    @ApiModel("支付渠道")
    public static class PayChannel {

        @ApiModelProperty(value = "微信 JSAPI 支付", required = true, example = "1")
        private Integer wxPub = CommonStatusEnum.DISABLE.getStatus();

        @ApiModelProperty(value = "微信小程序支付", required = true, example = "1")
        private Integer wxLite = CommonStatusEnum.DISABLE.getStatus();

        @ApiModelProperty(value = "微信 App 支付", required = true, example = "1")
        private Integer wxApp = CommonStatusEnum.DISABLE.getStatus();

        @ApiModelProperty(value = "支付宝 PC 网站支付", required = true, example = "1")
        private Integer alipayPc = CommonStatusEnum.DISABLE.getStatus();

        @ApiModelProperty(value = "支付宝 Wap 网站支付", required = true, example = "1")
        private Integer alipayWap = CommonStatusEnum.DISABLE.getStatus();

        @ApiModelProperty(value = "支付宝App 支付", required = true, example = "1")
        private Integer alipayApp = CommonStatusEnum.DISABLE.getStatus();

        @ApiModelProperty(value = "支付宝扫码支付", required = true, example = "1")
        private Integer alipayQr = CommonStatusEnum.DISABLE.getStatus();
    }
}
