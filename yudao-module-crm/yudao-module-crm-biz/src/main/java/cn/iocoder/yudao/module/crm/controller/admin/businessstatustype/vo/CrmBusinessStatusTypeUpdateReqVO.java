package cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 商机状态类型更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmBusinessStatusTypeUpdateReqVO extends CrmBusinessStatusTypeBaseVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "24019")
    @NotNull(message = "主键不能为空")
    private Long id;

}
