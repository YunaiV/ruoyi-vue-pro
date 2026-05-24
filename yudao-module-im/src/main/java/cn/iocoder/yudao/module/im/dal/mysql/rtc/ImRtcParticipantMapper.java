package cn.iocoder.yudao.module.im.dal.mysql.rtc;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * IM 通话参与者 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImRtcParticipantMapper extends BaseMapperX<ImRtcParticipantDO> {

    default ImRtcParticipantDO selectByRoomAndUserId(String room, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ImRtcParticipantDO>()
                .eq(ImRtcParticipantDO::getRoom, room)
                .eq(ImRtcParticipantDO::getUserId, userId));
    }

    default List<ImRtcParticipantDO> selectListByRoom(String room) {
        return selectList(ImRtcParticipantDO::getRoom, room);
    }

    default List<ImRtcParticipantDO> selectListByCallId(Long callId) {
        return selectList(ImRtcParticipantDO::getCallId, callId);
    }

    default List<ImRtcParticipantDO> selectListByStatusAndInviteTimeBefore(Integer status, LocalDateTime threshold) {
        return selectList(new LambdaQueryWrapperX<ImRtcParticipantDO>()
                .eq(ImRtcParticipantDO::getStatus, status)
                .lt(ImRtcParticipantDO::getInviteTime, threshold));
    }

    default List<ImRtcParticipantDO> selectListByRoomAndStatusAndInviteTimeBefore(String room, Integer status, LocalDateTime threshold) {
        return selectList(new LambdaQueryWrapperX<ImRtcParticipantDO>()
                .eq(ImRtcParticipantDO::getRoom, room)
                .eq(ImRtcParticipantDO::getStatus, status)
                .lt(ImRtcParticipantDO::getInviteTime, threshold));
    }

    default ImRtcParticipantDO selectLastOneByUserIdAndStatus(Long userId, Collection<Integer> statuses) {
        return selectLastOne(new LambdaQueryWrapperX<ImRtcParticipantDO>()
                .eq(ImRtcParticipantDO::getUserId, userId)
                .in(ImRtcParticipantDO::getStatus, statuses));
    }

    default ImRtcParticipantDO selectLastOneByUserIdAndStatusInAndRoomNot(Long userId, Collection<Integer> statuses, String room) {
        return selectLastOne(new LambdaQueryWrapperX<ImRtcParticipantDO>()
                .eq(ImRtcParticipantDO::getUserId, userId)
                .in(ImRtcParticipantDO::getStatus, statuses)
                .ne(ImRtcParticipantDO::getRoom, room));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateByIdAndStatus(Long id, Integer oldStatus, ImRtcParticipantDO updateObj) {
        return update(updateObj, Wrappers.<ImRtcParticipantDO>lambdaUpdate()
                .eq(ImRtcParticipantDO::getId, id)
                .eq(ImRtcParticipantDO::getStatus, oldStatus));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateByRoomAndStatus(String room, Integer oldStatus, ImRtcParticipantDO updateObj) {
        return update(updateObj, Wrappers.<ImRtcParticipantDO>lambdaUpdate()
                .eq(ImRtcParticipantDO::getRoom, room)
                .eq(ImRtcParticipantDO::getStatus, oldStatus));
    }

}
