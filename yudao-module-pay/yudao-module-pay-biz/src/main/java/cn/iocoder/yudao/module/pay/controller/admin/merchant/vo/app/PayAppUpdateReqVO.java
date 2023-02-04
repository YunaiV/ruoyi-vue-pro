package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.app;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 支付应用信息更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayAppUpdateReqVO extends PayAppBaseVO {

    @Schema(description = "应用编号", required = true)
    @NotNull(message = "应用编号不能为空")
    private Long id;

}
