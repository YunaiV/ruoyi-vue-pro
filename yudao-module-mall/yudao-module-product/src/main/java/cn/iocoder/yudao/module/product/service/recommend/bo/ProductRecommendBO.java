package cn.iocoder.yudao.module.product.service.recommend.bo;

import lombok.Data;

import java.util.List;

/**
 * 商品推荐结果 BO
 * <p>
 * 对应 Python IntelligentFashionRecommender 推荐列表中的每一项
 *
 * @author deepay
 */
@Data
public class ProductRecommendBO {

    // ===== 商品基本信息 =====
    private Long spuId;
    private String spuName;
    private String picUrl;
    private Long categoryId;
    private String categoryName;

    // ===== 价格 =====
    /** 零售价（分） */
    private Integer retailPrice;
    /** 批发价（分，按会员等级折扣计算）*/
    private Integer wholesalePrice;
    /** 折扣百分比（0-100，例如 30 表示七折） */
    private Integer discountPercent;

    // ===== 推荐元数据 =====
    /** 综合推荐分数（0.0 ~ 1.0） */
    private Double score;
    /** 各维度分数：category/popularity/season/budget */
    private DimensionScore dimensionScore;
    /** 推荐理由（给用户展示） */
    private String reason;

    // ===== 库存状态 =====
    private Integer stock;
    private Integer salesCount;

    /**
     * 各维度推荐分数
     */
    @Data
    public static class DimensionScore {
        /** 分类匹配度（0-1） */
        private Double categoryMatch;
        /** 热度分（0-1） */
        private Double popularity;
        /** 季节适配分（0-1） */
        private Double seasonRelevance;
        /** 预算匹配分（0-1） */
        private Double budgetFit;
    }

}
