package cn.iocoder.yudao.module.ai.controller.admin.skill.vo.skill;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - AI 技能新增/修改 Request VO")
@Data
public class AiSkillSaveReqVO {

    @Schema(description = "技能编号", example = "1024")
    private Long id;

    @Schema(description = "技能名称，创建后不可修改", requiredMode = Schema.RequiredMode.REQUIRED, example = "order-analysis")
    @NotEmpty(message = "技能名称不能为空")
    @Size(max = 64, message = "技能名称长度不能超过 64 个字符")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "技能名称仅支持小写字母、数字、中划线，且不能以中划线开头或结尾")
    private String name;

    @Schema(description = "技能描述，仅创建时写入 SKILL.md；之后以 SKILL.md 头信息为准", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "订单统计口径与报表格式")
    @NotBlank(message = "技能描述不能为空")
    @Size(max = 1024, message = "技能描述长度不能超过 1024 个字符")
    private String description;

    @Schema(description = "版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0.0")
    @NotEmpty(message = "版本不能为空")
    @Size(max = 32, message = "版本长度不能超过 32 个字符")
    private String version;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "备注", example = "订单相关")
    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;

}
