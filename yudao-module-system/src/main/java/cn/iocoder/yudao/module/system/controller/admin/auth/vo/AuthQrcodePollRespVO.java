package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * 电脑领取扫码登录结果 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 扫码登录结果 Response VO")
@Data
public class AuthQrcodePollRespVO {

    @Schema(description = "扫码状态，参见 AuthQrcodeStatusEnum", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "电脑独立登录结果，仅确认成功时返回")
    @ToString.Exclude
    private AuthLoginRespVO login;

}
