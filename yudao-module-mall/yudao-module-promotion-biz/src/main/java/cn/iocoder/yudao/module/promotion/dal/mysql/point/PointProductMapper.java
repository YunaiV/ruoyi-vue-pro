package cn.iocoder.yudao.module.promotion.dal.mysql.point;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.point.PointProductDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 积分商城商品 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface PointProductMapper extends BaseMapperX<PointProductDO> {

    default List<PointProductDO> selectListByActivityId(Collection<Long> activityIds) {
        return selectList(PointProductDO::getActivityId, activityIds);
    }

    default List<PointProductDO> selectListByActivityId(Long activityId) {
        return selectList(PointProductDO::getActivityId, activityId);
    }

    default void updateByActivityId(PointProductDO pointProductDO) {
        update(pointProductDO, new LambdaUpdateWrapper<PointProductDO>()
                .eq(PointProductDO::getActivityId, pointProductDO.getActivityId()));
    }

}