package cn.iocoder.yudao.module.member.controller.app.address.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
* 用户收件地址 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class AppAddressBaseVO {

    @ApiModelProperty(value = "收件人名称", required = true)
    @NotNull(message = "收件人名称不能为空")
    private String name;

    @ApiModelProperty(value = "手机号", required = true)
    @NotNull(message = "手机号不能为空")
    private String mobile;

    @ApiModelProperty(value = "地区编号", required = true)
    @NotNull(message = "地区编号不能为空")
    private Long areaId;

    @ApiModelProperty(value = "邮编", required = true)
    @NotEmpty(message = "邮编不能为空")
    private String postCode;

    @ApiModelProperty(value = "收件详细地址", required = true)
    @NotNull(message = "收件详细地址不能为空")
    private String detailAddress;

    @ApiModelProperty(value = "是否默认地址", required = true)
    @NotNull(message = "是否默认地址不能为空")
    private Boolean defaulted;

}
