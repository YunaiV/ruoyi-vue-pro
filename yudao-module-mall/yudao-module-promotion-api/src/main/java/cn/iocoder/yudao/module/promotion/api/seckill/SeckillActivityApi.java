package cn.iocoder.yudao.module.promotion.api.seckill;

/**
 * 秒杀活动 API 接口
 *
 * @author HUIHUI
 */
public interface SeckillActivityApi {

    /**
     * 更新秒杀库存
     *
     * @param id 活动编号
     * @param skuId      sku 编号
     * @param count      数量
     */
    void updateSeckillStock(Long id, Long skuId, Integer count);

}
