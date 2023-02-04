package cn.iocoder.yudao.module.product.controller.admin.property.vo.property;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 商品属性项 + 属性值 Response VO")
@Data
public class ProductPropertyAndValueRespVO {

    @Schema(description = "属性项的编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "属性项的名称", required = true, example = "颜色")
    private String name;

    /**
     * 属性值的集合
     */
    private List<Value> values;

    @Schema(description = "管理后台 - 属性值的简单 Response VO")
    @Data
    public static class Value {

        @Schema(description = "属性值的编号", required = true, example = "2048")
        private Long id;

        @Schema(description = "属性值的名称", required = true, example = "红色")
        private String name;

    }

}
