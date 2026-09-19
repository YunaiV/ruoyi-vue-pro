package cn.iocoder.yudao.module.oa.controller.admin.leave.vo;

import cn.iocoder.yudao.framework.dict.validation.InDict;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 请假申请新增/修改 Request VO")
@Data
public class OaLeaveApplySaveReqVO {

    @Schema(description = "申请编号", example = "1024")
    private Long id;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "个人事务请假申请")
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题不能超过 255 个字符")
    private String title;

    @Schema(description = "紧急程度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "紧急程度不能为空")
    @InDict(type = DictTypeConstants.APPLY_URGENCY, message = "紧急程度必须是 {value}")
    private Integer urgency;

    @Schema(description = "请假类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "请假类型不能为空")
    @InDict(type = DictTypeConstants.LEAVE_TYPE, message = "请假类型必须是 {value}")
    private Integer type;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789347600000")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, type = "integer", format = "int64", example = "1789380000000")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "申请原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "处理个人事务")
    @NotBlank(message = "申请原因不能为空")
    @Size(max = 5000, message = "申请原因不能超过 5000 个字符")
    private String reason;

    @Schema(description = "附件地址列表", example = "[\"https://example.com/file.pdf\"]")
    @Size(max = 5, message = "附件不能超过 5 个")
    private List<@NotBlank(message = "附件地址不能为空") @Size(max = 1024, message = "附件地址不能超过 1024 个字符") String> fileUrls;

    @AssertTrue(message = "结束时间不能早于开始时间")
    @JsonIgnore
    public boolean isTimeRangeValid() {
        // 缺失值交由字段上的必填校验处理
        return startTime == null || endTime == null || !endTime.isBefore(startTime);
    }

}
