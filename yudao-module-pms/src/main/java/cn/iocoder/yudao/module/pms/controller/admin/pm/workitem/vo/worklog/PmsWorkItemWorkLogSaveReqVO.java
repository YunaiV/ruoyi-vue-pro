package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.worklog;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 工作项工时记录新增/修改 Request VO")
@Data
public class PmsWorkItemWorkLogSaveReqVO {

    @Schema(description = "工时记录编号", example = "1024")
    private Long id;

    @Schema(description = "工作项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "工作项编号不能为空")
    private Long workItemId;

    @Schema(description = "实际投入工时，单位：小时", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    @NotNull(message = "实际投入工时不能为空")
    @Min(value = 1, message = "实际投入工时必须大于 0")
    private Integer actualHours;

    @Schema(description = "本次登记后的剩余工时，单位：小时", example = "8")
    @Min(value = 0, message = "剩余工时不能小于 0")
    private Integer remainingHours;

    @Schema(description = "工时说明", example = "完成登录接口联调")
    @Size(max = 500, message = "工时说明不能超过 500 个字符")
    private String description;

}
