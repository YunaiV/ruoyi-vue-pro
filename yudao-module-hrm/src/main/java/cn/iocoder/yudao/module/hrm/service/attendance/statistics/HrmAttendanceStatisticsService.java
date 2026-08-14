package cn.iocoder.yudao.module.hrm.service.attendance.statistics;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyDetailReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceDailyDetailRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthDailyOverviewRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthDetailRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.statistics.HrmAttendanceMonthRecordRespVO;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * HRM 考勤统计 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmAttendanceStatisticsService {

    /**
     * 获得员工考勤日详情
     *
     * @param reqVO 查询条件
     * @return 考勤日详情
     */
    HrmAttendanceDailyDetailRespVO getAttendanceDailyDetail(HrmAttendanceDailyDetailReqVO reqVO);

    /**
     * 获得员工月度考勤汇总分页
     *
     * @param reqVO 分页查询条件
     * @return 月度考勤汇总分页
     */
    PageResult<HrmAttendanceMonthRecordRespVO> getAttendanceMonthRecordPage(HrmAttendanceMonthRecordPageReqVO reqVO);

    /**
     * 获得员工月度考勤汇总列表
     *
     * @param reqVO 查询条件
     * @return 月度考勤汇总列表
     */
    List<HrmAttendanceMonthRecordRespVO> getAttendanceMonthRecordList(HrmAttendanceMonthRecordPageReqVO reqVO);

    /**
     * 获得指定员工的月度考勤汇总列表
     *
     * @param year 年份
     * @param month 月份
     * @param employeeIds 员工编号数组
     * @return 月度考勤汇总列表
     */
    List<HrmAttendanceMonthRecordRespVO> getAttendanceMonthRecordList(
            Integer year, Integer month, List<Long> employeeIds);

    /**
     * 获得指定员工的月度考勤汇总 Map
     *
     * @param year 年份
     * @param month 月份
     * @param employeeIds 员工编号数组
     * @return 员工编号与月度考勤汇总的映射
     */
    default Map<Long, HrmAttendanceMonthRecordRespVO> getAttendanceMonthRecordMap(
            Integer year, Integer month, List<Long> employeeIds) {
        return convertMap(getAttendanceMonthRecordList(year, month, employeeIds),
                HrmAttendanceMonthRecordRespVO::getEmployeeId);
    }

    /**
     * 获得员工月度打卡概况分页
     *
     * @param reqVO 分页查询条件
     * @return 月度打卡概况分页
     */
    PageResult<HrmAttendanceMonthDailyOverviewRespVO> getAttendanceMonthDailyOverviewPage(
            HrmAttendanceMonthRecordPageReqVO reqVO);

    /**
     * 获得员工月度打卡概况列表
     *
     * @param reqVO 查询条件
     * @return 月度打卡概况列表
     */
    List<HrmAttendanceMonthDailyOverviewRespVO> getAttendanceMonthDailyOverviewList(
            HrmAttendanceMonthRecordPageReqVO reqVO);

    /**
     * 获得员工考勤月详情
     *
     * @param employeeId 员工编号
     * @param year 年份
     * @param month 月份
     * @return 考勤月详情
     */
    HrmAttendanceMonthDetailRespVO getAttendanceMonthDetail(Long employeeId, Integer year, Integer month);

}
