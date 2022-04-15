package cn.iocoder.yudao.module.member.controller.app.address.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 用户收件地址 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class AppAddressBaseVO {

    // TODO @shuaidawang：swagger 注解的 example；其它 VO 类也要补充下

    @ApiModelProperty(value = "收件人名称", required = true)
    @NotNull(message = "收件人名称不能为空")
    private String name;

    @ApiModelProperty(value = "手机号", required = true)
    @NotNull(message = "手机号不能为空")
    private String mobile;

    @ApiModelProperty(value = "地区编码", required = true)
    @NotNull(message = "地区编码不能为空")
    private Integer areaCode;

    @ApiModelProperty(value = "收件详细地址", required = true)
    @NotNull(message = "收件详细地址不能为空")
    private String detailAddress;

    @ApiModelProperty(value = "地址类型", required = true) // TODO @shuaidawang：这个是枚举字段，最好说明下对应的枚举类
    @NotNull(message = "地址类型不能为空")
    private Integer type;

}
