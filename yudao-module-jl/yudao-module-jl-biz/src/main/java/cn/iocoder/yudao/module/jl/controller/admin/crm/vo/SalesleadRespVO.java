package cn.iocoder.yudao.module.jl.controller.admin.crm.vo;

import cn.iocoder.yudao.module.jl.controller.admin.project.vo.ProjectQuoteBaseVO;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.ProjectQuoteOrScheduleSaveReqVO;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.ProjectQuoteRespVO;
import cn.iocoder.yudao.module.jl.entity.project.Project;
import cn.iocoder.yudao.module.jl.entity.project.ProjectQuote;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 销售线索 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesleadRespVO extends SalesleadBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "26580")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    private CustomerRespVO customer;

    private ProjectQuoteRespVO quote;

    @Schema(description = "折扣前总价", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long totalPrice = 10000L;

    private Project project;
}
