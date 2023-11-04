package cn.iocoder.yudao.module.system.controller.admin.socail.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 社交用户 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SocialUserBaseVO {

    @Schema(description = "社交平台的类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @NotNull(message = "社交平台的类型不能为空")
    private Integer type;

    @Schema(description = "社交 openid", requiredMode = Schema.RequiredMode.REQUIRED, example = "666")
    @NotNull(message = "社交 openid不能为空")
    private String openid;

    @Schema(description = "社交 token", requiredMode = Schema.RequiredMode.REQUIRED, example = "666")
    private String token;

    @Schema(description = "原始 Token 数据，一般是 JSON 格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private String rawTokenInfo;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotNull(message = "用户昵称不能为空")
    private String nickname;

    @Schema(description = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String avatar;

    @Schema(description = "原始用户数据，一般是 JSON 格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private String rawUserInfo;

    @Schema(description = "最后一次的认证 code", requiredMode = Schema.RequiredMode.REQUIRED, example = "666666")
    private String code;

    @Schema(description = "最后一次的认证 state", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String state;

}
