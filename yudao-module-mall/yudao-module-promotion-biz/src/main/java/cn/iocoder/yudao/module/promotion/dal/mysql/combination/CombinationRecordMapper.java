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

    default List<CombinationRecordDO> selectListByUserId(Long userId) {
        return selectList(CombinationRecordDO::getUserId, userId);
    }

    default List<CombinationRecordDO> selectListByUserIdAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getUserId, userId)
                .eq(CombinationRecordDO::getStatus, status));
    }

    /**
     * 查询拼团记录
     *
     * @param headId 团长编号
     * @return 拼团记录
     */
    default CombinationRecordDO selectOneByHeadId(Long headId, Integer status) {
        return selectOne(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getId, headId)
                .eq(CombinationRecordDO::getStatus, status));
    }

    default List<CombinationRecordDO> selectListByHeadIdAndStatus(Long headId, Integer status) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getHeadId, headId)
                .eq(CombinationRecordDO::getStatus, status));
    }

    /**
     * 查询拼团记录
     *
     * @param userId     用户 id
     * @param activityId 活动 id
     * @return 拼团记录
     */
    default List<CombinationRecordDO> selectListByUserIdAndActivityId(Long userId, Long activityId) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eq(CombinationRecordDO::getUserId, userId)
                .eq(CombinationRecordDO::getActivityId, activityId));
    }

    /**
     * 获取最近的 count 条数据
     *
     * @param count 数量
     * @return 拼团记录列表
     */
    default List<CombinationRecordDO> selectLatestList(int count) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .orderByDesc(CombinationRecordDO::getCreateTime)
                .last("LIMIT " + count));
    }

    /**
     * 获得最近 count 条拼团记录（团长发起的）
     *
     * @param activityId 拼团活动编号
     * @param status     记录状态
     * @param count      数量
     * @return 拼团记录列表
     */
    default List<CombinationRecordDO> selectList(Long activityId, Integer status, Integer count) {
        return selectList(new LambdaQueryWrapperX<CombinationRecordDO>()
                .eqIfPresent(CombinationRecordDO::getActivityId, activityId)
                .eqIfPresent(CombinationRecordDO::getStatus, status)
                .eq(CombinationRecordDO::getHeadId, null) // TODO 团长的 headId 是不是 null 还是自己的记录编号来着？
                .orderByDesc(CombinationRecordDO::getCreateTime)
                .last("LIMIT " + count));
    }

}
