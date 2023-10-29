package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 客户创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerCreateReqVO extends CrmCustomerBaseVO {

    // TODO @wanwan：负责人

}
