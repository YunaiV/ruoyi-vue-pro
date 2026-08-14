package cn.iocoder.yudao.module.hrm.service.attendance.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.holiday.HrmAttendanceHolidayPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.attendance.vo.holiday.HrmAttendanceHolidaySaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.attendance.config.HrmAttendanceHolidayDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.attendance.config.HrmAttendanceHolidayMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_HOLIDAY_DATE_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.ATTENDANCE_HOLIDAY_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link HrmAttendanceHolidayServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmAttendanceHolidayServiceImpl.class)
public class HrmAttendanceHolidayServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmAttendanceHolidayServiceImpl attendanceHolidayService;

    @Resource
    private HrmAttendanceHolidayMapper attendanceHolidayMapper;

    @Test
    public void testCreateAttendanceHoliday_success() {
        // 准备参数
        HrmAttendanceHolidaySaveReqVO reqVO = new HrmAttendanceHolidaySaveReqVO();
        reqVO.setDate(LocalDateTime.of(2026, 10, 1, 9, 0));
        reqVO.setType(2);

        // 调用
        Long id = attendanceHolidayService.createAttendanceHoliday(reqVO);

        // 断言
        HrmAttendanceHolidayDO dbHoliday = attendanceHolidayMapper.selectById(id);
        assertPojoEquals(reqVO, dbHoliday, "id", "date");
        assertEquals(reqVO.getDate().toLocalDate().atStartOfDay(), dbHoliday.getDate());
    }

    @Test
    public void testCreateAttendanceHoliday_dateDuplicate() {
        // mock 数据
        HrmAttendanceHolidayDO dbHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 1, 9, 0), 2);
        attendanceHolidayMapper.insert(dbHoliday);
        // 准备参数
        HrmAttendanceHolidaySaveReqVO reqVO = new HrmAttendanceHolidaySaveReqVO();
        reqVO.setDate(LocalDateTime.of(2026, 10, 1, 18, 0));
        reqVO.setType(1);

        // 调用，并断言异常
        assertServiceException(() -> attendanceHolidayService.createAttendanceHoliday(reqVO),
                ATTENDANCE_HOLIDAY_DATE_DUPLICATE);
    }

    @Test
    public void testUpdateAttendanceHoliday_success() {
        // mock 数据
        HrmAttendanceHolidayDO dbHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 1, 9, 0), 2);
        attendanceHolidayMapper.insert(dbHoliday);
        // 准备参数
        HrmAttendanceHolidaySaveReqVO reqVO = new HrmAttendanceHolidaySaveReqVO();
        reqVO.setId(dbHoliday.getId());
        reqVO.setDate(LocalDateTime.of(2026, 10, 2, 9, 0));
        reqVO.setType(1);

        // 调用
        attendanceHolidayService.updateAttendanceHoliday(reqVO);

        // 断言
        HrmAttendanceHolidayDO updatedHoliday = attendanceHolidayMapper.selectById(dbHoliday.getId());
        assertPojoEquals(reqVO, updatedHoliday, "date");
        assertEquals(reqVO.getDate().toLocalDate().atStartOfDay(), updatedHoliday.getDate());
    }

    @Test
    public void testUpdateAttendanceHoliday_notExists() {
        // 准备参数
        HrmAttendanceHolidaySaveReqVO reqVO = new HrmAttendanceHolidaySaveReqVO();
        reqVO.setId(randomLongId());
        reqVO.setDate(LocalDateTime.of(2026, 10, 1, 9, 0));
        reqVO.setType(2);

        // 调用，并断言异常
        assertServiceException(() -> attendanceHolidayService.updateAttendanceHoliday(reqVO),
                ATTENDANCE_HOLIDAY_NOT_EXISTS);
    }

    @Test
    public void testUpdateAttendanceHoliday_dateDuplicate() {
        // mock 数据
        HrmAttendanceHolidayDO firstHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 1, 9, 0), 2);
        attendanceHolidayMapper.insert(firstHoliday);
        HrmAttendanceHolidayDO secondHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 2, 9, 0), 1);
        attendanceHolidayMapper.insert(secondHoliday);
        // 准备参数
        HrmAttendanceHolidaySaveReqVO reqVO = new HrmAttendanceHolidaySaveReqVO();
        reqVO.setId(firstHoliday.getId());
        reqVO.setDate(LocalDateTime.of(2026, 10, 2, 18, 0));
        reqVO.setType(2);

        // 调用，并断言异常
        assertServiceException(() -> attendanceHolidayService.updateAttendanceHoliday(reqVO),
                ATTENDANCE_HOLIDAY_DATE_DUPLICATE);
    }

    @Test
    public void testDeleteAttendanceHoliday_success() {
        // mock 数据
        HrmAttendanceHolidayDO dbHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 1, 9, 0), 2);
        attendanceHolidayMapper.insert(dbHoliday);

        // 调用
        attendanceHolidayService.deleteAttendanceHoliday(dbHoliday.getId());

        // 断言
        assertNull(attendanceHolidayMapper.selectById(dbHoliday.getId()));
    }

    @Test
    public void testDeleteAttendanceHoliday_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> attendanceHolidayService.deleteAttendanceHoliday(id),
                ATTENDANCE_HOLIDAY_NOT_EXISTS);
    }

    @Test
    public void testGetAttendanceHolidayPage() {
        // mock 数据
        HrmAttendanceHolidayDO selectedHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 2, 9, 0), 2);
        attendanceHolidayMapper.insert(selectedHoliday);
        attendanceHolidayMapper.insert(randomAttendanceHoliday(
                LocalDateTime.of(2026, 9, 30, 9, 0), 2));
        attendanceHolidayMapper.insert(randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 3, 9, 0), 1));
        HrmAttendanceHolidayDO endBoundaryHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 8, 0, 0), 2);
        attendanceHolidayMapper.insert(endBoundaryHoliday);
        attendanceHolidayMapper.insert(randomPojo(HrmAttendanceHolidayDO.class,
                holiday -> holiday.setId(null).setDate(LocalDateTime.of(2026, 10, 8, 0, 0, 1))
                        .setType(2).setDeleted(false)));
        // 准备参数
        HrmAttendanceHolidayPageReqVO reqVO = new HrmAttendanceHolidayPageReqVO();
        reqVO.setDate(new LocalDateTime[]{LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 10, 8, 0, 0)});
        reqVO.setType(2);

        // 调用
        PageResult<HrmAttendanceHolidayDO> pageResult = attendanceHolidayService.getAttendanceHolidayPage(reqVO);

        // 断言
        assertEquals(2, pageResult.getTotal());
        assertEquals(endBoundaryHoliday.getId(), pageResult.getList().get(0).getId());
        assertPojoEquals(selectedHoliday, pageResult.getList().get(1));
    }

    @Test
    public void testGetAttendanceHolidayListByDateRange() {
        // mock 数据
        HrmAttendanceHolidayDO firstHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 1, 9, 0), 2);
        attendanceHolidayMapper.insert(firstHoliday);
        HrmAttendanceHolidayDO secondHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 2, 18, 0), 1);
        attendanceHolidayMapper.insert(secondHoliday);
        HrmAttendanceHolidayDO endBoundaryHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 8, 9, 0), 2);
        attendanceHolidayMapper.insert(endBoundaryHoliday);
        attendanceHolidayMapper.insert(randomPojo(HrmAttendanceHolidayDO.class,
                holiday -> holiday.setId(null).setDate(LocalDateTime.of(2026, 10, 8, 0, 0, 1))
                        .setType(2).setDeleted(false)));

        // 调用
        List<HrmAttendanceHolidayDO> result = attendanceHolidayService.getAttendanceHolidayListByDateRange(
                new LocalDateTime[]{LocalDateTime.of(2026, 10, 1, 0, 0),
                        LocalDateTime.of(2026, 10, 8, 0, 0)});

        // 断言
        assertEquals(3, result.size());
        assertEquals(firstHoliday.getId(), result.get(0).getId());
        assertEquals(secondHoliday.getId(), result.get(1).getId());
        assertEquals(endBoundaryHoliday.getId(), result.get(2).getId());
    }

    @Test
    public void testGetAttendanceHolidayMap() {
        // mock 数据
        HrmAttendanceHolidayDO selectedHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 2, 9, 0), 2);
        attendanceHolidayMapper.insert(selectedHoliday);
        HrmAttendanceHolidayDO latestHoliday = randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 2, 18, 0), 1);
        attendanceHolidayMapper.insert(latestHoliday);
        attendanceHolidayMapper.insert(randomAttendanceHoliday(
                LocalDateTime.of(2026, 10, 8, 9, 0), 1));

        // 调用
        Map<LocalDate, HrmAttendanceHolidayDO> result = attendanceHolidayService.getAttendanceHolidayMap(
                new LocalDateTime[]{LocalDate.of(2026, 10, 1).atStartOfDay(),
                        LocalDate.of(2026, 10, 8).atStartOfDay()});

        // 断言
        assertEquals(2, result.size());
        assertEquals(latestHoliday.getId(), result.get(LocalDate.of(2026, 10, 2)).getId());
        assertEquals(LocalDate.of(2026, 10, 8), result.get(LocalDate.of(2026, 10, 8)).getDate().toLocalDate());
        assertEquals(latestHoliday.getId(), attendanceHolidayService.checkAttendanceHoliday(
                LocalDateTime.of(2026, 10, 2, 23, 59)).getId());
    }

    // ========== 随机对象 ==========

    private HrmAttendanceHolidayDO randomAttendanceHoliday(LocalDateTime date, Integer type) {
        return randomPojo(HrmAttendanceHolidayDO.class,
                o -> o.setId(null).setDate(date.toLocalDate().atStartOfDay()).setType(type).setDeleted(false));
    }

}
