package cn.iocoder.yudao.module.promotion.dal.mysql.discount;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountProductDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 限时折扣商城 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DiscountProductMapper extends BaseMapperX<DiscountProductDO> {


    default List<DiscountProductDO> selectListByActivityId(Long activityId) {
        return selectList(DiscountProductDO::getActivityId, activityId);
    }

    default List<DiscountProductDO> selectListByActivityId(Collection<Long> activityIds) {
        return selectList(DiscountProductDO::getActivityId, activityIds);
    }

    // TODO @zhangshuai：逻辑里，尽量避免写 join 语句哈，你可以看看这个查询，有什么办法优化？目前的一个思路，是分 2 次查询，性能也是 ok 的
    List<DiscountProductDO> getMatchDiscountProductList(@Param("skuIds") Collection<Long> skuIds);

    /**
     * 查询出指定 spuId 的 spu 参加的活动最接近现在的一条记录。多个的话，一个 spuId 对应一个最近的活动编号
     *
     * @param spuIds spu 编号
     * @param status 状态
     * @return 包含 spuId 和 activityId 的 map 对象列表
     */
    default List<Map<String, Object>> selectSpuIdAndActivityIdMapsBySpuIdsAndStatus(Collection<Long> spuIds, Integer status) {
        return selectMaps(new QueryWrapper<DiscountProductDO>()
                .select("spu_id AS spuId, MAX(DISTINCT(activity_id)) AS activityId")
                .in("spu_id", spuIds)
                .eq("activity_status", status)
                .groupBy("spu_id"));
    }

}
