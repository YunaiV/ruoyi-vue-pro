package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 自提门店绑定核销人 Request VO")
@Data
@ToString(callSuper = true)
public class DeliveryPickUpBindReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "绑定用户编号组数", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    @NotEmpty(message = "绑定用户编号组数不能未空")
    private List<Long> verifyUserIds;

}