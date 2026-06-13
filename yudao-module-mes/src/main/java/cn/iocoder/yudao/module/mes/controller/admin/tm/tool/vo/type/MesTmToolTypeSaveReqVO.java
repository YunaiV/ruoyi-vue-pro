package cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo.type;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - MES 工具类型新增/修改 Request VO")
@Data
public class MesTmToolTypeSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "类型编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "TT-001")
    @NotEmpty(message = "类型编码不能为空")
    @Size(max = 64, message = "类型编码长度不能超过 64 个字符")
    private String code;

    @Schema(description = "类型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "铣刀")
    @NotEmpty(message = "类型名称不能为空")
    @Size(max = 255, message = "类型名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "是否编码管理", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否编码管理不能为空")
    private Boolean codeFlag;

    @Schema(description = "保养维护类型", example = "1")
    private Integer maintenType;

    @Schema(description = "保养周期", example = "30")
    private Integer maintenPeriod;

    @Schema(description = "备注", example = "备注")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

}