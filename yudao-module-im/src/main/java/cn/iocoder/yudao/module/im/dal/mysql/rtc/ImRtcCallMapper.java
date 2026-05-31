package cn.iocoder.yudao.module.im.dal.mysql.rtc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.im.controller.admin.manager.rtc.vo.ImRtcCallManagerPageReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.rtc.ImRtcCallDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * IM 通话记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImRtcCallMapper extends BaseMapperX<ImRtcCallDO> {

    default ImRtcCallDO selectByRoom(String room) {
        return selectOne(ImRtcCallDO::getRoom, room);
    }

    default ImRtcCallDO selectLastOneByGroupIdAndStatusIn(Long groupId, Collection<Integer> statuses) {
        return selectLastOne(new LambdaQueryWrapperX<ImRtcCallDO>()
                .eq(ImRtcCallDO::getGroupId, groupId)
                .in(ImRtcCallDO::getStatus, statuses));
    }

    default int updateByIdAndStatus(Long id, Integer oldStatus, ImRtcCallDO updateObj) {
        return update(updateObj, Wrappers.<ImRtcCallDO>lambdaUpdate()
                .eq(ImRtcCallDO::getId, id)
                .eq(ImRtcCallDO::getStatus, oldStatus));
    }

    @SuppressWarnings("UnusedReturnValue")
    default int updateByIdAndStatusIn(Long id, Collection<Integer> statuses, ImRtcCallDO updateObj) {
        return update(updateObj, Wrappers.<ImRtcCallDO>lambdaUpdate()
                .eq(ImRtcCallDO::getId, id)
                .in(ImRtcCallDO::getStatus, statuses));
    }

    default List<ImRtcCallDO> selectListByStatusInAndStartTimeBefore(Collection<Integer> statuses,
                                                                    LocalDateTime startTimeBefore) {
        return selectList(new LambdaQueryWrapperX<ImRtcCallDO>()
                .in(ImRtcCallDO::getStatus, statuses)
                .lt(ImRtcCallDO::getStartTime, startTimeBefore));
    }

    default PageResult<ImRtcCallDO> selectPage(ImRtcCallManagerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImRtcCallDO>()
                .eqIfPresent(ImRtcCallDO::getInviterUserId, reqVO.getInviterUserId())
                .eqIfPresent(ImRtcCallDO::getConversationType, reqVO.getConversationType())
                .eqIfPresent(ImRtcCallDO::getMediaType, reqVO.getMediaType())
                .eqIfPresent(ImRtcCallDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ImRtcCallDO::getEndReason, reqVO.getEndReason())
                .betweenIfPresent(ImRtcCallDO::getStartTime, reqVO.getStartTime())
                .orderByDesc(ImRtcCallDO::getId));
    }

}
