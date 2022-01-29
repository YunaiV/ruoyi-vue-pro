package cn.iocoder.yudao.module.system.controller.tenant.vo;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 租户 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class SysTenantBaseVO {

    @ApiModelProperty(value = "租户名", required = true, example = "芋道")
    @NotNull(message = "租户名不能为空")
    private String name;

    @ApiModelProperty(value = "联系人", required = true, example = "芋艿")
    @NotNull(message = "联系人不能为空")
    private String contactName;

    @ApiModelProperty(value = "联系手机", example = "15601691300")
    private String contactMobile;

    @ApiModelProperty(value = "租户状态（0正常 1停用）", required = true, example = "1")
    @NotNull(message = "租户状态（0正常 1停用）不能为空")
    private Integer status;

}
