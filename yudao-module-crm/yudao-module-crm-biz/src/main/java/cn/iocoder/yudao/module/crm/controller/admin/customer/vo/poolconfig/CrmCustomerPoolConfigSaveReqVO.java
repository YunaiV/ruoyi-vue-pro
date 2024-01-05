package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.poolconfig;

import cn.hutool.core.util.BooleanUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Objects;

@Schema(description = "管理后台 - CRM 客户公海配置的保存 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerPoolConfigSaveReqVO extends CrmCustomerPoolConfigBaseVO {

    @AssertTrue(message = "未成交放入公海天数不能为空")
    @JsonIgnore
    public boolean isDealExpireDaysValid() {
        if (!BooleanUtil.isTrue(getEnabled())) {
            return true;
        }
        return Objects.nonNull(getDealExpireDays());
    }

    @AssertTrue(message = "未跟进放入公海天数不能为空")
    @JsonIgnore
    public boolean isContactExpireDaysValid() {
        if (!BooleanUtil.isTrue(getEnabled())) {
            return true;
        }
        return Objects.nonNull(getContactExpireDays());
    }

    @AssertTrue(message = "提前提醒天数不能为空")
    @JsonIgnore
    public boolean isNotifyDaysValid() {
        if (!BooleanUtil.isTrue(getNotifyEnabled())) {
            return true;
        }
        return Objects.nonNull(getNotifyDays());
    }

}
