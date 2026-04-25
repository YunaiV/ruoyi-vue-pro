package cn.iocoder.yudao.module.product.service.recommend;

import cn.iocoder.yudao.module.product.service.recommend.bo.ProductRecommendBO;

import java.util.List;

/**
 * 智能服装推荐服务
 * <p>
 * 对应 Python IntelligentFashionRecommender + RealTimePersonalization
 *
 * @author deepay
 */
public interface ProductRecommendService {

    /**
     * 获取个性化推荐列表（最多 10 条）
     *
     * @param userId      会员用户编号
     * @param budgetCents 预算金额（分），0 表示不限
     * @param occasion    使用场景（如 casual / formal / sport）
     * @return 推荐列表，按综合得分降序
     */
    List<ProductRecommendBO> getPersonalizedRecommendations(Long userId, int budgetCents, String occasion);

    /**
     * 实时行为追踪 & 即时更新推荐
     * <p>
     * 对应 Python RealTimePersonalization.adjust_recommendations_in_realtime()
     *
     * @param userId  会员用户编号
     * @param action  行为类型：VIEW / LIKE / DISLIKE / CART_ADD
     * @param spuId   操作的商品 SPU 编号
     * @return 更新后的推荐列表
     */
    List<ProductRecommendBO> trackActionAndRefresh(Long userId, String action, Long spuId);

}
