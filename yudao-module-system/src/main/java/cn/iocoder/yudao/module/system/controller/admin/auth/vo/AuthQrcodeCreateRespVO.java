package cn.iocoder.yudao.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

/**
 * 扫码登录二维码 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - 扫码登录二维码 Response VO")
@Data
public class AuthQrcodeCreateRespVO {

    @Schema(description = "二维码编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String qrcodeId;

    @Schema(description = "电脑领取凭证，仅保存在生成二维码的页面", requiredMode = Schema.RequiredMode.REQUIRED)
    @ToString.Exclude
    private String secret;

    @Schema(description = "二维码内容，不含领取凭证", requiredMode = Schema.RequiredMode.REQUIRED)
    private String qrContent;

    @Schema(description = "有效期，单位：秒", requiredMode = Schema.RequiredMode.REQUIRED, example = "180")
    private Integer expiresIn;

}
