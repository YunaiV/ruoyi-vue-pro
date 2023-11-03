package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - CRM 回款更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReceivableUpdateReqVO extends ReceivableBaseVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25787")
    @NotNull(message = "ID不能为空")
    private Long id;

}
