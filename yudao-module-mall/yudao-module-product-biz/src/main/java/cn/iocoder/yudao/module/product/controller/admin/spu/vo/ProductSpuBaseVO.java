package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.module.product.dal.dataobject.brand.ProductBrandDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuSpecTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
* 商品spu Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class ProductSpuBaseVO {

    @ApiModelProperty(value = "商品名称")
    private String name;

    /**
     * 商品编码
     */
    private String code;

    @ApiModelProperty(value = "卖点", required = true)
    @NotNull(message = "卖点不能为空")
    private String sellPoint;

    @ApiModelProperty(value = "描述", required = true)
    @NotNull(message = "描述不能为空")
    private String description;

    /**
     * 商品品牌编号
     *
     * 关联 {@link ProductBrandDO#getId()}
     */
    private Long brandId;

    @ApiModelProperty(value = "分类id", required = true)
    @NotNull(message = "分类id不能为空")
    private Long categoryId;

    /**
     * 商品主图
     */
    private String bannerUrl;

    @ApiModelProperty(value = "商品主图地址,* 数组，以逗号分隔,最多上传15张", required = true)
    @NotNull(message = "商品主图地址,* 数组，以逗号分隔,最多上传15张不能为空")
    private List<String> picUrls;

    /**
     * 商品视频
     */
    private String videoUrl;

    /**
     * 规格类型
     *
     * 枚举 {@link ProductSpuSpecTypeEnum}
     */
    private Integer specType;

    @ApiModelProperty(value = "排序字段", required = true)
    @NotNull(message = "排序字段不能为空")
    private Integer sort;
    /**
     * 最小价格，单位使用：分
     *
     * 基于其对应的 {@link ProductSkuDO#getPrice()} 最小值
     */
    private Integer minPrice;
    /**
     * 最大价格，单位使用：分
     *
     * 基于其对应的 {@link ProductSkuDO#getPrice()} 最大值
     */
    private Integer maxPrice;
    /**
     * 市场价，单位使用：分
     *
     * 基于其对应的 {@link ProductSkuDO#getMarketPrice()} 最大值
     */
    private Integer marketPrice;
    /**
     * 总库存
     *
     * 基于其对应的 {@link ProductSkuDO#getStock()} 求和
     */
    private Integer totalStock;
    /**
     * 预警预存
     */
    private Integer warnStock;
    /**
     * 是否展示库存
     */
    private Boolean showStock;

    @ApiModelProperty(value = "上下架状态： 0 上架（开启） 1 下架（禁用）")
    private Integer status;

    /**
     * 商品销量
     */
    private Integer salesCount;
    /**
     * 虚拟销量
     */
    private Integer virtualSalesCount;
    /**
     * 商品点击量
     */
    private Integer clickCount;
}
