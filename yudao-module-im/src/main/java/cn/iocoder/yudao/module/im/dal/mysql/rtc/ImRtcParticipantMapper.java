package cn.iocoder.yudao.module.im.dal.mysql.rtc;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcParticipantDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

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

    default List<ImRtcParticipantDO> selectListByRoomAndStatus(String room, Integer status) {
        return selectList(new LambdaQueryWrapperX<ImRtcParticipantDO>()
                .eq(ImRtcParticipantDO::getRoom, room)
                .eq(ImRtcParticipantDO::getStatus, status));
    }

    default ImRtcParticipantDO selectLastOneByUserIdAndStatus(Long userId, Collection<Integer> statuses) {
        return selectLastOne(new LambdaQueryWrapperX<ImRtcParticipantDO>()
                .eq(ImRtcParticipantDO::getUserId, userId)
                .in(ImRtcParticipantDO::getStatus, statuses));
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
