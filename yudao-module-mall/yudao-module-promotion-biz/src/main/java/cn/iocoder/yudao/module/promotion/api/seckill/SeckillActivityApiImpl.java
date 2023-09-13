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

    // TODO @puhui：建议这块弄到 activityService 实现哈；
    // TODO @puhui：这个方法，要考虑事务性
    @Override
    public void updateSeckillStock(SeckillActivityUpdateStockReqDTO updateStockReqDTO) {
        // TODO @puhui999：长方法，最好有 1.1 1.2 2.1 这种步骤哈；
        SeckillActivityDO seckillActivity = activityService.getSeckillActivity(updateStockReqDTO.getActivityId());
        if (seckillActivity.getStock() < updateStockReqDTO.getCount()) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        // 获取活动商品
        // TODO @puhui999：在一个方法里，dos 和 dolist 最好保持一致，要么用 s，要么用 list 哈；
        List<SeckillProductDO> productDOs = activityService.getSeckillProductListByActivityId(updateStockReqDTO.getActivityId());
        // TODO @puhui999：这个是不是搞成  CollectionUtils.convertMultiMap()
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
        // TODO @puhui999：productDOList 可以简化成 productList；一般来说，do 之类不用带着哈，在变量里；
        List<SeckillProductDO> productDOList = CollectionUtils.filterList(productDOs, item -> map.get(item.getSpuId()).contains(item.getSkuId()));
        Map<Long, SeckillActivityUpdateStockReqDTO.Item> productDOMap = CollectionUtils.convertMap(items, SeckillActivityUpdateStockReqDTO.Item::getSkuId, p -> p);
        // 检查活动商品库存是否充足
        // TODO @puhui999：避免 b 这种无业务含义的变量；
        boolean b = CollectionUtils.anyMatch(productDOList, item -> {
            SeckillActivityUpdateStockReqDTO.Item item1 = productDOMap.get(item.getSkuId());
            return (item.getStock() < item1.getCount()) || (item.getStock() - item1.getCount()) < 0;
        });
        if (b) {
            throw exception(SECKILL_ACTIVITY_UPDATE_STOCK_FAIL);
        }
        // TODO @puhui999：类似 doList，应该和下面的 update 逻辑粘的更紧密一点；so 在空行的时候，应该挪到 74 之后里去；甚至更合理，应该是 79 之后；说白了，逻辑要分块，每个模块涉及的代码要紧密在一起；
        List<SeckillProductDO> doList = CollectionUtils.convertList(productDOList, item -> {
            item.setStock(item.getStock() - productDOMap.get(item.getSkuId()).getCount());
            return item;
        });

        // 更新活动库存
        // TODO @puhui999：考虑下并发更新
        seckillActivity.setStock(seckillActivity.getStock() + updateStockReqDTO.getCount());
        seckillActivity.setTotalStock(seckillActivity.getTotalStock() - updateStockReqDTO.getCount());
        activityService.updateSeckillActivity(seckillActivity);
        // 更新活动商品库存
        activityService.updateSeckillActivityProductList(doList);
    }

}
