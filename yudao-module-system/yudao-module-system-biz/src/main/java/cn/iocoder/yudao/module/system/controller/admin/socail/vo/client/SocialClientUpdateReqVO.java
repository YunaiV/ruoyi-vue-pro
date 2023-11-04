package cn.iocoder.yudao.module.system.controller.admin.socail.vo.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 社交客户端更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SocialClientUpdateReqVO extends SocialClientBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27162")
    @NotNull(message = "编号不能为空")
    private Long id;

}
