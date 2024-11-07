package cn.iocoder.yudao.module.promotion.dal.mysql.discount;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountProductDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

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

    default List<DiscountProductDO> selectListBySpuIdsAndStatus(Collection<Long> spuIds, Integer status) {
        return selectList(new LambdaQueryWrapperX<DiscountProductDO>()
                .in(DiscountProductDO::getSpuId, spuIds)
                .eq(DiscountProductDO::getActivityStatus, status));
    }

    default void updateByActivityId(DiscountProductDO discountProductDO) {
        update(discountProductDO, new LambdaUpdateWrapper<DiscountProductDO>()
                .eq(DiscountProductDO::getActivityId, discountProductDO.getActivityId()));
    }

    default void deleteByActivityId(Long activityId) {
        delete(DiscountProductDO::getActivityId, activityId);
    }

    default List<DiscountProductDO> selectListBySkuIdsAndStatusAndNow(Collection<Long> skuIds, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return selectList(new LambdaQueryWrapperX<DiscountProductDO>()
                .in(DiscountProductDO::getSkuId, skuIds)
                .eq(DiscountProductDO::getActivityStatus,status)
                .lt(DiscountProductDO::getActivityStartTime, now)
                .gt(DiscountProductDO::getActivityEndTime, now));
    }

}
