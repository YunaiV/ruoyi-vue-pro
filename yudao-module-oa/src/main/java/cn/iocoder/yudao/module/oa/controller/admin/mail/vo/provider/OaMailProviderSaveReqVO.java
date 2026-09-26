package cn.iocoder.yudao.module.oa.controller.admin.mail.vo.provider;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Schema(description = "管理后台 - 企业邮箱服务配置新增/修改 Request VO")
public class OaMailProviderSaveReqVO {

    @Schema(description = "服务配置编号", example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "企业邮箱")
    @NotBlank(message = "名称不能为空")
    @Size(max = 255, message = "名称长度不能超过 {max} 个字符")
    private String name;

    @Schema(description = "IMAP 连接配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "IMAP 连接配置不能为空")
    @Valid
    private ConnectionConfig imap;

    @Schema(description = "SMTP 连接配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "SMTP 连接配置不能为空")
    @Valid
    private ConnectionConfig smtp;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "管理后台 - 邮箱连接配置 Request VO")
    @Data
    public static class ConnectionConfig {

        @Schema(description = "服务器域名", requiredMode = Schema.RequiredMode.REQUIRED, example = "imap.example.com")
        @NotBlank(message = "服务器域名不能为空")
        @Size(max = 253, message = "服务器域名长度不能超过 253 个字符")
        private String host;

        /** 校验服务器主机名或 IP，不包含协议、路径和端口 */
        @AssertTrue(message = "请输入有效的服务器域名或 IP 地址")
        @JsonIgnore
        public boolean isHostValid() {
            if (StrUtil.isEmpty(host)) {
                return true; // 空值由必填校验处理
            }
            if (host.contains(":")) {
                return Validator.isIpv6(host);
            }
            if (host.matches("\\d+(?:\\.\\d+){3}")) {
                return host.matches("(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}");
            }
            return host.matches("(?i)[a-z\\d](?:[a-z\\d-]{0,61}[a-z\\d])?(?:\\.[a-z\\d](?:[a-z\\d-]{0,61}[a-z\\d])?)*\\.?");
        }

        @Schema(description = "服务器端口", requiredMode = Schema.RequiredMode.REQUIRED, example = "993")
        @NotNull(message = "服务器端口不能为空")
        @Min(value = 1, message = "服务器端口不能小于 1")
        @Max(value = 65535, message = "服务器端口不能大于 65535")
        private Integer port;

        @Schema(description = "是否开启 SSL", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull(message = "是否开启 SSL 不能为空")
        private Boolean sslEnable;

        @Schema(description = "是否开启 STARTTLS", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
        @NotNull(message = "是否开启 STARTTLS 不能为空")
        private Boolean starttlsEnable;

        /**
         * 校验邮箱连接加密方式
         *
         * @return 是否仅启用一种加密方式，空值由字段必填注解校验
         */
        @AssertTrue(message = "请选择 SSL 或 STARTTLS，且不能同时开启")
        @JsonIgnore
        public boolean isEncryptionValid() {
            return sslEnable == null || !sslEnable.equals(starttlsEnable);
        }

        /**
         * 校验服务器主机名或 IP，不包含协议、路径和端口
         */
        @AssertTrue(message = "请输入有效的服务器域名或 IP 地址")
        @JsonIgnore
        public boolean isHostValid() {
            if (StrUtil.isEmpty(host)) {
                return true;
            }
            if (host.contains(":")) {
                return Validator.isIpv6(host);
            }
            if (host.matches("\\d+(?:\\.\\d+){3}")) {
                return host.matches("(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}");
            }
            return host.matches("(?i)[a-z\\d](?:[a-z\\d-]{0,61}[a-z\\d])?(?:\\.[a-z\\d](?:[a-z\\d-]{0,61}[a-z\\d])?)*\\.?");
        }

    }

}
