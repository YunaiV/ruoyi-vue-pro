package cn.iocoder.yudao.module.oa.dal.mysql.meetingroom;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.booking.OaMeetingRoomBookingPageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.OaMeetingRoomBookingDO;
import cn.iocoder.yudao.module.oa.enums.meetingroom.OaMeetingRoomUseStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * OA 会议室预定 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaMeetingRoomBookingMapper extends BaseMapperX<OaMeetingRoomBookingDO> {

    default OaMeetingRoomBookingDO selectByNo(String no) {
        return selectOne(OaMeetingRoomBookingDO::getNo, no);
    }

    default PageResult<OaMeetingRoomBookingDO> selectPage(Long userId, OaMeetingRoomBookingPageReqVO reqVO,
                                                        Collection<Long> roomIds, Collection<Long> moderatorIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaMeetingRoomBookingDO>()
                .eq(OaMeetingRoomBookingDO::getCreator, userId.toString())
                .likeIfPresent(OaMeetingRoomBookingDO::getNo, reqVO.getNo())
                .likeIfPresent(OaMeetingRoomBookingDO::getTitle, reqVO.getTitle())
                .eqIfPresent(OaMeetingRoomBookingDO::getDeptId, reqVO.getDeptId())
                .inIfPresent(OaMeetingRoomBookingDO::getRoomId, roomIds)
                .inIfPresent(OaMeetingRoomBookingDO::getModeratorUserId, moderatorIds)
                .eqIfPresent(OaMeetingRoomBookingDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OaMeetingRoomBookingDO::getUseStatus, reqVO.getUseStatus())
                .betweenIfPresent(OaMeetingRoomBookingDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(OaMeetingRoomBookingDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(OaMeetingRoomBookingDO::getCreator, reqVO.getCreator())
                .betweenIfPresent(OaMeetingRoomBookingDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(OaMeetingRoomBookingDO::getId));
    }

    default List<OaMeetingRoomBookingDO> selectListByRoomIdAndTimeOverlap(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<OaMeetingRoomBookingDO>()
                .eq(OaMeetingRoomBookingDO::getRoomId, roomId)
                .in(OaMeetingRoomBookingDO::getStatus, BpmProcessInstanceStatusEnum.RUNNING.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                .in(OaMeetingRoomBookingDO::getUseStatus, OaMeetingRoomUseStatusEnum.PENDING.getStatus(), OaMeetingRoomUseStatusEnum.IN_USE.getStatus())
                .lt(OaMeetingRoomBookingDO::getStartTime, endTime)
                .gt(OaMeetingRoomBookingDO::getEndTime, startTime)
                .orderByAsc(OaMeetingRoomBookingDO::getStartTime).orderByAsc(OaMeetingRoomBookingDO::getId));
    }

    default List<OaMeetingRoomBookingDO> selectListByReminderTypesAndStartTime(Collection<Integer> reminderTypes,
                                                                               LocalDateTime beginTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<OaMeetingRoomBookingDO>()
                .eq(OaMeetingRoomBookingDO::getStatus, BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                .eq(OaMeetingRoomBookingDO::getUseStatus, OaMeetingRoomUseStatusEnum.PENDING.getStatus())
                .eq(OaMeetingRoomBookingDO::getReminded, false)
                .in(OaMeetingRoomBookingDO::getReminderType, reminderTypes)
                .gt(OaMeetingRoomBookingDO::getStartTime, beginTime)
                .le(OaMeetingRoomBookingDO::getStartTime, endTime)
                .orderByAsc(OaMeetingRoomBookingDO::getStartTime).orderByAsc(OaMeetingRoomBookingDO::getId));
    }

    default int updateUseStatusByExpired(LocalDateTime endTime) {
        return update(new OaMeetingRoomBookingDO().setUseStatus(OaMeetingRoomUseStatusEnum.CANCELLED.getStatus()),
                new LambdaQueryWrapperX<OaMeetingRoomBookingDO>()
                        .eq(OaMeetingRoomBookingDO::getStatus, BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                        .eq(OaMeetingRoomBookingDO::getUseStatus, OaMeetingRoomUseStatusEnum.PENDING.getStatus())
                        .le(OaMeetingRoomBookingDO::getEndTime, endTime));
    }

    default Long selectCountByRoomIdAndEndTimeAfter(Long roomId, LocalDateTime time) {
        return selectCount(new LambdaQueryWrapperX<OaMeetingRoomBookingDO>()
                .eq(OaMeetingRoomBookingDO::getRoomId, roomId)
                .in(OaMeetingRoomBookingDO::getStatus, BpmProcessInstanceStatusEnum.RUNNING.getStatus(), BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                .in(OaMeetingRoomBookingDO::getUseStatus, OaMeetingRoomUseStatusEnum.PENDING.getStatus(), OaMeetingRoomUseStatusEnum.IN_USE.getStatus())
                .gt(OaMeetingRoomBookingDO::getEndTime, time));
    }
}
