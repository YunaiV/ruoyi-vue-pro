package cn.iocoder.yudao.module.promotion.api.seckill;

import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillActivityProductRespDTO;

import java.util.Collection;
import java.util.List;

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

    /**
     * 获取秒杀活动商品信息
     *
     * @param id     活动编号
     * @param skuIds sku 编号
     * @return 秒杀活动商品信息列表
     */
    List<SeckillActivityProductRespDTO> getSeckillActivityProductList(Long id, Collection<Long> skuIds);

}
