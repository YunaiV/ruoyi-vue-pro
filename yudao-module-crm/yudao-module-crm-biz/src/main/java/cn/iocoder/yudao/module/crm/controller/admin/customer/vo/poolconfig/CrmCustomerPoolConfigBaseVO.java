package cn.iocoder.yudao.module.crm.controller.admin.customer.vo.poolconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 客户公海配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmCustomerPoolConfigBaseVO {

    @Schema(description = "是否启用客户公海", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否启用客户公海不能为空")
    private Boolean enabled;

    @Schema(description = "未跟进放入公海天数", example = "2")
    private Integer contactExpireDays;

    @Schema(description = "未成交放入公海天数", example = "2")
    private Integer dealExpireDays;

    @Schema(description = "是否开启提前提醒", example = "true")
    private Boolean notifyEnabled;

    @Schema(description = "提前提醒天数", example = "2")
    private Integer notifyDays;

}
