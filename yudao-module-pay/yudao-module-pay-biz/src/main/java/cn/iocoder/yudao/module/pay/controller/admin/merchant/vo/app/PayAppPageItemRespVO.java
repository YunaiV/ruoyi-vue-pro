package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "管理后台 - 支付应用信息分页查询 Response VO,相比于支付信息，还会多出应用渠道的开关信息")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppPageItemRespVO extends PayAppBaseVO {

    @Schema(description = "应用编号", required = true)
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    /**
     * 所属商户
     */
    private PayMerchant payMerchant;

    @Schema(description = "商户")
    @Data
    public static class PayMerchant {

        @Schema(description = "商户编号", required = true, example = "1")
        private Long id;

        @Schema(description = "商户名称", required = true, example = "研发部")
        private String name;

    }

    @Schema(description = "渠道编码集合", required = true, example = "alipay_pc,alipay_wap...")
    private Set<String> channelCodes;


}
