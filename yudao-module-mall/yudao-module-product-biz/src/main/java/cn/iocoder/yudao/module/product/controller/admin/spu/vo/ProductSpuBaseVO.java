package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
* 商品 SPU Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductSpuBaseVO {

    @ApiModelProperty(value = "商品名称", required = true, example = "芋道")
    @NotEmpty(message = "商品名称不能为空")
    private String name;

    @ApiModelProperty(value = "商品编码", example = "yudaoyuanma")
    private String code;

    @ApiModelProperty(value = "促销语", example = "好吃！")
    private String sellPoint;

    @ApiModelProperty(value = "商品详情", required = true, example = "我是商品描述")
    @NotNull(message = "商品详情不能为空")
    private String description;

    @ApiModelProperty(value = "商品分类编号", required = true, example = "1")
    @NotNull(message = "商品分类编号不能为空")
    private Long categoryId;

    @ApiModelProperty(value = "商品品牌编号", example = "1")
    private Long brandId;

    @ApiModelProperty(value = "商品图片的数组", required = true)
    @NotNull(message = "商品图片的数组不能为空")
    private List<String> picUrls;

    @ApiModelProperty(value = "商品视频", required = true)
    private String videoUrl;

    @ApiModelProperty(value = "排序字段", required = true, example = "1")
    private Integer sort;

    @ApiModelProperty(value = "商品状态", required = true, example = "1", notes = "参见 ProductSpuStatusEnum 枚举类")
    @NotNull(message = "商品状态不能为空")
    @InEnum(ProductSpuStatusEnum.class)
    private Integer status;

    // ========== SKU 相关字段 =========

    @ApiModelProperty(value = "规格类型", required = true, example = "1", notes = "参见 ProductSpuSpecTypeEnum 枚举类")
    @NotNull(message = "规格类型不能为空")
    @InEnum(ProductSpuSpecTypeEnum.class)
    private Integer specType;

    @ApiModelProperty(value = "是否展示库存", required = true, example = "true")
    @NotNull(message = "是否展示库存不能为空")
    private Boolean showStock;

    @ApiModelProperty(value = "库存", required = true, example = "true")
    private Integer totalStock;

    @ApiModelProperty(value = "市场价", example = "1024")
    private Integer marketPrice;

    @ApiModelProperty(value = " 最小价格，单位使用：分", required = true, example = "1024")
    private Integer minPrice;

    @ApiModelProperty(value = "最大价格，单位使用：分", required = true, example = "1024")
    private Integer maxPrice;

    // ========== 统计相关字段 =========

    @ApiModelProperty(value = "商品销量", example = "1024")
    private Integer salesCount;

    @ApiModelProperty(value = "虚拟销量", required = true, example = "1024")
    @NotNull(message = "虚拟销量不能为空")
    private Integer virtualSalesCount;

    @ApiModelProperty(value = "点击量", example = "1024")
    private Integer clickCount;

}
