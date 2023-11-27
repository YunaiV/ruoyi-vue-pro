package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - CRM 全部客户 Response VO")
@Data
public class CrmCustomerQueryAllRespVO{
    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String name;
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private Long id;

}
