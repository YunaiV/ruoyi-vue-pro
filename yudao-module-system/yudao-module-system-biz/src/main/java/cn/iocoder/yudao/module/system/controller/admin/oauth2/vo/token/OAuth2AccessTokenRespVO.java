package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.token;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 访问令牌 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2AccessTokenRespVO {

    @Schema(description = "编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "访问令牌", required = true, example = "tudou")
    private String accessToken;

    @Schema(description = "刷新令牌", required = true, example = "nice")
    private String refreshToken;

    @Schema(description = "用户编号", required = true, example = "666")
    private Long userId;

    @Schema(description = "用户类型,参见 UserTypeEnum 枚举", required = true, example = "2")
    private Integer userType;

    @Schema(description = "客户端编号", required = true, example = "2")
    private String clientId;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "过期时间", required = true)
    private LocalDateTime expiresTime;

}
