package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 项目 Response VO")
@Data
public class PmsProjectRespVO {

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "官网重构")
    private String name;

    @Schema(description = "项目状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "项目类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "项目优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer level;

    @Schema(description = "项目描述", example = "完成官网前后端重构")
    private String description;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean openStatus;

    @Schema(description = "项目图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "ep:folder")
    private String icon;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "截止时间")
    private LocalDateTime endTime;

    @Schema(description = "归档时间")
    private LocalDateTime archiveTime;

    @Schema(description = "移入回收站时间")
    private LocalDateTime recycleTime;

    @Schema(description = "最近访问时间")
    private LocalDateTime accessTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "创建人编号", example = "1")
    private String creator;

    @Schema(description = "创建人姓名", example = "芋道源码")
    private String creatorName;

    @Schema(description = "项目管理员姓名列表")
    private List<String> adminNames;

    @Schema(description = "项目成员数量", example = "5")
    private Integer memberCount;

    @Schema(description = "未开始工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long pendingWorkItemCount;

    @Schema(description = "进行中工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long processingWorkItemCount;

    @Schema(description = "已完成工作项数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Long completedWorkItemCount;

    @Schema(description = "当前用户是否为项目管理员", example = "true")
    private Boolean adminStatus;

    @Schema(description = "当前用户是否拥有项目所有者权限", example = "true")
    private Boolean ownerStatus;

    @Schema(description = "当前用户是否为项目成员", example = "true")
    private Boolean memberStatus;

    @Schema(description = "当前用户是否可以主动退出项目", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean exitStatus;

    @Schema(description = "当前用户是否可以编辑项目业务数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean writeStatus;

    @Schema(description = "当前用户是否已收藏", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean favoriteStatus;

}
