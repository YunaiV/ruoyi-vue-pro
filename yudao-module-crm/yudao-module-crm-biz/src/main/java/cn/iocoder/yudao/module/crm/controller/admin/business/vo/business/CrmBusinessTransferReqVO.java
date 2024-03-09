package cn.iocoder.yudao.module.crm.controller.admin.business.vo.business;

import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 商机转移 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrmBusinessTransferReqVO {

    // TODO @puhui999：这里最好还是用 id 哈，主要还是在 Business 业务里
    @Schema(description = "商机编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    @NotNull(message = "商机编号不能为空")
    private Long bizId;

    /**
     * 新负责人的用户编号
     */
    @Schema(description = "新负责人的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    @NotNull(message = "新负责人的用户编号不能为空")
    private Long newOwnerUserId;

    /**
     * 老负责人加入团队后的权限级别。如果 null 说明移除
     *
     * 关联 {@link CrmPermissionLevelEnum}
     */
    @Schema(description = "老负责人加入团队后的权限级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer oldOwnerPermissionLevel;

}
