package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客户限制配置 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerLimitConfigRespVO extends CrmCustomerLimitConfigBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27930")
    private Long id;

    @Schema(description = "规则适用人群名称")
    private String userNames;

    @Schema(description = "规则适用部门名称")
    private String deptNames;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
