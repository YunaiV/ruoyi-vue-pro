package cn.iocoder.yudao.module.member.controller.admin.level.vo.experience;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 会员经验记录 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberExperienceRecordBaseVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3638")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "业务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12164")
    @NotNull(message = "业务编号不能为空")
    private String bizId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "业务类型不能为空")
    private Integer bizType;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "增加经验")
    @NotNull(message = "标题不能为空")
    private String title;

    @Schema(description = "经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "经验不能为空")
    private Integer experience;

    @Schema(description = "变更后的经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    @NotNull(message = "变更后的经验不能为空")
    private Integer totalExperience;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "下单增加 100 经验")
    @NotNull(message = "描述不能为空")
    private String description;

}
