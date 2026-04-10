package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - CRM 客户分配公海给对应负责人 Request VO")
@Data
public class CrmCustomerDistributeReqVO {

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1024]")
    @NotEmpty(message = "客户编号不能为空")
    private List<Long> ids;

    @Schema(description = "负责人", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "负责人不能为空")
    private Long ownerUserId;

}
