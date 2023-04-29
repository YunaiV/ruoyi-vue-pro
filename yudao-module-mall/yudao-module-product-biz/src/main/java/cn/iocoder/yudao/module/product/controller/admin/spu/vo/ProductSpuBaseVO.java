package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.product.dal.dataobject.brand.ProductBrandDO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.delivery.DeliveryTemplateDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "商品名称", required = true, example = "芋道")
    @NotEmpty(message = "商品名称不能为空")
    private String name;

    @Schema(description = "关键字", required = true, example = "芋道")
    @NotEmpty(message = "商品关键字不能为空")
    private String keyword;

    @Schema(description = "商品简介", required = true, example = "芋道")
    @NotEmpty(message = "商品简介不能为空")
    private String introduction;

    @Schema(description = "商品详情", required = true, example = "芋道")
    @NotEmpty(message = "商品详情不能为空")
    private String description;

    @Schema(description = "商品条码（一维码）", required = true, example = "芋道")
    @NotEmpty(message = "商品条码（一维码）不能为空")
    private String barCode;

    @Schema(description = "商品分类编号", required = true, example = "芋道")
    @NotEmpty(message = "商品分类编号不能为空")
    private Long categoryId;

    @Schema(description = "商品品牌编号", required = true, example = "芋道")
    @NotEmpty(message = "商品品牌编号不能为空")
    private Long brandId;

    @Schema(description = "商品封面图", required = true, example = "芋道")
    @NotEmpty(message = "商品封面图不能为空")
    private String picUrl;

    @Schema(description = "商品轮播图", required = true)
    @NotEmpty(message = "商品轮播图不能为空")
    private List<String> sliderPicUrls;

    @Schema(description = "商品视频")
    private String videoUrl;

    @Schema(description = "单位", required = true, example = "1")
    @NotEmpty(message = "商品单位不能为空")
    private Integer unit;

    @Schema(description = "排序字段", required = true, example = "1")
    @NotEmpty(message = "商品排序字段不能为空")
    private Integer sort;

    @Schema(description = "商品状态", required = true, example = "1")
    @NotEmpty(message = "商品状态不能为空")
    private Integer status;

    // ========== SKU 相关字段 =========

    @Schema(description = "规格类型", required = true, example = "true")
    @NotEmpty(message = "商品规格类型不能为空")
    private Boolean specType;

    @Schema(description = "商品价格", required = true, example = "1212")
    @NotEmpty(message = "商品价格不能为空")
    private Integer price;

    @Schema(description = "市场价", required = true, example = "3")
    @NotEmpty(message = "商品市场价不能为空")
    private Integer marketPrice;

    @Schema(description = "成本价", required = true, example = "12")
    @NotEmpty(message = "商品成本价不能为空")
    private Integer costPrice;

    @Schema(description = "库存", required = true, example = "111")
    @NotEmpty(message = "商品库存不能为空")
    private Integer stock;

    // ========== 物流相关字段 =========

    @Schema(description = "物流配置模板编号", required = true, example = "111")
    @NotEmpty(message = "物流配置模板编号不能为空")
    private Long deliveryTemplateId;

    // ========== 营销相关字段 =========

    @Schema(description = "是否热卖推荐", required = true, example = "true")
    @NotEmpty(message = "商品推荐不能为空")
    private Boolean recommendHot;

    @Schema(description = "是否优惠推荐", required = true, example = "true")
    @NotEmpty(message = "商品推荐不能为空")
    private Boolean recommendBenefit;

    @Schema(description = "是否精品推荐", required = true, example = "true")
    @NotEmpty(message = "商品推荐不能为空")
    private Boolean recommendBest;

    @Schema(description = "是否新品推荐", required = true, example = "true")
    @NotEmpty(message = "商品推荐不能为空")
    private Boolean recommendNew;

    @Schema(description = "是否优品推荐", required = true, example = "true")
    @NotEmpty(message = "商品推荐不能为空")
    private Boolean recommendGood;

    @Schema(description = "赠送积分", required = true, example = "111")
    @NotEmpty(message = "商品赠送积分不能为空")
    private Integer giveIntegral;

    @Schema(description = "赠送的优惠劵编号的数组")
    private List<Long> giveCouponTemplateIds;

    @Schema(description = "分销类型")
    private Boolean subCommissionType;

    @Schema(description = "活动展示顺序")
    private List<Integer> activityOrders;

    // ========== 统计相关字段 =========

    @Schema(description = "商品销量", required = true, example = "芋道")
    @NotEmpty(message = "商品销量不能为空")
    private Integer salesCount;

    @Schema(description = "虚拟销量", required = true, example = "芋道")
    @NotEmpty(message = "商品虚拟销量不能为空")
    private Integer virtualSalesCount;

    @Schema(description = "浏览量", required = true, example = "芋道")
    @NotEmpty(message = "商品浏览量不能为空")
    private Integer browseCount;

}
