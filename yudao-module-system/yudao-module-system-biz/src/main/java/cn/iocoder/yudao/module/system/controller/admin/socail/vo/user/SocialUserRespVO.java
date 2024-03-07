package cn.iocoder.yudao.module.system.controller.admin.socail.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 社交用户 Response VO")
@Data
public class SocialUserRespVO {

    @Schema(description = "主键(自增策略)", requiredMode = Schema.RequiredMode.REQUIRED, example = "14569")
    private Long id;

    @Schema(description = "社交平台的类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer type;

    @Schema(description = "社交 openid", requiredMode = Schema.RequiredMode.REQUIRED, example = "666")
    private String openid;

    @Schema(description = "社交 token", requiredMode = Schema.RequiredMode.REQUIRED, example = "666")
    private String token;

    @Schema(description = "原始 Token 数据，一般是 JSON 格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private String rawTokenInfo;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String nickname;

    @Schema(description = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String avatar;

    @Schema(description = "原始用户数据，一般是 JSON 格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "{}")
    private String rawUserInfo;

    @Schema(description = "最后一次的认证 code", requiredMode = Schema.RequiredMode.REQUIRED, example = "666666")
    private String code;

    @Schema(description = "最后一次的认证 state", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String state;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
