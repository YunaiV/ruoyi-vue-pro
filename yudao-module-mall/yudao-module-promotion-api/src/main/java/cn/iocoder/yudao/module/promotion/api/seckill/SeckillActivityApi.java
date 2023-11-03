package cn.iocoder.yudao.module.promotion.api.seckill;

import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillValidateJoinRespDTO;

/**
 * 秒杀活动 API 接口
 *
 * @author HUIHUI
 */
public interface SeckillActivityApi {

    /**
     * 更新秒杀库存（减少）
     *
     * @param id    活动编号
     * @param skuId sku 编号
     * @param count 数量(正数)
     */
    void updateSeckillStockDecr(Long id, Long skuId, Integer count);

    /**
     * 更新秒杀库存（增加）
     *
     * @param id    活动编号
     * @param skuId sku 编号
     * @param count 数量(正数)
     */
    void updateSeckillStockIncr(Long id, Long skuId, Integer count);

    /**
     * 【下单前】校验是否参与秒杀活动
     *
     * 如果校验失败，则抛出业务异常
     *
     * @param activityId 活动编号
     * @param skuId      SKU 编号
     * @param count      数量
     * @return 秒杀信息
     */
    SeckillValidateJoinRespDTO validateJoinSeckill(Long activityId, Long skuId, Integer count);

}
