package cn.iocoder.yudao.module.crm.controller.admin.clue.vo.clueconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - CRM 线索规则 Response VO")
@Data
public class CrmClueConfigRespVO {

    @Schema(description = "是否启用线索列表中手机号脱敏", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用客户公海不能为空")
    private Boolean hidphoneEnabled;

}
