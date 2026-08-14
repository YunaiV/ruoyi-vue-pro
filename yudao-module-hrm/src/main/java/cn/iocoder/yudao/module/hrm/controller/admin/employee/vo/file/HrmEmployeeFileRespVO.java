package cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - HRM 员工材料附件 Response VO")
@Data
public class HrmEmployeeFileRespVO {

    @Schema(description = "附件编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "员工编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long employeeId;

    @Schema(description = "附件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "11")
    private Integer type;

    @Schema(description = "附件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
