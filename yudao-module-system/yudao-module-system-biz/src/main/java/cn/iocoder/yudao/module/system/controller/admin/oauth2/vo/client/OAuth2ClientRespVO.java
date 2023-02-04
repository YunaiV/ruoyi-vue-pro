package cn.iocoder.yudao.module.system.controller.admin.oauth2.vo.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - OAuth2 客户端 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OAuth2ClientRespVO extends OAuth2ClientBaseVO {

    @Schema(description = "编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}
