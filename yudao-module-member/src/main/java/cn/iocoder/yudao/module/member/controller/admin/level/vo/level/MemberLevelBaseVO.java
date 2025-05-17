package cn.iocoder.yudao.module.member.controller.admin.level.vo.level;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 会员等级 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberLevelBaseVO {

    @Schema(description = "等级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotBlank(message = "等级名称不能为空")
    private String name;

    @Schema(description = "升级经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "升级经验不能为空")
    @Positive(message = "升级经验必须大于 0")
    private Integer experience;

    @Schema(description = "等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "等级不能为空")
    @Positive(message = "等级必须大于 0")
    private Integer level;

    @Schema(description = "享受折扣", requiredMode = Schema.RequiredMode.REQUIRED, example = "98")
    @NotNull(message = "享受折扣不能为空")
    @Range(min = 0, max = 100, message = "享受折扣的范围为 0-100")
    private Integer discountPercent;

    @Schema(description = "等级图标", example = "https://www.iocoder.cn/yudao.jpg")
    @URL(message = "等级图标必须是 URL 格式")
    private String icon;

    @Schema(description = "等级背景图", example = "https://www.iocoder.cn/yudao.jpg")
    @URL(message = "等级背景图必须是 URL 格式")
    private String backgroundUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
