package cn.iocoder.yudao.module.member.controller.admin.signin.vo.config;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 签到规则 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberSignInConfigBaseVO {

    @Schema(description = "签到第 x 天", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "签到天数不能为空")
    private Integer day;

    @Schema(description = "奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "奖励积分不能为空")
    @PositiveOrZero(message = "奖励积分不能小于 0")
    private Integer point;

    @Schema(description = "奖励经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "奖励经验不能为空")
    @PositiveOrZero(message = "奖励经验不能小于 0")
    private Integer experience;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @AssertTrue(message = "签到奖励积分和经验不能同时为空")
    @JsonIgnore
    public boolean isConfigAward() {
        return ObjUtil.notEqual(point, 0) || ObjUtil.notEqual(experience, 0);
    }
}
