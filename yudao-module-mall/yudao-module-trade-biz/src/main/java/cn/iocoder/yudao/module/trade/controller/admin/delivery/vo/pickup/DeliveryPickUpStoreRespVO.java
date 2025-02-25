package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import cn.iocoder.yudao.module.trade.controller.admin.base.system.user.UserSimpleBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 自提门店 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryPickUpStoreRespVO extends DeliveryPickUpStoreBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "核销用户数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<UserSimpleBaseVO> verifyUsers;

}
