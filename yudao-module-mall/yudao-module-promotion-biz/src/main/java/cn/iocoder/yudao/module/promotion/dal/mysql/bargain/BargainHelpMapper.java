package cn.iocoder.yudao.module.promotion.dal.mysql.bargain;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainHelpDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BargainHelpMapper extends BaseMapperX<BargainHelpDO> {

    default Long selectCountByUserIdAndActivityId(Long userId, Long activityId) {
        return selectCount(new LambdaQueryWrapper<>(BargainHelpDO.class)
                .eq(BargainHelpDO::getUserId, userId)
                .eq(BargainHelpDO::getActivityId, activityId));
    }

    default Long selectCountByRecordId(Long recordId) {
        return selectCount(BargainHelpDO::getRecordId, recordId);
    }

    default BargainHelpDO selectByUserIdAndRecordId(Long userId, Long recordId) {
        return selectOne(new LambdaQueryWrapper<>(BargainHelpDO.class)
                .eq(BargainHelpDO::getUserId, userId)
                .eq(BargainHelpDO::getRecordId, recordId));
    }

}
