package cn.iocoder.yudao.module.hrm.dal.mysql.attendance.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeavePageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceLeaveDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface HrmAttendanceLeaveMapper extends BaseMapperX<HrmAttendanceLeaveDO> {

    default PageResult<HrmAttendanceLeaveDO> selectPage(HrmAttendanceLeavePageReqVO reqVO) {
        return selectPage(reqVO, buildQueryWrapper(reqVO));
    }

    default List<HrmAttendanceLeaveDO> selectList(HrmAttendanceLeavePageReqVO reqVO) {
        LambdaQueryWrapperX<HrmAttendanceLeaveDO> queryWrapper = buildQueryWrapper(reqVO);
        MyBatisUtils.addOrder(queryWrapper, reqVO.getSortingFields());
        return selectList(queryWrapper);
    }

    default LambdaQueryWrapperX<HrmAttendanceLeaveDO> buildQueryWrapper(HrmAttendanceLeavePageReqVO reqVO) {
        return new LambdaQueryWrapperX<HrmAttendanceLeaveDO>()
                .inIfPresent(HrmAttendanceLeaveDO::getEmployeeId, reqVO.getEmployeeIds())
                .inIfPresent(HrmAttendanceLeaveDO::getType, reqVO.getTypes())
                .betweenIfPresent(HrmAttendanceLeaveDO::getStartTime, reqVO.getStartTime())
                .eqIfPresent(HrmAttendanceLeaveDO::getApprovalStatus, reqVO.getApprovalStatus());
    }

    default List<HrmAttendanceLeaveDO> selectListByEmployeeId(Long employeeId) {
        return selectList(new LambdaQueryWrapperX<HrmAttendanceLeaveDO>()
                .eq(HrmAttendanceLeaveDO::getEmployeeId, employeeId)
                .orderByDesc(HrmAttendanceLeaveDO::getStartTime)
                .orderByDesc(HrmAttendanceLeaveDO::getId));
    }

    default List<HrmAttendanceLeaveDO> selectListByEmployeeIdAndApprovalStatus(Long employeeId,
                                                                                Integer approvalStatus) {
        return selectList(new LambdaQueryWrapperX<HrmAttendanceLeaveDO>()
                .eq(HrmAttendanceLeaveDO::getEmployeeId, employeeId)
                .eq(HrmAttendanceLeaveDO::getApprovalStatus, approvalStatus)
                .orderByDesc(HrmAttendanceLeaveDO::getStartTime)
                .orderByDesc(HrmAttendanceLeaveDO::getId));
    }

    default List<HrmAttendanceLeaveDO> selectListByEmployeeIdsAndTimeRangeAndApprovalStatus(
            Collection<Long> employeeIds, LocalDateTime[] leaveTimes, Integer approvalStatus) {
        return selectList(new LambdaQueryWrapperX<HrmAttendanceLeaveDO>()
                .in(HrmAttendanceLeaveDO::getEmployeeId, employeeIds)
                .betweenIfPresent(HrmAttendanceLeaveDO::getStartTime,
                        HrmAttendanceLeaveDO::getEndTime, leaveTimes)
                .eq(HrmAttendanceLeaveDO::getApprovalStatus, approvalStatus)
                .orderByDesc(HrmAttendanceLeaveDO::getStartTime)
                .orderByDesc(HrmAttendanceLeaveDO::getId));
    }

    default Long selectCountByEmployeeIdAndTimeRangeAndApprovalStatusIn(
            Long employeeId, LocalDateTime[] leaveTimes, Collection<Integer> approvalStatuses) {
        return selectCount(new LambdaQueryWrapperX<HrmAttendanceLeaveDO>()
                .eq(HrmAttendanceLeaveDO::getEmployeeId, employeeId)
                .betweenIfPresent(HrmAttendanceLeaveDO::getStartTime,
                        HrmAttendanceLeaveDO::getEndTime, leaveTimes)
                .in(HrmAttendanceLeaveDO::getApprovalStatus, approvalStatuses));
    }

    /**
     * 在指定请假记录和审批流程处于指定状态时，更新审批结果
     *
     * @param id 请假记录编号
     * @param processInstanceId 流程实例编号
     * @param approvalStatus 审批状态
     * @param updateObj 更新对象
     * @return 更新条数
     */
    default int updateApprovalResultByIdAndProcessInstanceIdAndApprovalStatus(
            Long id, String processInstanceId, Integer approvalStatus, HrmAttendanceLeaveDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<HrmAttendanceLeaveDO>()
                .eq(HrmAttendanceLeaveDO::getId, id)
                .and(wrapper -> wrapper.isNull(HrmAttendanceLeaveDO::getProcessInstanceId)
                        .or().eq(HrmAttendanceLeaveDO::getProcessInstanceId, processInstanceId))
                .eq(HrmAttendanceLeaveDO::getApprovalStatus, approvalStatus));
    }

}
