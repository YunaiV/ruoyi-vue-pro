package cn.iocoder.yudao.module.hrm.controller.admin.portal.salary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - HRM 员工端未读工资条概况 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrmPortalSalarySlipUnreadSummaryRespVO {

    @Schema(description = "未读工资条数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long unreadCount;

    @Schema(description = "最新未读工资条提醒")
    private String reminder;

}
