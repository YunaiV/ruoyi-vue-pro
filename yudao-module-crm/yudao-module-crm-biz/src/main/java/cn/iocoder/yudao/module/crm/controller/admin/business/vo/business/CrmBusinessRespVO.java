package cn.iocoder.yudao.module.crm.controller.admin.business.vo.business;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 商机 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmBusinessRespVO extends CrmBusinessBaseVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "32129")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String customerName;

    @Schema(description = "状态类型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "进行中")
    private String statusTypeName;

    @Schema(description = "状态名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "跟进中")
    private String statusName;

}
