package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 客户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerPageReqVO extends PageParam {

    @Schema(description = "客户名称", example = "赵六")
    private String name;

    @Schema(description = "手机", example = "18000000000")
    private String mobile;

    // TODO @wanwan：这个字段不需要哈
    @Schema(description = "电话", example = "18000000000")
    private String telephone;

    // TODO @wanwan：这个字段不需要哈
    @Schema(description = "网址", example = "https://www.baidu.com")
    private String website;

    // TODO @芋艿：场景；
}
