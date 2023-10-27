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

    // TODO @wanwan：类型应该是传递 List<Long>; 不过这个字段，默认新建的时候不传递，在“移交”功能里管理
    @Schema(description = "只读权限的用户编号数组")
    private String roUserIds;

    // TODO @wanwan：类型应该是传递 List<Long>; 不过这个字段，默认新建的时候不传递，在“移交”功能里管理
    @Schema(description = "读写权限的用户编号数组")
    private String rwUserIds;

}
