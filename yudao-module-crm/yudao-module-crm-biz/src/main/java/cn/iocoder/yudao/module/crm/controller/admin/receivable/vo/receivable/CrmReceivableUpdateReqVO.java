package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - CRM 回款更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmReceivableUpdateReqVO extends CrmReceivableBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25787")
    @NotNull(message = "ID不能为空")
    private Long id;

}
