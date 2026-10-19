package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.label.PmsWorkItemLabelRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - PMS 工作项 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PmsWorkItemRespVO {

    @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long projectId;

    @Schema(description = "工作项类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @ExcelProperty("类型")
    private Integer type;

    @Schema(description = "项目内工作项序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    @ExcelProperty("编号")
    private Integer serialNumber;

    @Schema(description = "工作项标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成登录页")
    @ExcelProperty("标题")
    private String name;

    @Schema(description = "工作项描述")
    @ExcelProperty("描述")
    private String description;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("优先级")
    private Integer priority;

    @Schema(description = "负责人用户编号", example = "1024")
    private Long assigneeUserId;

    @Schema(description = "负责人姓名", example = "芋道源码")
    @ExcelProperty("负责人")
    private String assigneeUserName;

    @Schema(description = "参与人用户编号列表")
    private List<Long> memberUserIds;

    @Schema(description = "参与人姓名列表")
    private List<String> memberUserNames;

    @Schema(description = "看板状态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long statusId;

    @Schema(description = "看板状态名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "未开始")
    @ExcelProperty("状态")
    private String statusName;

    @Schema(description = "语义状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "生命周期状态，1 正常，2 已归档，3 回收站", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer lifecycleStatus;

    @Schema(description = "归档时间")
    private LocalDateTime archiveTime;

    @Schema(description = "移入回收站时间")
    private LocalDateTime recycleTime;

    @Schema(description = "所属迭代编号", example = "1024")
    private Long iterationId;

    @Schema(description = "所属迭代名称", example = "第一期")
    @ExcelProperty("所属迭代")
    private String iterationName;

    @Schema(description = "父工作项编号", example = "1024")
    private Long parentId;

    @Schema(description = "关联需求编号", example = "1024")
    private Long relatedRequirementId;

    @Schema(description = "关联需求标题", example = "用户登录")
    private String relatedRequirementName;

    @Schema(description = "缺陷类型", example = "1")
    private Integer defectType;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "截止时间")
    @ExcelProperty("截止时间")
    private LocalDateTime endTime;

    @Schema(description = "预估工时", example = "8")
    @ExcelProperty("预估工时")
    private Integer estimatedHours;

    @Schema(description = "完成进度", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    @ExcelProperty("完成进度")
    private Integer progress;

    @Schema(description = "附件地址列表")
    private List<String> fileUrls;

    @Schema(description = "标签编号列表")
    private List<Long> labelIds;

    @Schema(description = "标签列表")
    private List<PmsWorkItemLabelRespVO> labels;

    @Schema(description = "看板内显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
