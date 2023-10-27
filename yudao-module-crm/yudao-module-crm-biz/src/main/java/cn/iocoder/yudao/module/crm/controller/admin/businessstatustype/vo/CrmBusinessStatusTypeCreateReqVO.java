package cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 商机状态类型创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmBusinessStatusTypeCreateReqVO extends CrmBusinessStatusTypeBaseVO {

}
