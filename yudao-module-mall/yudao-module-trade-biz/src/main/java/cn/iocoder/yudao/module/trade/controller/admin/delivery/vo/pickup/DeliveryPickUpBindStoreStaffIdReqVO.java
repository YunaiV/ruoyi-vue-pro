package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 自提门店创建 Request VO")
@Data
@ToString(callSuper = true)
public class DeliveryPickUpBindStoreStaffIdReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "绑定用户编号组数", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    @NotNull(message = "绑定用户编号组数不能未空")
    private List<Long> storeStaffIds;

}
