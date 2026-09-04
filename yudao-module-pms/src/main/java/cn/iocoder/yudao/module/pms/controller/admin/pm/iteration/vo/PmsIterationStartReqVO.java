package cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - PMS 项目迭代开始 Request VO")
@Data
public class PmsIterationStartReqVO {

    @Schema(description = "迭代编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "迭代编号不能为空")
    private Long id;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "迭代开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "迭代结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(hidden = true)
    @AssertTrue(message = "迭代结束时间必须晚于开始时间")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        return startTime == null || endTime == null || endTime.isAfter(startTime);
    }

}
