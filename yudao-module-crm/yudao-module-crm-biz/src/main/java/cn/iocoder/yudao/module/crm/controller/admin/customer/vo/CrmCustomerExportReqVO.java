package cn.iocoder.yudao.module.crm.controller.admin.customer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

// TODO 芋艿：导出最后做，等基本确认的差不多之后；
@Schema(description = "管理后台 - 客户 Excel 导出 Request VO，参数和 CrmCustomerPageReqVO 是一致的")
@Data
public class CrmCustomerExportReqVO {

    @Schema(description = "客户名称", example = "赵六")
    private String name;

    @Schema(description = "手机", example = "18000000000")
    private String mobile;

    @Schema(description = "电话", example = "18000000000")
    private String telephone;

    @Schema(description = "网址", example = "https://www.baidu.com")
    private String website;

}
