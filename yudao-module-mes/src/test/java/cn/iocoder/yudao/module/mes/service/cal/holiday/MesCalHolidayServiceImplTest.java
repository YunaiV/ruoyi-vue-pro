package cn.iocoder.yudao.module.mes.service.cal.holiday;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.cal.holiday.vo.MesCalHolidaySaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.holiday.MesCalHolidayDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.holiday.MesCalHolidayMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalHolidayTypeEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalPlanStatusEnum;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_HOLIDAY_PLAN_CONFIRMED;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesCalHolidayServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesCalHolidayServiceImpl.class)
public class MesCalHolidayServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesCalHolidayServiceImpl holidayService;

    @Resource
    private MesCalHolidayMapper holidayMapper;

    @MockitoBean
    private MesCalPlanService planService;

    @Test
    public void testSaveHoliday_addHolidayInConfirmedPlan() {
        // 准备参数
        MesCalHolidaySaveReqVO reqVO = randomPojo(MesCalHolidaySaveReqVO.class, o -> {
            o.setId(null); // 避免 id 被赋值
            o.setDay(LocalDateTime.of(2026, 10, 20, 0, 0));
            o.setType(MesCalHolidayTypeEnum.HOLIDAY.getType());
        });
        // mock planService 的方法：该日期在已确认的排班计划范围内
        MesCalPlanDO plan = randomPojo(MesCalPlanDO.class, o -> o.setStatus(MesCalPlanStatusEnum.CONFIRMED.getStatus()));
        when(planService.getPlanListByStatusAndDay(eq(MesCalPlanStatusEnum.CONFIRMED.getStatus()),
                eq(reqVO.getDay().toLocalDate()))).thenReturn(singletonList(plan));

        // 调用，并断言异常
        assertServiceException(() -> holidayService.saveHoliday(reqVO), CAL_HOLIDAY_PLAN_CONFIRMED,
                reqVO.getDay().toLocalDate(), plan.getName());
        // 断言：未写入假期设置
        assertNull(holidayMapper.selectByDay(reqVO.getDay()));
    }

    @Test
    public void testSaveHoliday_cancelHolidayInConfirmedPlan() {
        // mock 数据：已有节假日
        MesCalHolidayDO dbHoliday = randomPojo(MesCalHolidayDO.class, o -> {
            o.setId(null); // 避免 id 被赋值
            o.setDay(LocalDateTime.of(2026, 10, 20, 0, 0));
            o.setType(MesCalHolidayTypeEnum.HOLIDAY.getType());
        });
        holidayMapper.insert(dbHoliday);
        // 准备参数：改为工作日
        MesCalHolidaySaveReqVO reqVO = randomPojo(MesCalHolidaySaveReqVO.class, o -> {
            o.setId(null); // 避免 id 被赋值
            o.setDay(dbHoliday.getDay());
            o.setType(MesCalHolidayTypeEnum.WORKDAY.getType());
        });
        // mock planService 的方法：该日期在已确认的排班计划范围内
        MesCalPlanDO plan = randomPojo(MesCalPlanDO.class, o -> o.setStatus(MesCalPlanStatusEnum.CONFIRMED.getStatus()));
        when(planService.getPlanListByStatusAndDay(eq(MesCalPlanStatusEnum.CONFIRMED.getStatus()),
                eq(reqVO.getDay().toLocalDate()))).thenReturn(singletonList(plan));

        // 调用，并断言异常
        assertServiceException(() -> holidayService.saveHoliday(reqVO), CAL_HOLIDAY_PLAN_CONFIRMED,
                reqVO.getDay().toLocalDate(), plan.getName());
        // 断言：假期设置未被修改
        assertPojoEquals(dbHoliday, holidayMapper.selectById(dbHoliday.getId()));
    }

    @Test
    public void testSaveHoliday_updateRemarkInConfirmedPlan() {
        // mock 数据：已有节假日
        MesCalHolidayDO dbHoliday = randomPojo(MesCalHolidayDO.class, o -> {
            o.setId(null); // 避免 id 被赋值
            o.setDay(LocalDateTime.of(2026, 10, 20, 0, 0));
            o.setType(MesCalHolidayTypeEnum.HOLIDAY.getType());
        });
        holidayMapper.insert(dbHoliday);
        // 准备参数：仍为节假日，只修改备注
        MesCalHolidaySaveReqVO reqVO = randomPojo(MesCalHolidaySaveReqVO.class, o -> {
            o.setId(null); // 避免 id 被赋值
            o.setDay(dbHoliday.getDay());
            o.setType(MesCalHolidayTypeEnum.HOLIDAY.getType());
        });

        // 调用
        Long id = holidayService.saveHoliday(reqVO);
        // 断言：更新已有记录，且是否节假日未变化，无需校验排班计划
        assertEquals(dbHoliday.getId(), id);
        assertPojoEquals(reqVO, holidayMapper.selectById(id), "id");
        verify(planService, never()).getPlanListByStatusAndDay(any(), any());
    }

    @Test
    public void testSaveHoliday_createWithoutConfirmedPlan() {
        // 准备参数
        MesCalHolidaySaveReqVO reqVO = randomPojo(MesCalHolidaySaveReqVO.class, o -> {
            o.setId(null); // 避免 id 被赋值
            o.setDay(LocalDateTime.of(2026, 10, 20, 0, 0));
            o.setType(MesCalHolidayTypeEnum.HOLIDAY.getType());
        });
        // mock planService 的方法：该日期没有已确认的排班计划
        when(planService.getPlanListByStatusAndDay(eq(MesCalPlanStatusEnum.CONFIRMED.getStatus()),
                eq(reqVO.getDay().toLocalDate()))).thenReturn(emptyList());

        // 调用
        Long id = holidayService.saveHoliday(reqVO);
        // 断言
        assertPojoEquals(reqVO, holidayMapper.selectById(id), "id");
    }

}
