package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.personalnote;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工个人备忘创建 Request VO")
@Data
public class HrmEmployeePersonalNoteCreateReqVO {

    @Schema(description = "备忘内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "跟进转正材料")
    @NotBlank(message = "备忘内容不能为空")
    @Size(max = 1024, message = "备忘内容不能超过 1024 个字符")
    private String content;

    @Schema(description = "提醒时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "提醒时间不能为空")
    private LocalDateTime reminderTime;

}
