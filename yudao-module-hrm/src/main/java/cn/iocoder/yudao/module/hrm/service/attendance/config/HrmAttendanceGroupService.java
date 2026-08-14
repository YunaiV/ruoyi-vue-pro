package cn.iocoder.yudao.module.hrm.service.attendance.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group.HrmAttendanceGroupPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.group.HrmAttendanceGroupSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceHolidayDO;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * HRM 考勤组 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmAttendanceGroupService {

    /**
     * 创建考勤组
     *
     * @param createReqVO 考勤组信息
     * @return 考勤组编号
     */
    Long createAttendanceGroup(@Valid HrmAttendanceGroupSaveReqVO createReqVO);

    /**
     * 更新考勤组
     *
     * @param updateReqVO 考勤组信息
     */
    void updateAttendanceGroup(@Valid HrmAttendanceGroupSaveReqVO updateReqVO);

    /**
     * 删除考勤组
     *
     * @param id 考勤组编号
     */
    void deleteAttendanceGroup(Long id);

    /**
     * 获得考勤组详情
     *
     * @param id 考勤组编号
     * @return 考勤组详情
     */
    HrmAttendanceGroupDO getAttendanceGroup(Long id);

    /**
     * 获得员工所在考勤组详情
     *
     * @param employeeId 员工编号
     * @return 考勤组详情
     */
    HrmAttendanceGroupDO getMyAttendanceGroup(Long employeeId);

    /**
     * 获得指定员工的考勤组 Map
     *
     * @param employeeIds 员工编号数组
     * @return 考勤组 Map
     */
    Map<Long, HrmAttendanceGroupDO> getAttendanceGroupMap(Collection<Long> employeeIds);

    /**
     * 获得考勤组指定日期的有效班次 Map
     *
     * @param attendanceGroup 考勤组
     * @param attendanceDates 考勤日期数组
     * @param holidayMap 节假日 Map
     * @return 有效班次 Map
     */
    Map<LocalDate, HrmAttendanceGroupDO.Shift> getAttendanceGroupShiftMap(
            HrmAttendanceGroupDO attendanceGroup, Collection<LocalDate> attendanceDates,
            Map<LocalDate, HrmAttendanceHolidayDO> holidayMap);

    /**
     * 获得员工指定考勤时间的实际班次
     *
     * @param employeeId 员工编号
     * @param attendanceTime 考勤时间
     * @return 实际班次；休息日返回 {@code null}
     */
    HrmAttendanceGroupDO.Shift getEmployeeAttendanceShift(
            Long employeeId, LocalDateTime attendanceTime);

    /**
     * 获得考勤组分页
     *
     * @param pageReqVO 分页查询条件
     * @return 考勤组分页
     */
    PageResult<HrmAttendanceGroupDO> getAttendanceGroupPage(HrmAttendanceGroupPageReqVO pageReqVO);

    /**
     * 获得考勤组列表
     *
     * @return 考勤组列表
     */
    List<HrmAttendanceGroupDO> getAttendanceGroupList();

    /**
     * 校验考勤组是否存在
     *
     * @param id 考勤组编号
     * @return 考勤组
     */
    HrmAttendanceGroupDO validateAttendanceGroupExists(Long id);

}
