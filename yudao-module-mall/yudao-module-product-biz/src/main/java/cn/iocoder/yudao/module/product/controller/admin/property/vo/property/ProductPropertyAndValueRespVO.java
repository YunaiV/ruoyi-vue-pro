package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("管理后台 - 商品属性项 + 属性值 Response VO")
@Data
public class ProductPropertyAndValueRespVO {

    @ApiModelProperty(value = "属性项的编号", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "属性项的名称", required = true, example = "颜色")
    private String name;

    /**
     * 属性值的集合
     */
    private List<Value> values;

    @ApiModel("管理后台 - 属性值的简单 Response VO")
    @Data
    public static class Value {

        @ApiModelProperty(value = "属性值的编号", required = true, example = "2048")
        private Long id;

        @ApiModelProperty(value = "属性值的名称", required = true, example = "红色")
        private String name;

    }

}
