package cn.iocoder.yudao.module.product.controller.admin.propertyvalue.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

/**
* 规格值 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductPropertyValueBaseVO {

    @ApiModelProperty(value = "规格键id")
    private Long propertyId;

    @ApiModelProperty(value = "规格值名字")
    private String name;

    @ApiModelProperty(value = "状态： 1 开启 ，2 禁用")
    private Integer status;

}
