package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 工作项工时记录 Response VO")
@Data
public class PmsWorkItemWorkLogRespVO {

    @Schema(description = "工时记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long workItemId;

    @Schema(description = "工作项标题", example = "完成登录接口")
    private String workItemName;

    @Schema(description = "实际投入工时，单位：小时", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Integer actualHours;

    @Schema(description = "本次登记后的剩余工时，单位：小时", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "8")
    private Integer remainingHours;

    @Schema(description = "工时说明", example = "完成登录接口联调")
    private String description;

    @Schema(description = "登记人用户编号", example = "1")
    private Long creatorUserId;

    @Schema(description = "登记人姓名", example = "芋道源码")
    private String creatorUserName;

    @Schema(description = "登记时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
