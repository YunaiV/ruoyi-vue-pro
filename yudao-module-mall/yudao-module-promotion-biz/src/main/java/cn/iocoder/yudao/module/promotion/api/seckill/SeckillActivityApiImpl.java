package cn.iocoder.yudao.module.promotion.api.seckill;

import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillActivityUpdateStockReqDTO;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillActivityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 秒杀活动接口 Api 接口实现类
 *
 * @author HUIHUI
 */
@Service
public class SeckillActivityApiImpl implements SeckillActivityApi {

    @Resource
    private SeckillActivityService activityService;

    @Override
    public void updateSeckillStock(SeckillActivityUpdateStockReqDTO updateStockReqDTO) {
        activityService.updateSeckillStock(updateStockReqDTO);
    }

}
