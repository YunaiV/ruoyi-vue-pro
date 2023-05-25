package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 销售线索跟进，可以是跟进客户，也可以是跟进线索 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FollowupRespVO extends FollowupBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "767")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "客户信息", defaultValue = "{}")
    private CustomerRespVO customer;

//    @Schema(description = "创建者", defaultValue = "{}")
//    private UserProfileRespVO creator;
}
