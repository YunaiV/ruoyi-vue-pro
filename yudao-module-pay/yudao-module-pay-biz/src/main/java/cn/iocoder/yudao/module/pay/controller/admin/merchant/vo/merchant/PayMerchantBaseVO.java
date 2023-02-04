package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
* 支付商户信息 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class PayMerchantBaseVO {

    @Schema(description = "商户全称", required = true)
    @NotNull(message = "商户全称不能为空")
    private String name;

    @Schema(description = "商户简称", required = true)
    @NotNull(message = "商户简称不能为空")
    private String shortName;

    @Schema(description = "开启状态", required = true)
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
