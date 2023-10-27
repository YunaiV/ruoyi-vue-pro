package cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 商机状态更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmBusinessStatusUpdateReqVO extends CrmBusinessStatusBaseVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6802")
    @NotNull(message = "主键不能为空")
    private Long id;

}
