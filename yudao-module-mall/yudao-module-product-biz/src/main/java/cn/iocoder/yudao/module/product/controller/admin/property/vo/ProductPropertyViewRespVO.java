package cn.iocoder.yudao.module.product.controller.admin.property.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

// TODO 芋艿：看看怎么删除
@ApiModel("管理后台 - 商品属性详情展示 Request VO")
@Data
@ToString(callSuper = true)
public class ProductPropertyViewRespVO {

    @ApiModelProperty(value = "属性项 id", example = "1")
    public Long propertyId;

    @ApiModelProperty(value = "属性项的名字", example = "内存")
    public String name;

    @ApiModelProperty(value = "属性值数组")
    public List<Tuple2> propertyValues;

    @Data
    @ApiModel(value = "属性值元组")
    public static class Tuple2 {
        private final long id;
        private final String name;

        public Tuple2(Long id, String name) {
            this.id = id;
            this.name = name;
        }

    }


}
