package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(title = "用户 APP - 登录 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthLoginRespVO {

    @Schema(title = "用户编号", required = true, example = "1024")
    private Long userId;

    @Schema(title = "访问令牌", required = true, example = "happy")
    private String accessToken;

    @Schema(title = "刷新令牌", required = true, example = "nice")
    private String refreshToken;

    @Schema(title = "过期时间", required = true)
    private LocalDateTime expiresTime;

}
