package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 快递运费模板 精简 Response VO")
@Data
@ToString(callSuper = true)
public class DeliveryExpressTemplateSimpleRespVO {

    @Schema(description = "编号，自增", required = true, example = "371")
    private Long id;

    @Schema(description = "模板名称", required = true, example = "王五")
    private String name;

}
