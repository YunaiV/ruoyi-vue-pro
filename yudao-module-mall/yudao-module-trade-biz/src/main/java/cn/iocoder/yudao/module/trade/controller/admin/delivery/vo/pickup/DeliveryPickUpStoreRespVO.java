package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 自提门店 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryPickUpStoreRespVO extends DeliveryPickUpStoreBaseVO {

    @Schema(description = "编号", required = true, example = "23128")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
