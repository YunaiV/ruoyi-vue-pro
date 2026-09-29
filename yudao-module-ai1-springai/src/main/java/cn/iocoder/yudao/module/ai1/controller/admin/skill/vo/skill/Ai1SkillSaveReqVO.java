package cn.iocoder.yudao.module.ai1.controller.admin.skill.vo.skill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI1 SKILL 新增/修改 Request VO")
@Data
public class Ai1SkillSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Long id;

    @Schema(description = "名称，同时作为物化目录名，仅支持字母、数字、中划线", requiredMode = Schema.RequiredMode.REQUIRED, example = "pdf-reader")
    @NotEmpty(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "描述", example = "读取并总结 PDF 文件")
    @Size(max = 500, message = "描述长度不能超过 500 个字符")
    private String description;

    @Schema(description = "版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0")
    @NotEmpty(message = "版本不能为空")
    @Size(max = 20, message = "版本长度不能超过 20 个字符")
    private String version;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
