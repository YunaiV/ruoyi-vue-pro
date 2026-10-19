package cn.iocoder.yudao.module.oa.controller.admin.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 工作计划报表 Response VO")
@Data
public class OaPlanReportRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String userName;

    @Schema(description = "部门编号", example = "1")
    private Long deptId;

    @Schema(description = "部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "计划编号", example = "1024")
    private Long planId;

    @Schema(description = "计划状态", example = "1")
    private Integer status;

    @Schema(description = "标题", example = "本周工作计划")
    private String title;

    @Schema(description = "标签", example = "本周重点")
    private String label;

    @Schema(description = "计划内容", example = "下周完成各部门办公用品盘点差异核实，并按实际需求补充库存。")
    private String content;

    @Schema(description = "计划总结", example = "已完成本周计划的全部任务")
    private String summary;

    @Schema(description = "计划点评", example = "计划执行及时，后续继续跟进")
    private String comment;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "创建时间", type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

}
