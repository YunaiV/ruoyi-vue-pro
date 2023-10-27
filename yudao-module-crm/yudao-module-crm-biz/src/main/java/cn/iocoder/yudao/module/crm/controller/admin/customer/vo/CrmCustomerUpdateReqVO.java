package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 客户更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerUpdateReqVO extends CrmCustomerBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "只读权限的用户编号数组")
    private String roUserIds;

    @Schema(description = "读写权限的用户编号数组")
    private String rwUserIds;

}
