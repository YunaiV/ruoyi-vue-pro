package cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - MES 工艺路线新增/修改 Request VO")
@Data
public class MesProRouteSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "工艺路线编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ROUTE001")
    @NotBlank(message = "工艺路线编码不能为空")
    private String code;

    @Schema(description = "工艺路线名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "标准路线")
    @NotBlank(message = "工艺路线名称不能为空")
    private String name;

    @Schema(description = "工艺路线说明")
    private String description;

    @Schema(description = "备注")
    private String remark;

}
