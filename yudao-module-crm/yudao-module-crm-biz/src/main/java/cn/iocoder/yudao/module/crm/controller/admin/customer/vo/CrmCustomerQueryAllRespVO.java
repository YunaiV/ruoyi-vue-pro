package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO 芋艿：这块要统一下；
@Schema(description = "管理后台 - CRM 全部客户 Response VO")
@Data
public class CrmCustomerQueryAllRespVO{

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private Long id;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String name;

}
