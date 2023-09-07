package cn.iocoder.yudao.module.promotion.api.seckill;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillActivityUpdateStockReqDTO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;
import cn.iocoder.yudao.module.promotion.service.seckill.SeckillActivityService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.SECKILL_ACTIVITY_UPDATE_STOCK_FAIL;

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
        SeckillActivityDO seckillActivity = activityService.getSeckillActivity(updateStockReqDTO.getActivityId());
        if (seckillActivity.getStock() < updateStockReqDTO.getCount()) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        // 获取活动商品
        List<SeckillProductDO> productDOs = activityService.getSeckillProductListByActivityId(updateStockReqDTO.getActivityId());
        List<SeckillActivityUpdateStockReqDTO.Item> items = updateStockReqDTO.getItems();
        Map<Long, List<Long>> map = new HashMap<>();
        items.forEach(item -> {
            if (map.containsKey(item.getSpuId())) {
                List<Long> skuIds = map.get(item.getSpuId());
                skuIds.add(item.getSkuId());
                map.put(item.getSpuId(), skuIds);
            } else {
                List<Long> list = new ArrayList<>();
                list.add(item.getSkuId());
                map.put(item.getSpuId(), list);
            }
        });
        // 过滤出购买的商品
        List<SeckillProductDO> productDOList = CollectionUtils.filterList(productDOs, item -> map.get(item.getSpuId()).contains(item.getSkuId()));
        Map<Long, SeckillActivityUpdateStockReqDTO.Item> productDOMap = CollectionUtils.convertMap(items, SeckillActivityUpdateStockReqDTO.Item::getSkuId, p -> p);
        // 检查活动商品库存是否充足
        boolean b = CollectionUtils.anyMatch(productDOList, item -> {
            SeckillActivityUpdateStockReqDTO.Item item1 = productDOMap.get(item.getSkuId());
            return (item.getStock() < item1.getCount()) || (item.getStock() - item1.getCount()) < 0;
        });
        if (b) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        List<SeckillProductDO> doList = CollectionUtils.convertList(productDOList, item -> {
            item.setStock(item.getStock() - productDOMap.get(item.getSkuId()).getCount());
            return item;
        });

        // 更新活动库存
        seckillActivity.setStock(seckillActivity.getStock() + updateStockReqDTO.getCount());
        seckillActivity.setTotalStock(seckillActivity.getTotalStock() - updateStockReqDTO.getCount());
        activityService.updateSeckillActivity(seckillActivity);
        // 更新活动商品库存
        activityService.updateSeckillActivityProductByList(doList);
    }

}
