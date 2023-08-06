package cn.iocoder.yudao.module.promotion.dal.mysql.bargain;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 砍价记录 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface BargainRecordMapper extends BaseMapperX<BargainRecordDO> {

    // TODO @puhui999：selectByUserIdAndOrderId
    default BargainRecordDO selectRecord(Long userId, Long orderId) {
        return selectOne(BargainRecordDO::getUserId, userId,
                BargainRecordDO::getOrderId, orderId);
    }

    /**
     * 查询砍价记录
     *
     * @param headId     团长编号
     * @param activityId 活动编号
     * @return 砍价记录
     */
    default BargainRecordDO selectRecordByHeadId(Long headId, Long activityId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<BargainRecordDO>()
                .eq(BargainRecordDO::getUserId, headId)
                .eq(BargainRecordDO::getActivityId, activityId)
                .eq(BargainRecordDO::getStatus, status));
    }

    default List<BargainRecordDO> selectListByHeadIdAndStatus(Long headId, Integer status) {
        return selectList(new LambdaQueryWrapperX<BargainRecordDO>()
                //.eq(BargainRecordDO::getHeadId, headId)
                .eq(BargainRecordDO::getStatus, status));
    }

    default List<BargainRecordDO> selectListByStatus(Integer status) {
        return selectList(BargainRecordDO::getStatus, status);
    }

}
