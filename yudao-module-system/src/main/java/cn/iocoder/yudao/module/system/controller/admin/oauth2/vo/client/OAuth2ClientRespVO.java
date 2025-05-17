package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - OAuth2 客户端 Response VO")
@Data
public class OAuth2ClientRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "客户端编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "tudou")
    private String clientId;

    @Schema(description = "客户端密钥", requiredMode = Schema.RequiredMode.REQUIRED, example = "fan")
    private String secret;

    @Schema(description = "应用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "土豆")
    private String name;

    @Schema(description = "应用图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.png")
    private String logo;

    @Schema(description = "应用描述", example = "我是一个应用")
    private String description;

    @Schema(description = "状态，参见 CommonStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "访问令牌的有效期", requiredMode = Schema.RequiredMode.REQUIRED, example = "8640")
    private Integer accessTokenValiditySeconds;

    @Schema(description = "刷新令牌的有效期", requiredMode = Schema.RequiredMode.REQUIRED, example = "8640000")
    private Integer refreshTokenValiditySeconds;

    @Schema(description = "可重定向的 URI 地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn")
    private List<String> redirectUris;

    @Schema(description = "授权类型，参见 OAuth2GrantTypeEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "password")
    private List<String> authorizedGrantTypes;

    @Schema(description = "授权范围", example = "user_info")
    private List<String> scopes;

    @Schema(description = "自动通过的授权范围", example = "user_info")
    private List<String> autoApproveScopes;

    @Schema(description = "权限", example = "system:user:query")
    private List<String> authorities;

    @Schema(description = "资源", example = "1024")
    private List<String> resourceIds;

    @Schema(description = "附加信息", example = "{yunai: true}")
    private String additionalInformation;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
