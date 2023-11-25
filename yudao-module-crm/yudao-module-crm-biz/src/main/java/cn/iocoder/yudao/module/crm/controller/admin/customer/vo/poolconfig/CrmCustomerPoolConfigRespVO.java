package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.poolconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// TODO @wanwan：vo 下，可以新建一个 poolconfig，放它的 vo；
@Schema(description = "管理后台 - CRM 客户公海规则 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerPoolConfigRespVO extends CrmCustomerPoolConfigBaseVO {

}
