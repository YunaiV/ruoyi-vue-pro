package cn.iocoder.yudao.module.promotion.dal.mysql.combination;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 拼团记录 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface CombinationRecordMapper extends BaseMapperX<CombinationRecordDO> {

    default CombinationRecordDO selectByUserIdAndOrderId(Long userId, Long orderId) {
        return selectOne(CombinationRecordDO::getUserId, userId,
                CombinationRecordDO::getOrderId, orderId);
    }

    // TODO @puhui999 selectByUserIdAndActivityId
    /**
     * 查询拼团记录
     *
     * @param headId     团长编号
     * @param activityId 活动编号
     * @return 拼团记录
     */
    default CombinationRecordDO selectRecordByHeadId(Long headId, Long activityId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getUserId, headId)
                .eq(CombinationRecordDO::getActivityId, activityId)
                .eq(CombinationRecordDO::getStatus, status));
    }

    default List<CombinationRecordDO> selectListByHeadIdAndStatus(Long headId, Integer status) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getHeadId, headId)
                .eq(CombinationRecordDO::getStatus, status));
    }

    default List<CombinationRecordDO> selectListByStatus(Integer status) {
        return selectList(CombinationRecordDO::getStatus, status);
    }

}
