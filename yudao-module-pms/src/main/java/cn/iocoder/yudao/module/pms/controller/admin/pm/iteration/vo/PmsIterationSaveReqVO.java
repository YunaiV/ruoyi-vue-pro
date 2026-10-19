package cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 项目迭代新增/修改 Request VO")
@Data
public class PmsIterationSaveReqVO {

    @Schema(description = "迭代编号", example = "1024")
    private Long id;

    @Schema(description = "项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "迭代名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "第一期")
    @NotBlank(message = "迭代名称不能为空")
    @Size(max = 100, message = "迭代名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "负责人用户编号", example = "1")
    private Long ownerUserId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "迭代目标", example = "完成核心流程")
    @Size(max = 255, message = "迭代目标长度不能超过 255 个字符")
    private String target;

    @Schema(description = "迭代描述")
    private String description;

    @Schema(hidden = true)
    @AssertTrue(message = "迭代结束时间必须晚于开始时间，且开始、结束时间需要同时填写")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null && endTime == null
                || startTime != null && endTime != null && endTime.isAfter(startTime);
    }

}
