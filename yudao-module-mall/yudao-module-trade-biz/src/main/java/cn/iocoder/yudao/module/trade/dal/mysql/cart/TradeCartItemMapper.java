package cn.iocoder.yudao.module.trade.dal.mysql.cart;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.TradeCartItemDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface TradeCartItemMapper extends BaseMapperX<TradeCartItemDO> {

    default TradeCartItemDO selectByUserIdAndSkuId(Long userId, Long skuId) {
        return selectOne(TradeCartItemDO::getUserId, userId,
                TradeCartItemDO::getSkuId, skuId);
    }

    default List<TradeCartItemDO> selectListByUserIdAndSkuIds(Long userId, Collection<Long> skuIds) {
        return selectList(new LambdaQueryWrapper<TradeCartItemDO>().eq(TradeCartItemDO::getUserId, userId)
                .in(TradeCartItemDO::getSkuId, skuIds));
    }

   default void updateByIds(Collection<Long> ids, TradeCartItemDO updateObject) {
       update(updateObject, new LambdaQueryWrapper<TradeCartItemDO>().in(TradeCartItemDO::getId, ids));
   }

    default Integer selectSumByUserId(Long userId) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<TradeCartItemDO>()
                .select("SUM(count) AS sumCount")
                .eq("user_id", userId));
        // 获得数量
        return CollUtil.isNotEmpty(result) ? MapUtil.getInt(result.get(0), "sumCount") : 0;
    }

    default List<TradeCartItemDO> selectListByUserId(Long userId, Boolean selected) {
        return selectList(new LambdaQueryWrapperX<TradeCartItemDO>().eq(TradeCartItemDO::getUserId, userId)
                .eqIfPresent(TradeCartItemDO::getSelected, selected));
    }

}
