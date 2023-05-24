package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserRespVO;
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

    @Schema(description = "医院信息")
    private InstitutionRespVO hospital;

    @Schema(description = "学校信息")
    private InstitutionRespVO university;

    @Schema(description = "公司信息")
    private InstitutionRespVO company;

    @Schema(description = "当前归属的销售人员信息")
    private UserProfileRespVO sales;

    @Schema(description = "最近一次的跟进记录")
    private FollowupRespVO lastFollowUp;

    @Schema(description = "最近一次的销售线索")
    private SalesleadRespVO lastSalesLead;
}
