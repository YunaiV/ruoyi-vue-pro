package cn.iocoder.yudao.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

// TODO 芋艿：code review 相关逻辑
@Schema(description = "用户 APP - 修改密码 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthUpdatePasswordReqVO {

    @Schema(description = "用户旧密码", required = true, example = "123456")
    @NotBlank(message = "旧密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String oldPassword;

    @Schema(description = "新密码", required = true, example = "buzhidao")
    @NotEmpty(message = "新密码不能为空")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;
}
