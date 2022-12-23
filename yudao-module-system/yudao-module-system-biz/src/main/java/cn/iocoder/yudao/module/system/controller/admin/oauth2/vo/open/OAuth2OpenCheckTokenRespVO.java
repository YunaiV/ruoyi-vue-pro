package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.open;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel("管理后台 - 【开放接口】校验令牌 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2OpenCheckTokenRespVO {

    @ApiModelProperty(value = "用户编号", required = true, example = "666")
    @JsonProperty("user_id")
    private Long userId;
    @ApiModelProperty(value = "用户类型", required = true, example = "2", notes = "参见 UserTypeEnum 枚举")
    @JsonProperty("user_type")
    private Integer userType;
    @ApiModelProperty(value = "租户编号", required = true, example = "1024")
    @JsonProperty("tenant_id")
    private Long tenantId;

    @ApiModelProperty(value = "客户端编号", required = true, example = "car")
    @JsonProperty("client_id")
    private String clientId;
    @ApiModelProperty(value = "授权范围", required = true, example = "user_info")
    private List<String> scopes;

    @ApiModelProperty(value = "访问令牌", required = true, example = "tudou")
    @JsonProperty("access_token")
    private String accessToken;

    @ApiModelProperty(value = "过期时间", required = true, example = "1593092157", notes = "时间戳 / 1000，即单位：秒")
    private Long exp;

}
