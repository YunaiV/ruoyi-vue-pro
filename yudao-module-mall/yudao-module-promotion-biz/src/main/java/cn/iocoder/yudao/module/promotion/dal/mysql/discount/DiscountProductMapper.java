package cn.iocoder.yudao.module.promotion.dal.mysql.discount;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.discount.DiscountProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 限时折扣商城 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface DiscountProductMapper extends BaseMapperX<DiscountProductDO> {

    default List<DiscountProductDO> selectListBySkuId(Collection<Long> skuIds) {
        return selectList(DiscountProductDO::getSkuId, skuIds);
    }

    default List<DiscountProductDO> selectListByActivityId(Long activityId) {
        return selectList(DiscountProductDO::getActivityId, activityId);
    }

    default List<DiscountProductDO> selectListByActivityId(Collection<Long> activityIds) {
        return selectList(DiscountProductDO::getActivityId, activityIds);
    }

    // TODO @zhangshuai：逻辑里，尽量避免写 join 语句哈，你可以看看这个查询，有什么办法优化？目前的一个思路，是分 2 次查询，性能也是 ok 的
    List<DiscountProductDO> getMatchDiscountProductList(@Param("skuIds") Collection<Long> skuIds);
}
