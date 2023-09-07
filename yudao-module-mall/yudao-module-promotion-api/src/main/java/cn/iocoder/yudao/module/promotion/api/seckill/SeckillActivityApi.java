package cn.iocoder.yudao.module.promotion.api.seckill;

import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillActivityUpdateStockReqDTO;

/**
 * 秒杀活动 API 接口
 *
 * @author HUIHUI
 */
public interface SeckillActivityApi {


    /**
     * 更新秒杀库存
     *
     * @param updateStockReqDTO 请求
     */
    void updateSeckillStock(SeckillActivityUpdateStockReqDTO updateStockReqDTO);

}
