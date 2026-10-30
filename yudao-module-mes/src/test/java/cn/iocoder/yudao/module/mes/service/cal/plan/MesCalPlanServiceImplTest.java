package cn.iocoder.yudao.module.mes.service.cal.plan;

import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.plan.MesCalPlanMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalPlanStatusEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftMethodEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamShiftService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_PLAN_NOT_PREPARE;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_PLAN_TEAM_COUNT_NOT_MATCH;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_REST_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesCalPlanServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesCalPlanServiceImpl.class)
public class MesCalPlanServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesCalPlanServiceImpl planService;

    @Resource
    private MesCalPlanMapper planMapper;

    @MockBean
    private MesCalPlanShiftService planShiftService;
    @MockBean
    private MesCalPlanTeamService planTeamService;
    @MockBean
    private MesCalTeamShiftService teamShiftService;

    @Test
    public void testCreatePlan_singleShift_addDefaultShift() {
        // 准备参数
        MesCalPlanSaveReqVO reqVO = new MesCalPlanSaveReqVO();
        reqVO.setCode("PLAN-SINGLE-001");
        reqVO.setName("单白班默认班次");
        reqVO.setCalendarType(1);
        reqVO.setStartDate(LocalDateTime.of(2026, 4, 1, 0, 0));
        reqVO.setEndDate(LocalDateTime.of(2026, 4, 30, 0, 0));
        reqVO.setShiftType(MesCalShiftTypeEnum.SINGLE.getType());
        reqVO.setShiftMethod(MesCalShiftMethodEnum.DAY.getMethod());
        reqVO.setShiftCount(1);

        // 调用
        Long planId = planService.createPlan(reqVO);

        // 断言 1：计划状态为草稿
        MesCalPlanDO plan = planMapper.selectById(planId);
        assertEquals(MesCalPlanStatusEnum.PREPARE.getStatus(), plan.getStatus());
        // 断言 2：自动生成默认班次
        verify(planShiftService).addDefaultPlanShift(planId, MesCalShiftTypeEnum.SINGLE.getType());
    }

    @Test
    public void testConfirmPlan_syncGenerateTeamShiftRecords() {
        // mock 数据：插入一条草稿状态的排班计划
        MesCalPlanDO plan = randomPojo(MesCalPlanDO.class, o -> {
            o.setCalendarType(1);
            o.setStartDate(LocalDateTime.of(2026, 4, 1, 0, 0));
            o.setEndDate(LocalDateTime.of(2026, 4, 3, 0, 0));
            o.setStatus(MesCalPlanStatusEnum.PREPARE.getStatus());
            o.setShiftType(MesCalShiftTypeEnum.SINGLE.getType());
            o.setShiftMethod(MesCalShiftMethodEnum.DAY.getMethod());
            o.setShiftCount(1);
        });
        planMapper.insert(plan);
        // mock 方法：班组数量满足要求
        when(planTeamService.getPlanTeamCountByPlanId(plan.getId())).thenReturn(1L);

        // 调用
        planService.confirmPlan(plan.getId());

        // 断言 1：计划状态更新为已确认
        MesCalPlanDO updatePlan = planMapper.selectById(plan.getId());
        assertEquals(MesCalPlanStatusEnum.CONFIRMED.getStatus(), updatePlan.getStatus());
        // 断言 2：触发生成班组排班记录
        verify(teamShiftService).generateTeamShiftRecords(plan.getId());
        // 断言 3：重复确认被拒绝，且不会再次生成班组排班记录
        assertServiceException(() -> planService.confirmPlan(plan.getId()), CAL_PLAN_NOT_PREPARE);
        verify(teamShiftService, times(1)).generateTeamShiftRecords(plan.getId());
    }

    @Test
    public void testConfirmPlan_generateFailed_rollbackStatus() {
        // mock 数据：插入一条草稿状态的两班倒排班计划
        MesCalPlanDO plan = randomPojo(MesCalPlanDO.class, o -> {
            o.setCalendarType(1);
            o.setStartDate(LocalDateTime.of(2026, 10, 19, 0, 0));
            o.setEndDate(LocalDateTime.of(2026, 10, 21, 0, 0));
            o.setStatus(MesCalPlanStatusEnum.PREPARE.getStatus());
            o.setShiftType(MesCalShiftTypeEnum.TWO.getType());
            o.setShiftMethod(MesCalShiftMethodEnum.DAY.getMethod());
            o.setShiftCount(1);
        });
        planMapper.insert(plan);
        // mock 方法：班组数量满足要求，但生成排班时校验到没有休息间隔
        when(planTeamService.getPlanTeamCountByPlanId(plan.getId())).thenReturn(2L);
        doThrow(exception(CAL_TEAM_SHIFT_GENERATE_REST_INVALID, "乙班",
                LocalDate.of(2026, 10, 19), "夜班", LocalDate.of(2026, 10, 20), "白班"))
                .when(teamShiftService).generateTeamShiftRecords(plan.getId());

        // 调用，并断言异常
        assertServiceException(() -> planService.confirmPlan(plan.getId()), CAL_TEAM_SHIFT_GENERATE_REST_INVALID,
                "乙班", LocalDate.of(2026, 10, 19), "夜班", LocalDate.of(2026, 10, 20), "白班");
        // 断言：事务回滚，计划仍为草稿
        MesCalPlanDO dbPlan = planMapper.selectById(plan.getId());
        assertEquals(MesCalPlanStatusEnum.PREPARE.getStatus(), dbPlan.getStatus());
    }

    @Test
    public void testConfirmPlan_teamCountExceed() {
        // mock 数据：插入一条草稿状态的两班倒排班计划
        MesCalPlanDO plan = randomPojo(MesCalPlanDO.class, o -> {
            o.setCalendarType(1);
            o.setStartDate(LocalDateTime.of(2026, 10, 19, 0, 0));
            o.setEndDate(LocalDateTime.of(2026, 10, 21, 0, 0));
            o.setStatus(MesCalPlanStatusEnum.PREPARE.getStatus());
            o.setShiftType(MesCalShiftTypeEnum.TWO.getType());
            o.setShiftMethod(MesCalShiftMethodEnum.DAY.getMethod());
            o.setShiftCount(1);
        });
        planMapper.insert(plan);
        // mock 方法：分配了 3 个班组，多出的班组不会参与排班
        when(planTeamService.getPlanTeamCountByPlanId(plan.getId())).thenReturn(3L);

        // 调用，并断言异常
        assertServiceException(() -> planService.confirmPlan(plan.getId()), CAL_PLAN_TEAM_COUNT_NOT_MATCH);
        // 断言：计划仍为草稿，且未生成排班记录
        MesCalPlanDO dbPlan = planMapper.selectById(plan.getId());
        assertEquals(MesCalPlanStatusEnum.PREPARE.getStatus(), dbPlan.getStatus());
        verify(teamShiftService, never()).generateTeamShiftRecords(plan.getId());
    }

    @Test
    public void testGetPlanListByStatusAndDay() {
        // mock 数据：已确认，结束日期为当天
        MesCalPlanDO plan01 = randomPlan(LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 10, 19, 0, 0), MesCalPlanStatusEnum.CONFIRMED.getStatus());
        planMapper.insert(plan01);
        // mock 数据：已确认，开始日期为当天
        MesCalPlanDO plan02 = randomPlan(LocalDateTime.of(2026, 10, 19, 0, 0),
                LocalDateTime.of(2026, 10, 31, 0, 0), MesCalPlanStatusEnum.CONFIRMED.getStatus());
        planMapper.insert(plan02);
        // mock 数据：已确认，但不包含当天
        planMapper.insert(randomPlan(LocalDateTime.of(2026, 10, 20, 0, 0),
                LocalDateTime.of(2026, 10, 31, 0, 0), MesCalPlanStatusEnum.CONFIRMED.getStatus()));
        // mock 数据：包含当天，但为草稿
        planMapper.insert(randomPlan(LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 10, 31, 0, 0), MesCalPlanStatusEnum.PREPARE.getStatus()));

        // 调用
        List<MesCalPlanDO> list = planService.getPlanListByStatusAndDay(
                MesCalPlanStatusEnum.CONFIRMED.getStatus(), LocalDate.of(2026, 10, 19));

        // 断言：只返回包含当天的已确认计划（起止日期为当天也算）
        assertEquals(SetUtils.asSet(plan01.getId(), plan02.getId()), convertSet(list, MesCalPlanDO::getId));
    }

    @Test
    public void testGetPlanListByStatusAndDay_boundary() {
        Integer status = MesCalPlanStatusEnum.CONFIRMED.getStatus();
        // mock 数据：开始时间为当天午夜前的最后时刻（H2 timestamp 精度为微秒）
        MesCalPlanDO lastMicro = randomPlan(LocalDateTime.of(2026, 10, 19, 23, 59, 59, 999_999_000),
                LocalDateTime.of(2026, 10, 31, 0, 0), status);
        planMapper.insert(lastMicro);
        MesCalPlanDO lastSecond = randomPlan(LocalDateTime.of(2026, 10, 19, 23, 59, 59),
                LocalDateTime.of(2026, 10, 31, 0, 0), status);
        planMapper.insert(lastSecond);
        // mock 数据：开始时间为次日零点，不包含当天
        planMapper.insert(randomPlan(LocalDateTime.of(2026, 10, 20, 0, 0),
                LocalDateTime.of(2026, 10, 31, 0, 0), status));
        // mock 数据：结束时间为当天零点，包含当天
        MesCalPlanDO endAtStart = randomPlan(LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 10, 19, 0, 0), status);
        planMapper.insert(endAtStart);
        // mock 数据：结束时间为当天零点之前，不包含当天
        planMapper.insert(randomPlan(LocalDateTime.of(2026, 10, 1, 0, 0),
                LocalDateTime.of(2026, 10, 18, 23, 59, 59, 999_999_000), status));

        // 调用
        List<MesCalPlanDO> list = planService.getPlanListByStatusAndDay(status, LocalDate.of(2026, 10, 19));

        // 断言：午夜前的时刻、结束时间为当天零点均包含；次日零点、前一天均不包含；按 id 升序返回
        assertEquals(SetUtils.asSet(lastMicro.getId(), lastSecond.getId(), endAtStart.getId()),
                convertSet(list, MesCalPlanDO::getId));
        List<Long> ids = list.stream().map(MesCalPlanDO::getId).collect(java.util.stream.Collectors.toList());
        assertEquals(ids.stream().sorted().collect(java.util.stream.Collectors.toList()), ids);
    }

    @Test
    public void testDeletePlan_notCascadeDeleteTeamShift() {
        // mock 数据：插入一条草稿状态的排班计划
        MesCalPlanDO plan = randomPojo(MesCalPlanDO.class, o -> {
            o.setCalendarType(1);
            o.setStartDate(LocalDateTime.of(2026, 4, 1, 0, 0));
            o.setEndDate(LocalDateTime.of(2026, 4, 3, 0, 0));
            o.setStatus(MesCalPlanStatusEnum.PREPARE.getStatus());
            o.setShiftType(MesCalShiftTypeEnum.SINGLE.getType());
            o.setShiftMethod(MesCalShiftMethodEnum.DAY.getMethod());
            o.setShiftCount(1);
        });
        planMapper.insert(plan);

        // 调用
        planService.deletePlan(plan.getId());

        // 断言 1：计划被删除
        assertNull(planMapper.selectById(plan.getId()));
        // 断言 2：级联删除班次和班组关联
        verify(planShiftService).deletePlanShiftByPlanId(plan.getId());
        verify(planTeamService).deleteByPlanId(plan.getId());
        // 断言 3：不级联删除班组排班记录
        verify(teamShiftService, never()).deleteByPlanId(plan.getId());
    }

    private static MesCalPlanDO randomPlan(LocalDateTime startDate, LocalDateTime endDate, Integer status) {
        return randomPojo(MesCalPlanDO.class, o -> {
            o.setCalendarType(1);
            o.setStartDate(startDate);
            o.setEndDate(endDate);
            o.setStatus(status);
            o.setShiftType(MesCalShiftTypeEnum.TWO.getType());
            o.setShiftMethod(MesCalShiftMethodEnum.DAY.getMethod());
            o.setShiftCount(1);
        });
    }

}
