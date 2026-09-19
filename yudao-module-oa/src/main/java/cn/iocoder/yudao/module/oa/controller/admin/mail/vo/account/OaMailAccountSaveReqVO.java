package cn.iocoder.yudao.module.oa.controller.admin.mail.vo.account;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.*;

@Data
@Schema(description = "管理后台 - 企业邮箱账号新增/修改 Request VO")
public class OaMailAccountSaveReqVO {

    @Schema(description = "账号编号", example = "1024")
    private Long id;

    @Schema(description = "服务配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "服务配置编号不能为空")
    private Long providerId;

    @Schema(description = "邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱地址格式不正确")
    @Size(max = 255, message = "邮箱地址长度不能超过 {max} 个字符")
    private String mail;

    @Schema(description = "登录用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    @NotBlank(message = "登录用户名不能为空")
    @Size(max = 255, message = "登录用户名长度不能超过 {max} 个字符")
    private String username;

    @Schema(description = "密码或授权码，新增时必填，修改时留空表示不变", accessMode = Schema.AccessMode.WRITE_ONLY)
    @ToString.Exclude
    @Size(max = 4096, message = "密码或授权码长度不能超过 {max} 个字符")
    private String password;

    @Schema(description = "是否默认发件账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否默认发件账号不能为空")
    private Boolean defaultStatus;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(value = CommonStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    /**
     * 校验默认邮箱账号的启用状态
     *
     * @return 是否符合默认账号规则
     */
    @AssertTrue(message = "默认邮箱账号必须启用")
    @JsonIgnore
    public boolean isDefaultStatusValid() {
        return !Boolean.TRUE.equals(defaultStatus) || CommonStatusEnum.ENABLE.getStatus().equals(status);
    }

}
