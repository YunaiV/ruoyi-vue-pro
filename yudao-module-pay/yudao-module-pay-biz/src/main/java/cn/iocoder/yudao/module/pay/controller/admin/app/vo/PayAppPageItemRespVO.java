package cn.iocoder.yudao.module.pay.controller.admin.app.vo;

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

    @Schema(description = "应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "已配置的支付渠道编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "[alipay_pc, alipay_wap]")
    private Set<String> channelCodes;

}
