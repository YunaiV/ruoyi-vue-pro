package cn.iocoder.yudao.module.member.controller.admin.signin.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 积分签到规则 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class MemberSignInConfigBaseVO {

    // TODO @xiaqing：必须的参数校验注解

    @Schema(description = "签到第 x 天", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    private Integer day;

    @Schema(description = "签到天数对应分数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer point;

    // TODO @xiaqing：enable
    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Boolean isEnable;

}
