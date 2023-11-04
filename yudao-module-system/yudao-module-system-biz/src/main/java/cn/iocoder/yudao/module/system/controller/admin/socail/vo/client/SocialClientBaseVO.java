package cn.iocoder.yudao.module.system.controller.admin.socail.vo.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 社交客户端 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class SocialClientBaseVO {

    @Schema(description = "应用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao商城")
    @NotNull(message = "应用名不能为空")
    private String name;

    @Schema(description = "社交平台的类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "31")
    @NotNull(message = "社交平台的类型不能为空")
    private Integer socialType;

    @Schema(description = "用户类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @Schema(description = "客户端编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "wwd411c69a39ad2e54")
    @NotNull(message = "客户端编号不能为空")
    private String clientId;

    @Schema(description = "客户端密钥", requiredMode = Schema.RequiredMode.REQUIRED, example = "1wTb7hYxnpT2TUbIeHGXGo7T0odav1ic10mLdyyATOw")
    @NotNull(message = "客户端密钥不能为空")
    private String clientSecret;

    @Schema(description = "授权方的网页应用编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000045")
    private String agentId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
