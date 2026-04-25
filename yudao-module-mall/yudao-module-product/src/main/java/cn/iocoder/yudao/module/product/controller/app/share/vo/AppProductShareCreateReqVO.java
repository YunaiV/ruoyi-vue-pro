package cn.iocoder.yudao.module.product.controller.app.share.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Schema(description = "用户 App - 创建分享链接 Request VO")
@Data
public class AppProductShareCreateReqVO {

    @Schema(description = "资源类型：product / order / contract / blockchain_proof",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "product")
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    @Schema(description = "资源 ID（商品 SPU 编号或订单号）",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotBlank(message = "资源编号不能为空")
    private String resourceId;

    @Schema(description = "目标平台：wechat / weibo / general", example = "wechat")
    private String platform;

    @Schema(description = "权限配置，可含：view, download, sign, verify, maxViews, watermark, passwordProtected, expiresIn")
    private Map<String, Object> permissions;

}
