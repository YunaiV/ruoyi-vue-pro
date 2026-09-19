package cn.iocoder.yudao.module.oa.controller.admin.overtime.vo;

import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 加班申请新增/修改 Request VO")
@Data
public class OaOvertimeApplySaveReqVO {

    @Schema(description = "申请编号", example = "1024")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "项目上线加班申请")
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题不能超过 255 个字符")
    private String title;

    @Schema(description = "紧急程度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "紧急程度不能为空")
    @InDict(type = DictTypeConstants.APPLY_URGENCY, message = "紧急程度必须是 {value}")
    private Integer urgency;

    @Schema(description = "加班类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "加班类型不能为空")
    @InDict(type = DictTypeConstants.OVERTIME_TYPE, message = "加班类型必须是 {value}")
    private Integer type;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "申请原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "完成项目上线前的联调工作")
    @NotBlank(message = "申请原因不能为空")
    @Size(max = 5000, message = "申请原因不能超过 5000 个字符")
    private String reason;

    @AssertTrue(message = "结束时间不能早于开始时间")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || !endTime.isBefore(startTime);
    }

}
