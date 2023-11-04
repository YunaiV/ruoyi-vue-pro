package cn.iocoder.yudao.module.system.controller.admin.socail.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 社交用户更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SocialUserUpdateReqVO extends SocialUserBaseVO {

    @Schema(description = "主键(自增策略)", requiredMode = Schema.RequiredMode.REQUIRED, example = "14569")
    @NotNull(message = "主键(自增策略)不能为空")
    private Long id;

}
