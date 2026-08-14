package cn.iocoder.yudao.module.hrm.service.home;

import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHomeCalendarItemRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmHrHomeStatisticsRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.home.vo.HrmTeamHomeStatisticsRespVO;

import java.time.LocalDate;
import java.util.List;

/**
 * HRM 首页 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmHomeService {

    /**
     * 获得 HR 工作台统计
     *
     * @return HR 工作台统计
     */
    HrmHrHomeStatisticsRespVO getHrHomeStatisticsSummary();

    /**
     * 获得当前登录员工的团队工作台统计
     *
     * @param loginUserId 登录用户编号
     * @return 团队工作台统计
     */
    HrmTeamHomeStatisticsRespVO getTeamHomeStatisticsSummary(Long loginUserId);

    /**
     * 获得 HR 工作台日期范围内的日历
     *
     * @param loginUserId 登录用户编号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日历事项列表
     */
    List<HrmHomeCalendarItemRespVO> getHrHomeCalendar(
            Long loginUserId, LocalDate startDate, LocalDate endDate);

    /**
     * 获得当前登录员工的团队日历
     *
     * @param loginUserId 登录用户编号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 团队日历事项列表
     */
    List<HrmHomeCalendarItemRespVO> getTeamHomeCalendar(
            Long loginUserId, LocalDate startDate, LocalDate endDate);

    /**
     * 获得员工端首页日期范围内的日历
     *
     * @param loginUserId 登录用户编号
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日历事项列表
     */
    List<HrmHomeCalendarItemRespVO> getEmployeeCalendar(
            Long loginUserId, LocalDate startDate, LocalDate endDate);

}
