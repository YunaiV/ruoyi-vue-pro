package cn.iocoder.yudao.module.trade.dal.mysql.cart;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.TradeCartDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface TradeCartMapper extends BaseMapperX<TradeCartDO> {

    default TradeCartDO selectByUserIdAndSkuId(Long userId, Long skuId,
                                               Boolean addStatus, Boolean orderStatus) {
        return selectOne(TradeCartDO::getUserId, userId,
                TradeCartDO::getSkuId, skuId,
                TradeCartDO::getAddStatus, addStatus,
                TradeCartDO::getOrderStatus, orderStatus);
    }

    default Integer selectSumByUserId(Long userId) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<TradeCartDO>()
                .select("SUM(count) AS sumCount")
                .eq("user_id", userId)
                .eq("add_status", true) // 只计算添加到购物车中的
                .eq("order_status", false)); // 必须未下单
        // 获得数量
        return CollUtil.getFirst(result) != null ? MapUtil.getInt(result.get(0), "sumCount") : 0;
    }

    default Map<Long, Integer> selectSumMapByUserId(Long userId) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<TradeCartDO>()
                .select("spu_id, SUM(count) AS sumCount")
                .eq("user_id", userId)
                .eq("add_status", true) // 只计算添加到购物车中的
                .eq("order_status", false) // 必须未下单
                .groupBy("spu_id"));
        // 获得数量
        return CollectionUtils.convertMap(result, item -> MapUtil.getLong(item, "spu_id"),
                item -> MapUtil.getInt(item, "sumCount"));
    }

    default TradeCartDO selectById(Long id, Long userId) {
        return selectOne(TradeCartDO::getId, id,
                TradeCartDO::getUserId, userId);
    }

    default List<TradeCartDO> selectListByIds(Collection<Long> ids, Long userId) {
        return selectList(new LambdaQueryWrapper<TradeCartDO>()
                .in(TradeCartDO::getId, ids)
                .eq(TradeCartDO::getUserId, userId));
    }

    default List<TradeCartDO> selectListByUserId(Long userId, Boolean addStatus, Boolean orderStatus) {
        return selectList(new LambdaQueryWrapper<TradeCartDO>()
                .eq(TradeCartDO::getUserId, userId)
                .eq(TradeCartDO::getAddStatus, addStatus)
                .eq(TradeCartDO::getOrderStatus, orderStatus));
    }

    default void updateByIds(Collection<Long> ids, TradeCartDO updateObject) {
        update(updateObject, new LambdaQueryWrapper<TradeCartDO>().in(TradeCartDO::getId, ids));
    }

}
