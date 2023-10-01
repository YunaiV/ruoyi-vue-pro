package cn.iocoder.yudao.module.promotion.api.seckill;

import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillActivityProductRespDTO;
import cn.iocoder.yudao.module.promotion.convert.seckill.seckillactivity.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillActivityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

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
    public void updateSeckillStock(Long id, Long skuId, Integer count) {
        activityService.updateSeckillStock(id, skuId, count);
    }

    @Override
    public List<SeckillActivityProductRespDTO> getSeckillActivityProductList(Long id, Collection<Long> skuIds) {
        return SeckillActivityConvert.INSTANCE.convertList4(activityService.getSeckillActivityProductList(id, skuIds));
    }

}
