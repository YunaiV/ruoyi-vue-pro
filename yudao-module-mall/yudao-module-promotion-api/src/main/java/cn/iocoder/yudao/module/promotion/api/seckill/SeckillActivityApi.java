package cn.iocoder.yudao.module.promotion.api.seckill;

/**
 * 秒杀活动 API 接口
 *
 * @author HUIHUI
 */
public interface SeckillActivityApi {

    // TODO @puhui999：activityId 改成 id 好点哈；
    /**
     * 更新秒杀库存
     *
     * @param activityId 活动编号
     * @param skuId      sku 编号
     * @param count      数量
     */
    void updateSeckillStock(Long activityId, Long skuId, Integer count);

}
