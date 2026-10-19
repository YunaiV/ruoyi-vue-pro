package cn.iocoder.yudao.module.oa.dal.mysql.task;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskReceiverDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * OA 任务接收人 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaTaskReceiverMapper extends BaseMapperX<OaTaskReceiverDO> {

    default OaTaskReceiverDO selectByTaskIdAndUserId(Long taskId, Long userId) {
        return selectOne(OaTaskReceiverDO::getTaskId, taskId, OaTaskReceiverDO::getUserId, userId);
    }

    default List<OaTaskReceiverDO> selectListByTaskId(Long taskId) {
        return selectList(new LambdaQueryWrapperX<OaTaskReceiverDO>()
                .eq(OaTaskReceiverDO::getTaskId, taskId)
                .orderByAsc(OaTaskReceiverDO::getId));
    }

    default List<OaTaskReceiverDO> selectListByTaskIds(Collection<Long> taskIds) {
        return selectList(new LambdaQueryWrapperX<OaTaskReceiverDO>()
                .in(OaTaskReceiverDO::getTaskId, taskIds)
                .orderByAsc(OaTaskReceiverDO::getId));
    }

    default List<OaTaskReceiverDO> selectListByUserIdAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<OaTaskReceiverDO>()
                .eq(OaTaskReceiverDO::getUserId, userId)
                .eqIfPresent(OaTaskReceiverDO::getStatus, status)
                .orderByDesc(OaTaskReceiverDO::getUpdateTime)
                .orderByDesc(OaTaskReceiverDO::getId));
    }

    default Map<Integer, Long> selectCountMapByUserIdGroupByStatus(Long userId) {
        List<Map<String, Object>> counts = selectMaps(new MPJLambdaWrapperX<OaTaskReceiverDO>()
                .selectAs(OaTaskReceiverDO::getStatus, "status")
                .selectCount(OaTaskReceiverDO::getId, "count")
                .eq(OaTaskReceiverDO::getUserId, userId)
                .groupBy(OaTaskReceiverDO::getStatus));
        return convertMap(counts, row -> MapUtil.getInt(row, "status"), row -> MapUtil.getLong(row, "count"));
    }

    default void updateStatusByTaskId(Long taskId, Integer status) {
        update(new LambdaUpdateWrapper<OaTaskReceiverDO>()
                .eq(OaTaskReceiverDO::getTaskId, taskId)
                .set(OaTaskReceiverDO::getStatus, status));
    }

    default void deleteByTaskIdAndUserId(Long taskId, Long userId) {
        delete(new LambdaQueryWrapperX<OaTaskReceiverDO>()
                .eq(OaTaskReceiverDO::getTaskId, taskId)
                .eq(OaTaskReceiverDO::getUserId, userId));
    }

    default void deleteByTaskIdAndUserIds(Long taskId, Collection<Long> userIds) {
        delete(new LambdaQueryWrapperX<OaTaskReceiverDO>()
                .eq(OaTaskReceiverDO::getTaskId, taskId)
                .in(OaTaskReceiverDO::getUserId, userIds));
    }

    default void deleteByTaskId(Long taskId) {
        delete(OaTaskReceiverDO::getTaskId, taskId);
    }

}
