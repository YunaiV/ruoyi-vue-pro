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

    default ImRtcParticipantDO selectByCallIdAndUserId(String callId, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ImRtcParticipantDO>()
                .eq(ImRtcParticipantDO::getCallId, callId)
                .eq(ImRtcParticipantDO::getUserId, userId));
    }

    default List<ImRtcParticipantDO> selectListByCallId(String callId) {
        return selectList(ImRtcParticipantDO::getCallId, callId);
    }

    default List<ImRtcParticipantDO> selectListByCallIdAndStatus(String callId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ImRtcParticipantDO>()
                .eq(ImRtcParticipantDO::getCallId, callId)
                .eq(ImRtcParticipantDO::getStatus, status));
    }

    default ImRtcParticipantDO selectLastByUserIdAndStatus(Long userId, Collection<Integer> statuses) {
        return selectLast(new LambdaQueryWrapperX<ImRtcParticipantDO>()
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
    default int updateByCallIdAndStatus(String callId, Integer oldStatus, ImRtcParticipantDO updateObj) {
        return update(updateObj, Wrappers.<ImRtcParticipantDO>lambdaUpdate()
                .eq(ImRtcParticipantDO::getCallId, callId)
                .eq(ImRtcParticipantDO::getStatus, oldStatus));
    }

}
