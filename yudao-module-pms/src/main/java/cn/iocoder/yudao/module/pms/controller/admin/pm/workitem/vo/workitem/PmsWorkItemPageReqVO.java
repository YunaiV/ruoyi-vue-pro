package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemLifecycleStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemPriorityEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - PMS 工作项分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsWorkItemPageReqVO extends PageParam {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "工作项类型", example = "3")
    @InEnum(PmsWorkItemTypeEnum.class)
    private Integer type;

    @Schema(description = "工作项类型列表", example = "[2, 3, 4]")
    private List<Integer> types;

    @Schema(description = "工作项标题", example = "登录")
    private String name;

    @Schema(description = "语义状态", example = "1")
    @InEnum(PmsWorkItemStatusTypeEnum.class)
    private Integer status;

    @Schema(description = "语义状态列表", example = "[1, 2]")
    private List<Integer> statuses;

    @Schema(description = "优先级", example = "2")
    @InEnum(PmsWorkItemPriorityEnum.class)
    private Integer priority;

    @Schema(description = "优先级列表", example = "[2, 3]")
    private List<Integer> priorities;

    @Schema(description = "生命周期状态，1 正常，2 已归档，3 回收站", example = "1")
    @InEnum(PmsWorkItemLifecycleStatusEnum.class)
    private Integer lifecycleStatus;

    @Schema(description = "看板状态编号", example = "1024")
    private Long statusId;

    @Schema(description = "迭代编号", example = "1024")
    private Long iterationId;

    @Schema(description = "迭代编号列表", example = "[1024, 2048]")
    private List<Long> iterationIds;

    @Schema(description = "排除的迭代编号列表", example = "[1024, 2048]")
    private List<Long> excludedIterationIds;

    @Schema(description = "父工作项编号", example = "1024")
    private Long parentId;

    @Schema(description = "标签编号列表", example = "[1024, 2048]")
    private List<Long> labelIds;

    @Schema(description = "是否只查询未规划工作项", example = "true")
    private Boolean unplannedOnly;

    @Schema(description = "是否用于待规划视图；为 true 时只查询一级工作项并按规划顺序返回", example = "true")
    private Boolean planningOnly;

    @Schema(description = "是否只查询一级工作项", example = "true")
    private Boolean rootOnly;

    @Schema(description = "负责人用户编号", example = "1024")
    private Long assigneeUserId;

    @Schema(description = "负责人用户编号列表", example = "[1024, 2048]")
    private List<Long> assigneeUserIds;

}
