package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
* 商品 SPU Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 *
 * @author HUIHUI
 */
@Data
public class ProductSpuBaseVO {

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖")
    @NotEmpty(message = "商品名称不能为空")
    private String name;

    @Schema(description = "关键字", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉丝滑不出汗")
    @NotEmpty(message = "商品关键字不能为空")
    private String keyword;

    @Schema(description = "商品简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖简介")
    @NotEmpty(message = "商品简介不能为空")
    private String introduction;

    @Schema(description = "商品详情", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖详情")
    @NotEmpty(message = "商品详情不能为空")
    private String description;

    @Schema(description = "商品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    @Schema(description = "商品品牌编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品品牌不能为空")
    private Long brandId;

    @Schema(description = "商品封面图", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xx.png")
    @NotEmpty(message = "商品封面图不能为空")
    private String picUrl;

    @Schema(description = "商品轮播图", requiredMode = Schema.RequiredMode.REQUIRED, example = "[https://www.iocoder.cn/xx.png, https://www.iocoder.cn/xxx.png]")
    private List<String> sliderPicUrls;

    @Schema(description = "商品视频", example = "https://www.iocoder.cn/xx.mp4")
    private String videoUrl;

    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品单位不能为空")
    private Integer unit;

    @Schema(description = "排序字段", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "商品排序字段不能为空")
    private Integer sort;

    // ========== SKU 相关字段 =========

    @Schema(description = "规格类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品规格类型不能为空")
    private Boolean specType;

    // ========== 物流相关字段 =========

    @Schema(description = "物流配置模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "111")
    @NotNull(message = "物流配置模板编号不能为空")
    private Long deliveryTemplateId;

    // ========== 营销相关字段 =========

    @Schema(description = "是否热卖推荐", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品推荐不能为空")
    private Boolean recommendHot;

    @Schema(description = "是否优惠推荐", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品推荐不能为空")
    private Boolean recommendBenefit;

    @Schema(description = "是否精品推荐", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品推荐不能为空")
    private Boolean recommendBest;

    @Schema(description = "是否新品推荐", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品推荐不能为空")
    private Boolean recommendNew;

    @Schema(description = "是否优品推荐", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品推荐不能为空")
    private Boolean recommendGood;

    @Schema(description = "赠送积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "111")
    @NotNull(message = "商品赠送积分不能为空")
    private Integer giveIntegral;

    @Schema(description = "分销类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "商品分销类型不能为空")
    private Boolean subCommissionType;

    @Schema(description = "活动展示顺序", example = "[1, 3, 2, 4, 5]")
    private List<Integer> activityOrders;

    // ========== 统计相关字段 =========

    @Schema(description = "虚拟销量", example = "66")
    private Integer virtualSalesCount;

}
