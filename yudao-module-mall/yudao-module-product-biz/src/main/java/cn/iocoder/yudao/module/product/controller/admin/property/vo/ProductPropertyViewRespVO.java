package cn.iocoder.yudao.module.product.controller.admin.property.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Description: ProductPropertyViewRespVO
 * @Author: franky
 * @CreateDate: 2022/7/5 21:29
 * @Version: 1.0.0
 */
@ApiModel("管理后台 - 规格名称详情展示 Request VO")
@Data
@ToString(callSuper = true)
public class ProductPropertyViewRespVO {

    @ApiModelProperty(value = "规格名称id", example = "1")
    public Long propertyId;

    @ApiModelProperty(value = "规格名称", example = "内存")
    public String name;

    @ApiModelProperty(value = "规格属性值集合", example = "[{\"v1\":11,\"v2\":\"64G\"},{\"v1\":10,\"v2\":\"32G\"}]")
    public List<Tuple2> propertyValues;

    @Data
    @ApiModel(value = "规格属性值元组")
    public static class Tuple2 {
        private final long id;
        private final String name;

        public Tuple2(Long id, String name) {
            this.id = id;
            this.name = name;
        }

    }


}
