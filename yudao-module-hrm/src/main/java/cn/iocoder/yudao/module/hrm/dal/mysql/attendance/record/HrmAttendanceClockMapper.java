package cn.iocoder.yudao.module.hrm.dal.mysql.attendance.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceClockDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface HrmAttendanceClockMapper extends BaseMapperX<HrmAttendanceClockDO> {

    default PageResult<HrmAttendanceClockDO> selectPage(HrmAttendanceClockPageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO)
                .eqIfPresent(HrmAttendanceClockDO::getStatus, reqVO.getStatus())
                .orderByDesc(HrmAttendanceClockDO::getClockTime)
                .orderByDesc(HrmAttendanceClockDO::getId));
    }

    default List<HrmAttendanceClockDO> selectList(HrmAttendanceClockPageReqVO reqVO) {
        return selectList(buildQueryWrapper(reqVO)
                .eqIfPresent(HrmAttendanceClockDO::getStatus, reqVO.getStatus())
                .orderByDesc(HrmAttendanceClockDO::getClockTime)
                .orderByDesc(HrmAttendanceClockDO::getId));
    }

    default List<HrmAttendanceClockDO> selectListByEmployeeIdAndClockTime(
            Long employeeId, LocalDateTime[] clockTimes) {
        return selectList(new LambdaQueryWrapperX<HrmAttendanceClockDO>()
                .eq(HrmAttendanceClockDO::getEmployeeId, employeeId)
                .betweenIfPresent(HrmAttendanceClockDO::getClockTime, clockTimes)
                .orderByAsc(HrmAttendanceClockDO::getClockTime));
    }

    default List<HrmAttendanceClockDO> selectListByEmployeeIdsAndClockTime(
            Collection<Long> employeeIds, LocalDateTime[] clockTimes) {
        return selectList(new LambdaQueryWrapperX<HrmAttendanceClockDO>()
                .inIfPresent(HrmAttendanceClockDO::getEmployeeId, employeeIds)
                .betweenIfPresent(HrmAttendanceClockDO::getClockTime, clockTimes)
                .orderByAsc(HrmAttendanceClockDO::getClockTime));
    }

    default LambdaQueryWrapperX<HrmAttendanceClockDO> buildQueryWrapper(HrmAttendanceClockPageReqVO reqVO) {
        return new LambdaQueryWrapperX<HrmAttendanceClockDO>()
                .eqIfPresent(HrmAttendanceClockDO::getEmployeeId, reqVO.getEmployeeId())
                .inIfPresent(HrmAttendanceClockDO::getEmployeeId, reqVO.getEmployeeIds())
                .eqIfPresent(HrmAttendanceClockDO::getType, reqVO.getType())
                .inIfPresent(HrmAttendanceClockDO::getSourceType, reqVO.getSourceTypes())
                .likeIfPresent(HrmAttendanceClockDO::getAddress, reqVO.getAddress())
                .betweenIfPresent(
                        HrmAttendanceClockDO::getAttendanceTime, reqVO.getAttendanceTime())
                .betweenIfPresent(HrmAttendanceClockDO::getClockTime, reqVO.getClockTime());
    }

}
