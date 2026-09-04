package cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - PMS 项目分组新增/修改 Request VO")
@Data
public class PmsProjectGroupSaveReqVO {

    @Schema(description = "项目分组编号", example = "1024")
    private Long id;

    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "重点项目")
    @NotBlank(message = "分组名称不能为空")
    @Size(max = 100, message = "分组名称不能超过 100 个字符")
    private String name;

}
