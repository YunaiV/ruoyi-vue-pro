package cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemPriorityEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - PMS 工作台分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsWorkbenchPageReqVO extends PageParam {

    @Schema(description = "项目编号", example = "1024")
    private Long projectId;

    @Schema(description = "工作项类型", example = "3")
    @InEnum(PmsWorkItemTypeEnum.class)
    private Integer type;

    @Schema(description = "名称", example = "登录")
    private String name;

    @Schema(description = "状态，工作项为语义状态，迭代为迭代状态", example = "2")
    private Integer status;

    @Schema(description = "优先级", example = "2")
    @InEnum(PmsWorkItemPriorityEnum.class)
    private Integer priority;

    @Schema(description = "迭代编号", example = "1024")
    private Long iterationId;

    @Schema(description = "截止时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

}
