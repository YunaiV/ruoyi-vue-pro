package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(title = "管理后台 - 支付商户信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayMerchantRespVO extends PayMerchantBaseVO {

    @Schema(title = "商户编号", required = true)
    private Long id;

    @Schema(title = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(title = "商户号", required = true, example = "M233666999")
    private String no;

}
