package cn.iocoder.yudao.module.promotion.dal.mysql.discount;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountProductDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
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

    default List<DiscountProductDO> selectListBySkuIds(Collection<Long> skuIds) {
        return selectList(DiscountProductDO::getSkuId, skuIds);
    }

    default List<DiscountProductDO> selectListBySkuIdsAndStatusAndNow(Collection<Long> skuIds, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return selectList(new LambdaQueryWrapperX<DiscountProductDO>()
                .in(DiscountProductDO::getSkuId, skuIds)
                .eq(DiscountProductDO::getActivityStatus,status)
                .lt(DiscountProductDO::getActivityStartTime, now)
                .gt(DiscountProductDO::getActivityEndTime, now));
    }

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
