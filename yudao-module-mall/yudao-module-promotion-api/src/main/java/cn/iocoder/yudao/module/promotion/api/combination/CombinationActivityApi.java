package cn.iocoder.yudao.module.promotion.api.combination;

/**
 * 拼团活动 Api 接口
 *
 * @author HUIHUI
 */
public interface CombinationActivityApi {

    /**
     * 校验是否满足拼团条件
     *
     * @param activityId 活动编号
     * @param userId 用户编号
     * @param skuId sku 编号
     * @param count 数量
     */
    void validateCombination(Long activityId, Long userId, Long skuId, Integer count);

}
