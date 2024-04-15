package cn.iocoder.yudao.module.crm.controller.admin.permission.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "管理后台 - CRM 数据权限创建/更新 Request VO")
@Data
public class CrmPermissionSaveReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "CRM 类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @InEnum(CrmBizTypeEnum.class)
    @NotNull(message = "CRM 类型不能为空")
    private Integer bizType;

    @Schema(description = "CRM 类型数据编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "CRM 类型数据编号不能为空")
    private Long bizId;

    @Schema(description = "权限级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @InEnum(CrmPermissionLevelEnum.class)
    @NotNull(message = "权限级别不能为空")
    private Integer level;

    /**
     * 添加客户团队成员时，需要额外有【联系人】【商机】【合同】的 checkbox 选择。
     * 选中时，同时添加对应的权限
     */
    @Schema(description = "同时添加", requiredMode = Schema.RequiredMode.REQUIRED, example = "10430")
    private List<Integer> toBizTypes;

}
