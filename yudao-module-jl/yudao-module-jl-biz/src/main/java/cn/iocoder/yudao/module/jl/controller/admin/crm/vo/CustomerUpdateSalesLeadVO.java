package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 客户更新线索信息 Request VO")
@Data
@ToString(callSuper = true)
public class CustomerUpdateSalesLeadVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28406")
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "最近销售线索", example = "1")
    private Long salesId;
}
