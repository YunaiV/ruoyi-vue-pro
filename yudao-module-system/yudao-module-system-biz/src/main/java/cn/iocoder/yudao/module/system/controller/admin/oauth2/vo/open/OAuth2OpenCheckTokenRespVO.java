package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.open;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "管理后台 - 【开放接口】校验令牌 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2OpenCheckTokenRespVO {

    @Schema(description = "用户编号", required = true, example = "666")
    @JsonProperty("user_id")
    private Long userId;
    @Schema(description = "用户类型,参见 UserTypeEnum 枚举", required = true, example = "2")
    @JsonProperty("user_type")
    private Integer userType;
    @Schema(description = "租户编号", required = true, example = "1024")
    @JsonProperty("tenant_id")
    private Long tenantId;

    @Schema(description = "客户端编号", required = true, example = "car")
    @JsonProperty("client_id")
    private String clientId;
    @Schema(description = "授权范围", required = true, example = "user_info")
    private List<String> scopes;

    @Schema(description = "访问令牌", required = true, example = "tudou")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "过期时间,时间戳 / 1000，即单位：秒", required = true, example = "1593092157")
    private Long exp;

}
