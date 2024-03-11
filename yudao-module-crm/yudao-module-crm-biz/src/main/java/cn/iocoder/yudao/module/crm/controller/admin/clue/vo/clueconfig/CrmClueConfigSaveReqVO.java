package cn.iocoder.yudao.module.crm.controller.admin.clue.vo.clueconfig;

import cn.hutool.core.util.BooleanUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Schema(description = "管理后台 - CRM 客户公海配置的创建/更新 Request VO")
@Data
public class CrmClueConfigSaveReqVO {

    @Schema(description = "是否启用线索列表中手机号脱敏", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @DiffLogField(name = "是否启用线索列表中手机号脱敏")
    @NotNull(message = "是否启用线索列表中手机号脱敏不能为空")
    private Boolean hidphoneEnabled;
}
