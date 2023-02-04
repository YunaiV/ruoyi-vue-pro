package cn.iocoder.yudao.module.product.controller.admin.property.vo.value;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 商品属性值分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductPropertyValuePageReqVO extends PageParam {

    @Schema(description = "属性项的编号", example = "1024")
    private String propertyId;

    @Schema(description = "名称", example = "红色")
    private String name;

    @Schema(description = "状态", required = true, example = "1")
    private Integer status;

}
