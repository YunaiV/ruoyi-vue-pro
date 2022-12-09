package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.open;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(title = "管理后台 - 【开放接口】校验令牌 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2OpenCheckTokenRespVO {

    @Schema(title = "用户编号", required = true, example = "666")
    @JsonProperty("user_id")
    private Long userId;
    @Schema(title = "用户类型", required = true, example = "2", description = "参见 UserTypeEnum 枚举")
    @JsonProperty("user_type")
    private Integer userType;
    @Schema(title = "租户编号", required = true, example = "1024")
    @JsonProperty("tenant_id")
    private Long tenantId;

    @Schema(title = "客户端编号", required = true, example = "car")
    @JsonProperty("client_id")
    private String clientId;
    @Schema(title = "授权范围", required = true, example = "user_info")
    private List<String> scopes;

    @Schema(title = "访问令牌", required = true, example = "tudou")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(title = "过期时间", required = true, example = "1593092157", description = "时间戳 / 1000，即单位：秒")
    private Long exp;

}
