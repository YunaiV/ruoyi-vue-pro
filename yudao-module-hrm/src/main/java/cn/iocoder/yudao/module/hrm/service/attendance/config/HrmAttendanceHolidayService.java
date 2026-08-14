package cn.iocoder.yudao.module.hrm.service.attendance.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.holiday.HrmAttendanceHolidayPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.holiday.HrmAttendanceHolidaySaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceHolidayDO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * HRM 考勤节假日 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmAttendanceHolidayService {

    /**
     * 创建考勤节假日
     *
     * @param createReqVO 节假日信息
     * @return 节假日编号
     */
    Long createAttendanceHoliday(HrmAttendanceHolidaySaveReqVO createReqVO);

    /**
     * 更新考勤节假日
     *
     * @param updateReqVO 节假日信息
     */
    void updateAttendanceHoliday(HrmAttendanceHolidaySaveReqVO updateReqVO);

    /**
     * 删除考勤节假日
     *
     * @param id 节假日编号
     */
    void deleteAttendanceHoliday(Long id);

    /**
     * 获得考勤节假日
     *
     * @param id 节假日编号
     * @return 节假日
     */
    HrmAttendanceHolidayDO getAttendanceHoliday(Long id);

    /**
     * 检查指定日期的考勤节假日
     *
     * @param date 日期
     * @return 节假日
     */
    HrmAttendanceHolidayDO checkAttendanceHoliday(LocalDateTime date);

    /**
     * 获得日期范围内的考勤节假日列表
     *
     * @param dateTimes 日期时间闭区间
     * @return 节假日列表
     */
    List<HrmAttendanceHolidayDO> getAttendanceHolidayListByDateRange(
            LocalDateTime[] dateTimes);

    /**
     * 获得日期范围内的考勤节假日 Map
     *
     * @param dateTimes 日期时间闭区间
     * @return 节假日 Map
     */
    Map<LocalDate, HrmAttendanceHolidayDO> getAttendanceHolidayMap(LocalDateTime[] dateTimes);

    /**
     * 获得考勤节假日分页
     *
     * @param pageReqVO 分页查询条件
     * @return 节假日分页
     */
    PageResult<HrmAttendanceHolidayDO> getAttendanceHolidayPage(HrmAttendanceHolidayPageReqVO pageReqVO);

}
