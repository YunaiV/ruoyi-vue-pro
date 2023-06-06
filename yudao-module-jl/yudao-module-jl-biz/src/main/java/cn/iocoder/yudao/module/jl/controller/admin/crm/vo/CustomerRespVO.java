package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import cn.iocoder.yudao.module.jl.controller.admin.user.vo.UserRespVO;
import cn.iocoder.yudao.module.jl.entity.crm.Followup;
import cn.iocoder.yudao.module.jl.entity.crm.Institution;
import cn.iocoder.yudao.module.jl.entity.crm.Saleslead;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客户 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CustomerRespVO extends CustomerBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28406")
    private Long id;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    private Institution company;
    private Institution hospital;
    private Institution university;
    private Followup lastFollowup;
    private Saleslead lastSaleslead;
    private UserRespVO sales;
}
