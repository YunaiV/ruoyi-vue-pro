package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 快递运费模板 精简 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryExpressTemplateSimpleRespVO extends DeliveryExpressTemplateBaseVO {

    @Schema(description = "编号，自增", required = true, example = "371")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
