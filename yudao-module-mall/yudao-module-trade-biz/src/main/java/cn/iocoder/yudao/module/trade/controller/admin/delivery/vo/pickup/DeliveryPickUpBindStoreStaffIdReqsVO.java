package cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.pickup;

import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 自提门店创建 Request VO")
@Data
@ToString(callSuper = true)
@Builder
public class DeliveryPickUpBindStoreStaffIdReqsVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    private Long id;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    private String name;

    @Schema(description = "绑定用户信息组数", requiredMode = Schema.RequiredMode.REQUIRED, example = "23128")
    private List<AdminUserRespDTO> storeStaffs;

}
