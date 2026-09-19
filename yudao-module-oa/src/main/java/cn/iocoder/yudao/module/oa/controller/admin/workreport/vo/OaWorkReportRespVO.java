package cn.iocoder.yudao.module.oa.controller.admin.workreport.vo;

import cn.iocoder.yudao.module.oa.dal.dataobject.workreport.OaWorkReportDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 工作汇报 Response VO")
@Data
public class OaWorkReportRespVO {

    @Schema(description = "汇报编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "汇报单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "HB202609140001")
    private String no;

    @Schema(description = "汇报类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "汇报状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "汇报标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-14 工作日报")
    private String title;

    @Schema(description = "汇报周期标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-14")
    private String periodKey;

    @Schema(description = "周期开始时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime startTime;

    @Schema(description = "周期结束时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    private LocalDateTime endTime;

    @Schema(description = "工作总结", example = "本周已完成库存盘点并整理差异清单")
    private String summary;

    @Schema(description = "工作计划补充说明", example = "下周完成盘点差异核实")
    private String plan;

    @Schema(description = "问题与协调事项", example = "需协调仓库复核盘点差异")
    private String problem;

    @Schema(description = "已完成工作项")
    private List<OaWorkReportDO.WorkItem> workItems;

    @Schema(description = "工作计划项")
    private List<OaWorkReportDO.PlanItem> planItems;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    private List<String> fileUrls;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    private String remark;

    @Schema(description = "汇报人用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "汇报人昵称", example = "张三")
    private String userName;

    @Schema(description = "汇报人部门编号", example = "1024")
    private Long deptId;

    @Schema(description = "汇报人部门名称", example = "研发部")
    private String deptName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    private LocalDateTime updateTime;

}
