package cn.iocoder.yudao.module.trade.dal.mysql.cart;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.cart.CartDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface CartMapper extends BaseMapperX<CartDO> {

    default CartDO selectByUserIdAndSkuId(Long userId, Long skuId) {
        return selectOne(CartDO::getUserId, userId,
                CartDO::getSkuId, skuId);
    }

    default Integer selectSumByUserId(Long userId) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<CartDO>()
                .select("SUM(count) AS sumCount")
                .eq("user_id", userId)
                .eq("add_status", true) // 只计算添加到购物车中的
                .eq("order_status", false)); // 必须未下单
        // 获得数量
        return CollUtil.getFirst(result) != null ? MapUtil.getInt(result.get(0), "sumCount") : 0;
    }

    default Map<Long, Integer> selectSumMapByUserId(Long userId) {
        // SQL sum 查询
        List<Map<String, Object>> result = selectMaps(new QueryWrapper<CartDO>()
                .select("spu_id, SUM(count) AS sumCount")
                .eq("user_id", userId)
                .eq("add_status", true) // 只计算添加到购物车中的
                .eq("order_status", false) // 必须未下单
                .groupBy("spu_id"));
        // 获得数量
        return CollectionUtils.convertMap(result, item -> MapUtil.getLong(item, "spu_id"),
                item -> MapUtil.getInt(item, "sumCount"));
    }

    default CartDO selectById(Long id, Long userId) {
        return selectOne(CartDO::getId, id,
                CartDO::getUserId, userId);
    }

    default List<CartDO> selectListByIds(Collection<Long> ids, Long userId) {
        return selectList(new LambdaQueryWrapper<CartDO>()
                .in(CartDO::getId, ids)
                .eq(CartDO::getUserId, userId));
    }

    default List<CartDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapper<CartDO>()
                .eq(CartDO::getUserId, userId));
    }

    default List<CartDO> selectListByUserId(Long userId, Set<Long> ids) {
        return selectList(new LambdaQueryWrapper<CartDO>()
                .eq(CartDO::getUserId, userId)
                .in(CartDO::getId, ids));
    }

    default void updateByIds(Collection<Long> ids, Long userId, CartDO updateObj) {
        update(updateObj, new LambdaQueryWrapper<CartDO>()
                .in(CartDO::getId, ids)
                .eq(CartDO::getUserId, userId));
    }

}
