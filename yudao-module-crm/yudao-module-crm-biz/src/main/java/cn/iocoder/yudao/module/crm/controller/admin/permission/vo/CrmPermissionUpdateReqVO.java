package cn.iocoder.yudao.module.crm.controller.admin.permission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - CRM 数据权限更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmPermissionUpdateReqVO extends CrmPermissionBaseVO {

    @Schema(description = "数据权限编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    @NotNull(message = "数据权限编号不能为空")
    private Long id;

    // TODO @puhui999：是不是只更新 permission？？？ 是的

}
