package cn.iocoder.yudao.module.oa.dal.mysql.attendance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.controller.admin.attendance.vo.OaAttendancePageReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.attendance.OaAttendanceDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OA 考勤 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaAttendanceMapper extends BaseMapperX<OaAttendanceDO> {

    default PageResult<OaAttendanceDO> selectPageByUserIds(OaAttendancePageReqVO pageReqVO, List<Long> userIds) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<OaAttendanceDO>()
                .in(OaAttendanceDO::getUserId, userIds)
                .eqIfPresent(OaAttendanceDO::getType, pageReqVO.getType())
                .eqIfPresent(OaAttendanceDO::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(OaAttendanceDO::getAttendanceTime, pageReqVO.getAttendanceTime())
                .orderByDesc(OaAttendanceDO::getAttendanceTime)
                .orderByDesc(OaAttendanceDO::getId));
    }

    default PageResult<OaAttendanceDO> selectPageByUserId(OaAttendancePageReqVO pageReqVO, Long userId) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<OaAttendanceDO>()
                .eq(OaAttendanceDO::getUserId, userId)
                .eqIfPresent(OaAttendanceDO::getType, pageReqVO.getType())
                .eqIfPresent(OaAttendanceDO::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(OaAttendanceDO::getAttendanceTime, pageReqVO.getAttendanceTime())
                .orderByDesc(OaAttendanceDO::getAttendanceTime)
                .orderByDesc(OaAttendanceDO::getId));
    }

    default List<OaAttendanceDO> selectListByUserIdAndAttendanceTime(
            Long userId, LocalDateTime[] attendanceTime) {
        return selectList(new LambdaQueryWrapperX<OaAttendanceDO>()
                .eqIfPresent(OaAttendanceDO::getUserId, userId)
                .betweenIfPresent(OaAttendanceDO::getAttendanceTime, attendanceTime)
                .orderByAsc(OaAttendanceDO::getAttendanceTime)
                .orderByAsc(OaAttendanceDO::getId));
    }

    default List<OaAttendanceDO> selectListByUserIdsAndAttendanceTime(
            List<Long> userIds, LocalDateTime[] attendanceTime) {
        return selectList(new LambdaQueryWrapperX<OaAttendanceDO>()
                .in(OaAttendanceDO::getUserId, userIds)
                .between(OaAttendanceDO::getAttendanceTime, attendanceTime[0], attendanceTime[1])
                .orderByAsc(OaAttendanceDO::getAttendanceTime)
                .orderByAsc(OaAttendanceDO::getId));
    }

}
