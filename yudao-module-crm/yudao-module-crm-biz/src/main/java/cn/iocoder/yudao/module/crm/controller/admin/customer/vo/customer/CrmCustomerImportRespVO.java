package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 客户导入 Response VO")
@Data
@Builder
public class CrmCustomerImportRespVO {

    @Schema(description = "创建成功的客户名数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> createCustomerNames;

    @Schema(description = "更新成功的客户名数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> updateCustomerNames;

    @Schema(description = "导入失败的客户集合，key 为客户名，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, String> failureCustomerNames;

}
