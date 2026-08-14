package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - HRM 工资条备注 Request VO")
@Data
public class HrmSalarySlipRemarkReqVO {

    @Schema(description = "工资条编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "工资条编号不能为空")
    private Long id;

    @Schema(description = "备注", example = "本月绩效奖金已计入")
    @Size(max = 500, message = "备注不能超过 500 个字符")
    private String remark;

}
