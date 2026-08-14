package cn.iocoder.yudao.module.hrm.service.attendance.record;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.clock.HrmAttendanceClockSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock.HrmPortalAttendanceClockCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.attendance.vo.clock.HrmPortalAttendanceClockDetailRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.record.HrmAttendanceClockDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * HRM 考勤打卡 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmAttendanceClockService {

    /**
     * 创建手工打卡记录
     *
     * @param createReqVO 打卡记录信息
     * @return 打卡记录编号
     */
    Long createAttendanceClock(HrmAttendanceClockSaveReqVO createReqVO);

    /**
     * 获得员工端当前打卡详情
     *
     * @param userId 登录用户编号
     * @return 打卡详情
     */
    HrmPortalAttendanceClockDetailRespVO getMyAttendanceClockDetail(Long userId);

    /**
     * 员工端手机打卡
     *
     * @param userId 登录用户编号
     * @param createReqVO 打卡请求
     * @return 打卡记录编号
     */
    Long createMyAttendanceClock(Long userId, HrmPortalAttendanceClockCreateReqVO createReqVO);

    /**
     * 更新手工打卡记录
     *
     * @param updateReqVO 打卡记录信息
     */
    void updateAttendanceClock(HrmAttendanceClockSaveReqVO updateReqVO);

    /**
     * 删除手工打卡记录
     *
     * @param id 打卡记录编号
     */
    void deleteAttendanceClock(Long id);

    /**
     * 批量删除手工打卡记录
     *
     * @param ids 打卡记录编号数组
     */
    void deleteAttendanceClockList(List<Long> ids);

    /**
     * 获得打卡记录
     *
     * @param id 打卡记录编号
     * @return 打卡记录
     */
    HrmAttendanceClockDO getAttendanceClock(Long id);

    /**
     * 获得指定员工在打卡时间范围内的打卡记录
     *
     * @param employeeId 员工编号
     * @param clockTimes 打卡时间闭区间
     * @return 打卡记录列表
     */
    List<HrmAttendanceClockDO> getAttendanceClockListByEmployeeIdAndClockTime(
            Long employeeId, LocalDateTime[] clockTimes);

    /**
     * 获得指定员工在打卡时间范围内的打卡记录
     *
     * @param employeeIds 员工编号集合
     * @param clockTimes 打卡时间闭区间
     * @return 打卡记录列表
     */
    List<HrmAttendanceClockDO> getAttendanceClockListByEmployeeIdsAndClockTime(
            Collection<Long> employeeIds, LocalDateTime[] clockTimes);

    /**
     * 获得打卡记录分页
     *
     * @param pageReqVO 分页查询条件
     * @return 打卡记录分页
     */
    PageResult<HrmAttendanceClockDO> getAttendanceClockPage(HrmAttendanceClockPageReqVO pageReqVO);

    /**
     * 获得打卡记录列表
     *
     * @param reqVO 查询条件
     * @return 打卡记录列表
     */
    List<HrmAttendanceClockDO> getAttendanceClockList(HrmAttendanceClockPageReqVO reqVO);

}
