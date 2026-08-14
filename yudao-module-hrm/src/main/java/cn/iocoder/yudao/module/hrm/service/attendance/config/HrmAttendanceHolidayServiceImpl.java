package cn.iocoder.yudao.module.hrm.service.attendance.config;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.holiday.HrmAttendanceHolidayPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.holiday.HrmAttendanceHolidaySaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceHolidayDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.attendance.config.HrmAttendanceHolidayMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDayBeginTime;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_HOLIDAY_DATE_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_HOLIDAY_NOT_EXISTS;

/**
 * HRM 考勤节假日 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmAttendanceHolidayServiceImpl implements HrmAttendanceHolidayService {

    @Resource
    private HrmAttendanceHolidayMapper attendanceHolidayMapper;

    @Override
    public Long createAttendanceHoliday(HrmAttendanceHolidaySaveReqVO createReqVO) {
        // 1. 校验日期未配置
        LocalDateTime date = getDayBeginTime(createReqVO.getDate());
        validateAttendanceHolidayDateUnique(null, date);

        // 2. 新增节假日配置
        HrmAttendanceHolidayDO holiday = BeanUtils.toBean(createReqVO, HrmAttendanceHolidayDO.class)
                .setDate(date);
        attendanceHolidayMapper.insert(holiday);
        return holiday.getId();
    }

    @Override
    public void updateAttendanceHoliday(HrmAttendanceHolidaySaveReqVO updateReqVO) {
        // 1. 校验节假日配置存在
        validateAttendanceHolidayExists(updateReqVO.getId());
        LocalDateTime date = getDayBeginTime(updateReqVO.getDate());
        validateAttendanceHolidayDateUnique(updateReqVO.getId(), date);

        // 2. 更新节假日配置
        attendanceHolidayMapper.updateById(BeanUtils.toBean(updateReqVO, HrmAttendanceHolidayDO.class)
                .setDate(date));
    }

    @Override
    public void deleteAttendanceHoliday(Long id) {
        // 1. 校验节假日配置存在
        validateAttendanceHolidayExists(id);

        // 2. 删除节假日配置
        attendanceHolidayMapper.deleteById(id);
    }

    @Override
    public HrmAttendanceHolidayDO getAttendanceHoliday(Long id) {
        return attendanceHolidayMapper.selectById(id);
    }

    @Override
    public HrmAttendanceHolidayDO checkAttendanceHoliday(LocalDateTime date) {
        return attendanceHolidayMapper.selectByDate(getDayBeginTime(date));
    }

    @Override
    public List<HrmAttendanceHolidayDO> getAttendanceHolidayListByDateRange(
            LocalDateTime[] dateTimes) {
        return attendanceHolidayMapper.selectListByDateRange(dateTimes);
    }

    @Override
    public Map<LocalDate, HrmAttendanceHolidayDO> getAttendanceHolidayMap(LocalDateTime[] dateTimes) {
        List<HrmAttendanceHolidayDO> list = getAttendanceHolidayListByDateRange(dateTimes);
        return convertMap(list, holiday -> holiday.getDate().toLocalDate());
    }

    @Override
    public PageResult<HrmAttendanceHolidayDO> getAttendanceHolidayPage(HrmAttendanceHolidayPageReqVO pageReqVO) {
        return attendanceHolidayMapper.selectPage(pageReqVO);
    }

    private void validateAttendanceHolidayExists(Long id) {
        if (attendanceHolidayMapper.selectById(id) == null) {
            throw exception(ATTENDANCE_HOLIDAY_NOT_EXISTS);
        }
    }

    private void validateAttendanceHolidayDateUnique(Long id, LocalDateTime date) {
        HrmAttendanceHolidayDO holiday = attendanceHolidayMapper.selectByDate(date);
        if (holiday != null && ObjUtil.notEqual(holiday.getId(), id)) {
            throw exception(ATTENDANCE_HOLIDAY_DATE_DUPLICATE);
        }
    }

}
