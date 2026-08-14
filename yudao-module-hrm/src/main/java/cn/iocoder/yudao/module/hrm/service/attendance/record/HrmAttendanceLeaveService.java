package cn.iocoder.yudao.module.hrm.service.attendance.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveCancelReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeaveCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.leave.HrmAttendanceLeavePageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceLeaveDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * HRM 考勤请假 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmAttendanceLeaveService {

    /**
     * 发起员工请假申请
     *
     * @param userId 用户编号
     * @param reqVO 请假申请信息
     * @return 请假记录编号
     */
    Long createLeave(Long userId, HrmAttendanceLeaveCreateReqVO reqVO);

    /**
     * 取消员工请假申请
     *
     * @param userId 用户编号
     * @param reqVO 取消申请信息
     */
    void cancelLeave(Long userId, HrmAttendanceLeaveCancelReqVO reqVO);

    /**
     * 获得当前员工的请假列表
     *
     * @param userId 用户编号
     * @return 请假列表
     */
    List<HrmAttendanceLeaveDO> getMyAttendanceLeaveList(Long userId);

    /**
     * 获得请假
     *
     * @param id 请假编号
     * @return 请假
     */
    HrmAttendanceLeaveDO getAttendanceLeave(Long id);

    /**
     * 获得请假分页
     *
     * @param reqVO 分页查询条件
     * @return 请假分页
     */
    PageResult<HrmAttendanceLeaveDO> getAttendanceLeavePage(HrmAttendanceLeavePageReqVO reqVO);

    /**
     * 获得请假列表
     *
     * @param reqVO 查询条件
     * @return 请假列表
     */
    List<HrmAttendanceLeaveDO> getAttendanceLeaveList(HrmAttendanceLeavePageReqVO reqVO);

    /**
     * 获得指定员工的请假列表
     *
     * @param employeeId 员工编号
     * @return 请假列表
     */
    List<HrmAttendanceLeaveDO> getAttendanceLeaveListByEmployeeId(Long employeeId);

    /**
     * 获得指定员工已生效的请假列表
     *
     * @param employeeId 员工编号
     * @return 请假列表
     */
    List<HrmAttendanceLeaveDO> getEffectiveAttendanceLeaveListByEmployeeId(Long employeeId);

    /**
     * 获得指定员工在时间范围内已生效的请假列表
     *
     * @param employeeIds 员工编号数组
     * @param leaveTimes 时间闭区间
     * @return 请假列表
     */
    List<HrmAttendanceLeaveDO> getEffectiveAttendanceLeaveListByEmployeeIdsAndTimeRange(
            Collection<Long> employeeIds, LocalDateTime[] leaveTimes);

    /**
     * 更新请假审批状态
     *
     * @param id 请假记录编号
     * @param processInstanceId 流程实例编号
     * @param status 审批状态
     * @param reason 审批原因
     */
    void updateLeaveApprovalStatus(
            Long id, String processInstanceId, Integer status, String reason);

}
