package cn.iocoder.yudao.module.oa.controller.admin.plan.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.oa.enums.plan.OaPlanStatusEnum;
import cn.iocoder.yudao.module.oa.enums.plan.OaPlanTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OA 工作计划新增/修改 Request VO")
@Data
public class OaPlanSaveReqVO {

    @Schema(description = "计划编号", example = "1024")
    private Long id;

    @Schema(description = "计划类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计划类型不能为空")
    @InEnum(value = OaPlanTypeEnum.class, message = "计划类型必须是 {value}")
    private Integer type;

    @Schema(description = "计划状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计划状态不能为空")
    @InEnum(value = OaPlanStatusEnum.class, message = "计划状态必须是 {value}")
    private Integer status;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "本周工作计划")
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过 50 个字符")
    private String title;

    @Schema(description = "标签", example = "重点")
    @Size(max = 255, message = "标签长度不能超过 255 个字符")
    private String label;

    @Schema(description = "计划内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "下周完成各部门办公用品盘点差异核实，并按实际需求补充库存。")
    @NotBlank(message = "计划内容不能为空")
    @Size(min = 20, message = "计划内容不能少于 20 个字符")
    private String content;

    @Schema(description = "计划总结", example = "已完成本周计划的全部任务")
    private String summary;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    @Size(max = 1, message = "附件最多上传 1 个")
    private List<@NotBlank(message = "附件地址不能为空") @Size(max = 512, message = "附件地址长度不能超过 512 个字符") String> fileUrls;

    @JsonIgnore
    @AssertTrue(message = "计划结束时间必须晚于开始时间")
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || startTime.isBefore(endTime);
    }

    @JsonIgnore
    @AssertTrue(message = "计划总结不能少于 20 个字符")
    public boolean isSummaryValid() {
        return summary == null || summary.isEmpty() || summary.trim().length() >= 20;
    }

}
