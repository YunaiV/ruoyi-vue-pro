package cn.iocoder.yudao.module.promotion.dal.mysql.bargain;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 砍价记录 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface BargainRecordMapper extends BaseMapperX<BargainRecordDO> {

    default BargainRecordDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(BargainRecordDO::getId, id,
                BargainRecordDO::getUserId, userId);
    }

    default List<BargainRecordDO> selectListByUserIdAndActivityIdAndStatus(
            Long userId, Long activityId, Integer status) {
        return selectList(new LambdaQueryWrapper<>(BargainRecordDO.class)
                .eq(BargainRecordDO::getUserId, userId)
                .eq(BargainRecordDO::getActivityId, activityId)
                .eq(BargainRecordDO::getStatus, status));
    }

    default Long selectCountByUserIdAndActivityIdAndStatus(
            Long userId, Long activityId, Integer status) {
        return selectCount(new LambdaQueryWrapper<>(BargainRecordDO.class)
                .eq(BargainRecordDO::getUserId, userId)
                .eq(BargainRecordDO::getActivityId, activityId)
                .eq(BargainRecordDO::getStatus, status));
    }

}
