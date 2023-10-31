package cn.iocoder.yudao.module.crm.controller.admin.business.vo;

import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 商机转移 Request VO")
@Data
public class CrmTransferBusinessReqVO {

    @Schema(description = "商机编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    @NotNull(message = "联系人编号不能为空")
    private Long id;

    /**
     * 新负责人的用户编号
     */
    @NotNull(message = "新负责人的用户编号不能为空")
    @Schema(description = "商机编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    private Long newOwnerUserId;

    /**
     * 老负责人是否加入团队，是/否
     */
    @Schema(description = "老负责人是否加入团队", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "老负责人是否加入团队不能为空")
    private Boolean joinTeam;

    /**
     * 老负责人加入团队后的权限级别。如果 {@link #joinTeam} 为 false, permissionLevel 为 null
     * 关联 {@link CrmPermissionLevelEnum}
     */
    @Schema(description = "老负责人加入团队后的权限级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer permissionLevel;

}
