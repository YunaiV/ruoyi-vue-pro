package cn.iocoder.yudao.module.oa.dal.mysql.schedule;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.dal.dataobject.schedule.OaScheduleParticipantDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OA 日程参与人 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaScheduleParticipantMapper extends BaseMapperX<OaScheduleParticipantDO> {

    default OaScheduleParticipantDO selectByScheduleIdAndUserId(Long scheduleId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<OaScheduleParticipantDO>()
                .eq(OaScheduleParticipantDO::getScheduleId, scheduleId)
                .eq(OaScheduleParticipantDO::getUserId, userId));
    }

    default List<OaScheduleParticipantDO> selectListByScheduleId(Long scheduleId) {
        return selectList(OaScheduleParticipantDO::getScheduleId, scheduleId);
    }

    default void deleteByScheduleIdAndUserIds(Long scheduleId, Collection<Long> userIds) {
        delete(new LambdaQueryWrapperX<OaScheduleParticipantDO>()
                .eq(OaScheduleParticipantDO::getScheduleId, scheduleId)
                .in(OaScheduleParticipantDO::getUserId, userIds));
    }

    default List<OaScheduleParticipantDO> selectListByScheduleIds(Collection<Long> scheduleIds) {
        return selectList(new LambdaQueryWrapperX<OaScheduleParticipantDO>()
                .in(OaScheduleParticipantDO::getScheduleId, scheduleIds));
    }

    default List<OaScheduleParticipantDO> selectListByUserId(Long userId) {
        return selectList(OaScheduleParticipantDO::getUserId, userId);
    }

    default void deleteByScheduleId(Long scheduleId) {
        delete(OaScheduleParticipantDO::getScheduleId, scheduleId);
    }

}
