package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 自提门店更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryPickUpStoreUpdateReqVO extends DeliveryPickUpStoreBaseVO {

    @Schema(description = "编号", required = true, example = "23128")
    @NotNull(message = "编号不能为空")
    private Long id;

}
