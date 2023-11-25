package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.poolconfig;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.AssertTrue;
import java.util.Objects;

@Schema(description = "管理后台 - CRM 客户更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerPoolConfigUpdateReqVO extends CrmCustomerPoolConfigBaseVO {

    @AssertTrue(message = "客户公海规则设置不正确")
    public boolean poolEnableValid() {
        if (!BooleanUtil.isTrue(getEnabled())) {
            return true;
        }
        return ObjectUtil.isAllNotEmpty(getContactExpireDays(), getDealExpireDays());
    }

    @AssertTrue(message = "客户公海规则设置不正确")
    public boolean notifyEnableValid() {
        if (!BooleanUtil.isTrue(getNotifyEnabled())) {
            return true;
        }
        return Objects.nonNull(getNotifyDays());
    }
}
