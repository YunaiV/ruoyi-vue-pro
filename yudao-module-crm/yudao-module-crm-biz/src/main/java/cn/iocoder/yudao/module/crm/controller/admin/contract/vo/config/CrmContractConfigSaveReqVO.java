package cn.iocoder.yudao.module.crm.controller.admin.contract.vo.config;

import cn.hutool.core.util.BooleanUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.AssertTrue;
import java.util.Objects;

@Schema(description = "管理后台 - CRM 合同配置 Request VO")
@Data
public class CrmContractConfigSaveReqVO {

    @Schema(description = "是否开启提前提醒", example = "true")
    @DiffLogField(name = "是否开启提前提醒")
    private Boolean notifyEnabled;

    @Schema(description = "提前提醒天数", example = "2")
    @DiffLogField(name = "提前提醒天数")
    private Integer notifyDays;

    @AssertTrue(message = "提前提醒天数不能为空")
    @JsonIgnore
    public boolean isNotifyDaysValid() {
        if (!BooleanUtil.isTrue(getNotifyEnabled())) {
            return true;
        }
        return Objects.nonNull(getNotifyDays());
    }

}
