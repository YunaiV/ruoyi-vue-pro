package cn.iocoder.yudao.module.member.controller.admin.level.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 会员等级记录 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberLevelRecordBaseVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25923")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "等级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25985")
    @NotNull(message = "等级编号不能为空")
    private Long levelId;

    @Schema(description = "会员等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "会员等级不能为空")
    private Integer level;

    @Schema(description = "享受折扣", requiredMode = Schema.RequiredMode.REQUIRED, example = "13319")
    @NotNull(message = "享受折扣不能为空")
    private Integer discountPercent;

    @Schema(description = "升级经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "13319")
    @NotNull(message = "升级经验不能为空")
    private Integer experience;

    @Schema(description = "会员此时的经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "13319")
    @NotNull(message = "会员此时的经验不能为空")
    private Integer userExperience;

    @Schema(description = "备注", requiredMode = Schema.RequiredMode.REQUIRED, example = "推广需要")
    @NotNull(message = "备注不能为空")
    private String remark;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "升级为金牌会员")
    @NotNull(message = "描述不能为空")
    private String description;

}
