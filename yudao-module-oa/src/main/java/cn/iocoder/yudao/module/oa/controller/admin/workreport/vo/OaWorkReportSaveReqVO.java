package cn.iocoder.yudao.module.oa.controller.admin.workreport.vo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.workreport.OaWorkReportTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 工作汇报新增/修改 Request VO")
@Data
public class OaWorkReportSaveReqVO {

    @Schema(description = "汇报编号", example = "1024")
    private Long id;

    @Schema(description = "汇报类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "汇报类型不能为空")
    @InEnum(value = OaWorkReportTypeEnum.class, message = "汇报类型必须是 {value}")
    private Integer type;

    @Schema(description = "汇报标题", example = "2026-09-10 工作日报")
    @Size(max = 255, message = "汇报标题不能超过 255 个字符")
    private String title;

    @Schema(description = "周期开始时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "周期开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "周期结束时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    @NotNull(message = "周期结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "工作总结", example = "本周已完成库存盘点并整理差异清单")
    @Size(max = 5000, message = "工作总结不能超过 5000 个字符")
    private String summary;

    @Schema(description = "工作计划补充说明", example = "下周完成盘点差异核实")
    @Size(max = 5000, message = "工作计划补充说明不能超过 5000 个字符")
    private String plan;

    @Schema(description = "问题与协调事项", example = "需协调仓库复核盘点差异")
    @Size(max = 5000, message = "问题与协调事项不能超过 5000 个字符")
    private String problem;

    @Schema(description = "已完成工作项")
    @Valid
    @Size(max = 100, message = "已完成工作项不能超过 100 项")
    private List<@NotNull(message = "已完成工作项不能为空") WorkItem> workItems;

    @Schema(description = "工作计划项")
    @Valid
    @Size(max = 100, message = "工作计划项不能超过 100 项")
    private List<@NotNull(message = "工作计划项不能为空") PlanItem> planItems;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    @Size(max = 10, message = "附件不能超过 10 个")
    private List<@NotBlank(message = "附件地址不能为空") @Size(max = 1024, message = "附件地址不能超过 1024 个字符") String> fileUrls;

    @Schema(description = "备注", example = "请提前联系行政部确认")
    @Size(max = 1000, message = "备注不能超过 1000 个字符")
    private String remark;

    /**
     * 校验汇报周期的起止时间顺序，空值由字段必填注解校验
     *
     * @return 时间顺序是否有效
     */
    @JsonIgnore
    @AssertTrue(message = "周期结束时间不能早于开始时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !startTime.isAfter(endTime);
    }

    /**
     * 保存时可填写文字总结、计划说明或工作明细
     *
     * @return 是否填写了汇报内容
     */
    @JsonIgnore
    @AssertTrue(message = "请填写工作总结、计划说明或工作明细")
    public boolean isContentValid() {
        return StrUtil.isNotBlank(summary) || StrUtil.isNotBlank(plan)
                || CollUtil.isNotEmpty(workItems) || CollUtil.isNotEmpty(planItems);
    }

    @Schema(description = "管理后台 - 工作汇报已完成工作项 Request VO")
    @Data
    public static class WorkItem {

        @Schema(description = "工作内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成办公用品库存盘点")
        @NotBlank(message = "工作内容不能为空")
        @Size(max = 1000, message = "内容不能超过 1000 个字符")
        private String content;

        @Schema(description = "完成进度", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "完成进度不能为空")
        @Min(value = 0, message = "完成进度不能小于 0")
        @Max(value = 100, message = "完成进度不能大于 100")
        private Integer progress;

    }

    @Schema(description = "管理后台 - 工作汇报工作计划项 Request VO")
    @Data
    public static class PlanItem {

        @Schema(description = "计划内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "下周完成各部门办公用品盘点差异核实，并按实际需求补充库存。")
        @NotBlank(message = "计划内容不能为空")
        @Size(max = 1000, message = "内容不能超过 1000 个字符")
        private String content;

    }

}
