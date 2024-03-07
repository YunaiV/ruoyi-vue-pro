package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig;

import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 客户限制配置 Response VO")
@Data
public class CrmCustomerLimitConfigRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27930")
    private Long id;

    @Schema(description = "规则类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer type;

    @Schema(description = "规则适用人群")
    private List<Long> userIds;

    @Schema(description = "规则适用部门")
    private List<Long> deptIds;

    @Schema(description = "数量上限", requiredMode = Schema.RequiredMode.REQUIRED, example = "28384")
    private Integer maxCount;

    @Schema(description = "成交客户是否占有拥有客户数")
    private Boolean dealCountEnabled;

    @Schema(description = "规则适用人群名称")
    private List<AdminUserRespDTO> users;

    @Schema(description = "规则适用部门名称")
    private List<DeptRespDTO> depts;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
