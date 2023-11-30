package cn.iocoder.yudao.module.crm.controller.admin.business.vo.business;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 商机创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmBusinessCreateReqVO extends CrmBusinessBaseVO {

    // TODO @ljileo：新建的时候，应该可以传递添加的产品；

}
