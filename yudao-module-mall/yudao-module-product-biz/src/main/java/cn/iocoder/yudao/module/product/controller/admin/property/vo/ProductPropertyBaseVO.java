package cn.iocoder.yudao.module.product.controller.admin.property.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 规格名称 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductPropertyBaseVO {

    @ApiModelProperty(value = "规格名称")
    private String name;

    @ApiModelProperty(value = "状态： 0 开启 ，1 禁用")
    private Integer status;

}
