package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 客户限制配置更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerLimitConfigUpdateReqVO extends CrmCustomerLimitConfigBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27930")
    @NotNull(message = "编号不能为空")
    private Long id;

}
