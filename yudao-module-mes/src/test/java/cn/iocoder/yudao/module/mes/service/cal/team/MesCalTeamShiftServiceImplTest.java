package cn.iocoder.yudao.module.mes.service.cal.team;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.holiday.MesCalHolidayDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamShiftDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.team.MesCalTeamShiftMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalHolidayTypeEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftMethodEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
import cn.iocoder.yudao.module.mes.service.cal.holiday.MesCalHolidayService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanShiftService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanTeamService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_REST_INVALID;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.CAL_TEAM_SHIFT_GENERATE_SHIFT_TIME_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesCalTeamShiftServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesCalTeamShiftServiceImpl.class)
public class MesCalTeamShiftServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesCalTeamShiftServiceImpl teamShiftService;

    @Resource
    private MesCalTeamShiftMapper teamShiftMapper;

    @MockBean
    private MesCalPlanService planService;
    @MockBean
    private MesCalPlanShiftService planShiftService;
    @MockBean
    private MesCalPlanTeamService planTeamService;
    @MockBean
    private MesCalHolidayService holidayService;
    @MockBean
    private MesCalTeamService teamService;

    @Test
    public void testGenerateTeamShiftRecords_threeShift_rotateThreeStates() {
        // 准备参数
        Long planId = 3001L;
        // mock 方法：排班计划（三班倒、按天轮班、3 天）
        MesCalPlanDO plan = MesCalPlanDO.builder()
                .id(planId)
                .code("PLAN-THREE-001")
                .name("三班倒轮转")
                .calendarType(1)
                .startDate(LocalDateTime.of(2026, 4, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 4, 3, 0, 0))
                .shiftType(MesCalShiftTypeEnum.THREE.getType())
                .shiftMethod(MesCalShiftMethodEnum.DAY.getMethod())
                .shiftCount(1)
                .build();
        List<MesCalPlanShiftDO> shifts = ListUtil.of(
                MesCalPlanShiftDO.builder().id(11L).planId(planId).sort(1).name("白班").startTime("08:00").endTime("16:00").build(),
                MesCalPlanShiftDO.builder().id(12L).planId(planId).sort(2).name("中班").startTime("16:00").endTime("00:00").build(),
                MesCalPlanShiftDO.builder().id(13L).planId(planId).sort(3).name("夜班").startTime("00:00").endTime("08:00").build()
        );
        List<MesCalPlanTeamDO> teams = ListUtil.of(
                MesCalPlanTeamDO.builder().id(21L).planId(planId).teamId(101L).build(),
                MesCalPlanTeamDO.builder().id(22L).planId(planId).teamId(102L).build(),
                MesCalPlanTeamDO.builder().id(23L).planId(planId).teamId(103L).build()
        );
        when(planService.getPlan(planId)).thenReturn(plan);
        when(planShiftService.getPlanShiftListByPlanId(planId)).thenReturn(shifts);
        when(planTeamService.getPlanTeamListByPlanId(planId)).thenReturn(teams);

        // 调用
        teamShiftService.generateTeamShiftRecords(planId);

        // 断言：3 天 × 3 班次 = 9 条排班记录，班组按 白班 → 夜班 → 中班 轮转
        // 说明：原先按 白班 → 中班 → 夜班 轮转时，B 组 4/1 中班 24:00 下班后会立即接 4/2 00:00 的夜班，没有休息间隔
        List<MesCalTeamShiftDO> records = getRecords(planId);
        assertEquals(9, records.size());
        // 断言：第 1 天（shiftIndex=0）：A 白班、B 中班、C 夜班
        assertRecord(records.get(0), LocalDateTime.of(2026, 4, 1, 0, 0), 1, 101L, 11L);
        assertRecord(records.get(1), LocalDateTime.of(2026, 4, 1, 0, 0), 2, 102L, 12L);
        assertRecord(records.get(2), LocalDateTime.of(2026, 4, 1, 0, 0), 3, 103L, 13L);
        // 断言：第 2 天（shiftIndex=1）：B 白班、C 中班、A 夜班（A、B 休息 8 小时，C 休息 32 小时）
        assertRecord(records.get(3), LocalDateTime.of(2026, 4, 2, 0, 0), 1, 102L, 11L);
        assertRecord(records.get(4), LocalDateTime.of(2026, 4, 2, 0, 0), 2, 103L, 12L);
        assertRecord(records.get(5), LocalDateTime.of(2026, 4, 2, 0, 0), 3, 101L, 13L);
        // 断言：第 3 天（shiftIndex=2）：C 白班、A 中班、B 夜班
        assertRecord(records.get(6), LocalDateTime.of(2026, 4, 3, 0, 0), 1, 103L, 11L);
        assertRecord(records.get(7), LocalDateTime.of(2026, 4, 3, 0, 0), 2, 101L, 12L);
        assertRecord(records.get(8), LocalDateTime.of(2026, 4, 3, 0, 0), 3, 102L, 13L);
    }

    @Test
    public void testGenerateTeamShiftRecords_singleWithoutRotation() {
        Long planId = 3002L;
        LocalDateTime start = LocalDateTime.of(2026, 9, 21, 0, 0);
        when(planService.getPlan(planId)).thenReturn(MesCalPlanDO.builder()
                .id(planId).code("SINGLE-001").name("单白班")
                .calendarType(1).startDate(start).endDate(start.plusDays(1))
                .shiftType(MesCalShiftTypeEnum.SINGLE.getType())
                .shiftMethod(null).shiftCount(null).build());
        when(planShiftService.getPlanShiftListByPlanId(planId)).thenReturn(ListUtil.of(
                MesCalPlanShiftDO.builder().id(11L).planId(planId).sort(1).name("白班")
                        .startTime("08:00").endTime("16:00").build()));
        when(planTeamService.getPlanTeamListByPlanId(planId)).thenReturn(ListUtil.of(
                MesCalPlanTeamDO.builder().id(21L).planId(planId).teamId(101L).build()));

        teamShiftService.generateTeamShiftRecords(planId);

        List<MesCalTeamShiftDO> records = teamShiftMapper.selectListByPlanId(planId);
        records.sort(Comparator.comparing(MesCalTeamShiftDO::getDay));
        assertEquals(2, records.size());
        assertRecord(records.get(0), start, 1, 101L, 11L);
        assertRecord(records.get(1), start.plusDays(1), 1, 101L, 11L);
    }

    @Test
    public void testGenerateTeamShiftRecords_singleCrossMidnight() {
        // 准备参数
        Long planId = 3003L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 21);
        // mock 方法：单班次 22:00~06:00，跨午夜到次日结束，每天之间休息 16 小时
        mockPlan(planId, MesCalShiftTypeEnum.SINGLE, null, null, startDate, endDate);
        when(planShiftService.getPlanShiftListByPlanId(planId)).thenReturn(ListUtil.of(
                MesCalPlanShiftDO.builder().id(11L).planId(planId).sort(1).name("夜班")
                        .startTime("22:00").endTime("06:00").build()));
        mockTeams(planId, 101L);

        // 调用
        teamShiftService.generateTeamShiftRecords(planId);

        // 断言：每天 1 条排班记录
        List<MesCalTeamShiftDO> records = getRecords(planId);
        assertEquals(3, records.size());
        assertRecord(records.get(0), startDate.atStartOfDay(), 1, 101L, 11L);
        assertRecord(records.get(1), startDate.plusDays(1).atStartOfDay(), 1, 101L, 11L);
        assertRecord(records.get(2), endDate.atStartOfDay(), 1, 101L, 11L);
    }

    @Test
    public void testGenerateTeamShiftRecords_twoShiftFullDay_rotateWithoutRest() {
        // 准备参数
        Long planId = 3004L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 21);
        // mock 方法：两班倒、按天倒班 1 天，默认班次 08:00~20:00 / 20:00~08:00 覆盖全天
        mockPlan(planId, MesCalShiftTypeEnum.TWO, MesCalShiftMethodEnum.DAY, 1, startDate, endDate);
        mockTwoShifts(planId, "08:00", "20:00", "20:00", "08:00");
        mockTeams(planId, 101L, 102L);
        when(teamService.getTeam(eq(102L))).thenReturn(MesCalTeamDO.builder().id(102L).name("乙班").build());

        // 调用，并断言异常：B 组 10-19 夜班次日 08:00 下班后，立即接 10-20 08:00 的白班，连续上班 24 小时
        assertServiceException(() -> teamShiftService.generateTeamShiftRecords(planId),
                CAL_TEAM_SHIFT_GENERATE_REST_INVALID, "乙班",
                LocalDate.of(2026, 10, 19), "夜班", LocalDate.of(2026, 10, 20), "白班");
        // 断言：没有写入任何排班记录
        assertTrue(teamShiftMapper.selectListByPlanId(planId).isEmpty());
    }

    @Test
    public void testGenerateTeamShiftRecords_twoShiftFullDay_longerShiftCountOnlyDelay() {
        // 准备参数
        Long planId = 3005L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 23);
        // mock 方法：两班倒、按天倒班 3 天，默认班次覆盖全天
        mockPlan(planId, MesCalShiftTypeEnum.TWO, MesCalShiftMethodEnum.DAY, 3, startDate, endDate);
        mockTwoShifts(planId, "08:00", "20:00", "20:00", "08:00");
        mockTeams(planId, 101L, 102L);
        when(teamService.getTeam(eq(102L))).thenReturn(MesCalTeamDO.builder().id(102L).name("乙班").build());

        // 调用，并断言异常：增加倒班天数只是推迟到 10-22 换班，B 组仍然夜班下班后立即接白班
        assertServiceException(() -> teamShiftService.generateTeamShiftRecords(planId),
                CAL_TEAM_SHIFT_GENERATE_REST_INVALID, "乙班",
                LocalDate.of(2026, 10, 21), "夜班", LocalDate.of(2026, 10, 22), "白班");
        // 断言：没有写入任何排班记录
        assertTrue(teamShiftMapper.selectListByPlanId(planId).isEmpty());
    }

    @Test
    public void testGenerateTeamShiftRecords_twoShiftOverlap() {
        // 准备参数
        Long planId = 3006L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 20);
        // mock 方法：两班倒、按天倒班 1 天，夜班 20:00~08:30 与次日 08:00 的白班重叠
        mockPlan(planId, MesCalShiftTypeEnum.TWO, MesCalShiftMethodEnum.DAY, 1, startDate, endDate);
        mockTwoShifts(planId, "08:00", "20:00", "20:00", "08:30");
        mockTeams(planId, 101L, 102L);
        when(teamService.getTeam(eq(102L))).thenReturn(MesCalTeamDO.builder().id(102L).name("乙班").build());

        // 调用，并断言异常
        assertServiceException(() -> teamShiftService.generateTeamShiftRecords(planId),
                CAL_TEAM_SHIFT_GENERATE_REST_INVALID, "乙班",
                LocalDate.of(2026, 10, 19), "夜班", LocalDate.of(2026, 10, 20), "白班");
        // 断言：没有写入任何排班记录
        assertTrue(teamShiftMapper.selectListByPlanId(planId).isEmpty());
    }

    @Test
    public void testGenerateTeamShiftRecords_twoShiftOneMinuteRest() {
        // 准备参数
        Long planId = 3007L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 21);
        // mock 方法：两班倒、按天倒班 1 天，夜班 20:00~07:59，与次日 08:00 的白班之间留 1 分钟
        mockPlan(planId, MesCalShiftTypeEnum.TWO, MesCalShiftMethodEnum.DAY, 1, startDate, endDate);
        mockTwoShifts(planId, "08:00", "20:00", "20:00", "07:59");
        mockTeams(planId, 101L, 102L);

        // 调用
        teamShiftService.generateTeamShiftRecords(planId);

        // 断言：3 天 × 2 班次 = 6 条排班记录，每天换班
        List<MesCalTeamShiftDO> records = getRecords(planId);
        assertEquals(6, records.size());
        assertRecord(records.get(0), LocalDateTime.of(2026, 10, 19, 0, 0), 1, 101L, 11L);
        assertRecord(records.get(1), LocalDateTime.of(2026, 10, 19, 0, 0), 2, 102L, 12L);
        assertRecord(records.get(2), LocalDateTime.of(2026, 10, 20, 0, 0), 1, 102L, 11L);
        assertRecord(records.get(3), LocalDateTime.of(2026, 10, 20, 0, 0), 2, 101L, 12L);
        assertRecord(records.get(4), LocalDateTime.of(2026, 10, 21, 0, 0), 1, 101L, 11L);
        assertRecord(records.get(5), LocalDateTime.of(2026, 10, 21, 0, 0), 2, 102L, 12L);
    }

    @Test
    public void testGenerateTeamShiftRecords_twoShiftFullDay_singleDay() {
        // 准备参数
        Long planId = 3008L;
        LocalDate day = LocalDate.of(2026, 10, 19);
        // mock 方法：两班倒、默认班次覆盖全天，只排 1 天，不发生换班
        mockPlan(planId, MesCalShiftTypeEnum.TWO, MesCalShiftMethodEnum.DAY, 1, day, day);
        mockTwoShifts(planId, "08:00", "20:00", "20:00", "08:00");
        mockTeams(planId, 101L, 102L);

        // 调用
        teamShiftService.generateTeamShiftRecords(planId);

        // 断言：1 天 × 2 班次 = 2 条排班记录
        List<MesCalTeamShiftDO> records = getRecords(planId);
        assertEquals(2, records.size());
        assertRecord(records.get(0), day.atStartOfDay(), 1, 101L, 11L);
        assertRecord(records.get(1), day.atStartOfDay(), 2, 102L, 12L);
    }

    @Test
    public void testGenerateTeamShiftRecords_shiftTimeInvalid() {
        // 准备参数
        Long planId = 3009L;
        LocalDate day = LocalDate.of(2026, 10, 19);
        // mock 方法：白班结束时间不合法
        mockPlan(planId, MesCalShiftTypeEnum.TWO, MesCalShiftMethodEnum.DAY, 1, day, day);
        mockTwoShifts(planId, "08:00", "25:00", "20:00", "08:00");
        mockTeams(planId, 101L, 102L);

        // 调用，并断言异常
        assertServiceException(() -> teamShiftService.generateTeamShiftRecords(planId),
                CAL_TEAM_SHIFT_GENERATE_SHIFT_TIME_INVALID, "白班");
        // 断言：没有写入任何排班记录
        assertTrue(teamShiftMapper.selectListByPlanId(planId).isEmpty());
    }

    @Test
    public void testGenerateTeamShiftRecords_threeShift_hourWithoutZeroAndEnd24() {
        // 准备参数
        Long planId = 3016L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 20);
        // mock 方法：三班倒、按天倒班 1 天，时间写作 8:00、0:00，中班结束写作 24:00
        mockPlan(planId, MesCalShiftTypeEnum.THREE, MesCalShiftMethodEnum.DAY, 1, startDate, endDate);
        when(planShiftService.getPlanShiftListByPlanId(planId)).thenReturn(ListUtil.of(
                MesCalPlanShiftDO.builder().id(11L).planId(planId).sort(1).name("白班").startTime("8:00").endTime("16:00").build(),
                MesCalPlanShiftDO.builder().id(12L).planId(planId).sort(2).name("中班").startTime("16:00").endTime("24:00").build(),
                MesCalPlanShiftDO.builder().id(13L).planId(planId).sort(3).name("夜班").startTime("0:00").endTime("8:00").build()));
        mockTeams(planId, 101L, 102L, 103L);

        // 调用
        teamShiftService.generateTeamShiftRecords(planId);

        // 断言：与默认班次一致，2 天 × 3 班次 = 6 条排班记录，换班后休息 8 / 8 / 32 小时
        List<MesCalTeamShiftDO> records = getRecords(planId);
        assertEquals(6, records.size());
        assertRecord(records.get(0), startDate.atStartOfDay(), 1, 101L, 11L);
        assertRecord(records.get(1), startDate.atStartOfDay(), 2, 102L, 12L);
        assertRecord(records.get(2), startDate.atStartOfDay(), 3, 103L, 13L);
        assertRecord(records.get(3), endDate.atStartOfDay(), 1, 102L, 11L);
        assertRecord(records.get(4), endDate.atStartOfDay(), 2, 103L, 12L);
        assertRecord(records.get(5), endDate.atStartOfDay(), 3, 101L, 13L);
    }

    @Test
    public void testGenerateTeamShiftRecords_singleFullDayEnd24_withoutRest() {
        // 准备参数
        Long planId = 3017L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 20);
        // mock 方法：单班次 00:00~24:00，结束于次日 00:00，与次日的班次首尾相接
        mockPlan(planId, MesCalShiftTypeEnum.SINGLE, null, null, startDate, endDate);
        when(planShiftService.getPlanShiftListByPlanId(planId)).thenReturn(ListUtil.of(
                MesCalPlanShiftDO.builder().id(11L).planId(planId).sort(1).name("全天班")
                        .startTime("00:00").endTime("24:00").build()));
        mockTeams(planId, 101L);
        when(teamService.getTeam(eq(101L))).thenReturn(MesCalTeamDO.builder().id(101L).name("甲班").build());

        // 调用，并断言异常：24:00 不放宽连班校验
        assertServiceException(() -> teamShiftService.generateTeamShiftRecords(planId),
                CAL_TEAM_SHIFT_GENERATE_REST_INVALID, "甲班", startDate, "全天班", endDate, "全天班");
        // 断言：没有写入任何排班记录
        assertTrue(teamShiftMapper.selectListByPlanId(planId).isEmpty());
    }

    @Test
    public void testGenerateTeamShiftRecords_shiftTimeInvalid_boundary() {
        // 准备参数：开始时间不允许 24:00；结束时间只允许 24:00 整；分钟、小时越界；非 H:mm 格式
        String[][] cases = {
                {"24:00", "08:00"}, {"08:00", "24:30"}, {"08:60", "16:00"},
                {"08:00", "16:5"}, {"8点", "16:00"}, {"08:00:00", "16:00"}
        };
        for (int i = 0; i < cases.length; i++) {
            Long planId = 3020L + i;
            LocalDate day = LocalDate.of(2026, 10, 19);
            // mock 方法：单班次
            mockPlan(planId, MesCalShiftTypeEnum.SINGLE, null, null, day, day);
            when(planShiftService.getPlanShiftListByPlanId(planId)).thenReturn(ListUtil.of(
                    MesCalPlanShiftDO.builder().id(11L).planId(planId).sort(1).name("白班")
                            .startTime(cases[i][0]).endTime(cases[i][1]).build()));
            mockTeams(planId, 101L);

            // 调用，并断言异常
            assertServiceException(() -> teamShiftService.generateTeamShiftRecords(planId),
                    CAL_TEAM_SHIFT_GENERATE_SHIFT_TIME_INVALID, "白班");
            // 断言：没有写入任何排班记录
            assertTrue(teamShiftMapper.selectListByPlanId(planId).isEmpty());
        }
    }

    @Test
    public void testGenerateTeamShiftRecords_dayMethod_holidayNotConsumeShiftCount() {
        // 准备参数
        Long planId = 3010L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 22);
        // mock 方法：两班倒、按天倒班 2 天，班次之间留有空档
        mockPlan(planId, MesCalShiftTypeEnum.TWO, MesCalShiftMethodEnum.DAY, 2, startDate, endDate);
        mockTwoShifts(planId, "08:00", "17:00", "20:00", "05:00");
        mockTeams(planId, 101L, 102L);
        // mock 方法：10-20 为节假日；10-21 为工作日，照常排班
        mockHolidays(startDate, endDate, buildHoliday(LocalDate.of(2026, 10, 20), MesCalHolidayTypeEnum.HOLIDAY),
                buildHoliday(LocalDate.of(2026, 10, 21), MesCalHolidayTypeEnum.WORKDAY));

        // 调用
        teamShiftService.generateTeamShiftRecords(planId);

        // 断言：节假日停班，3 个有效排班日 × 2 班次 = 6 条排班记录
        List<MesCalTeamShiftDO> records = getRecords(planId);
        assertEquals(6, records.size());
        // 断言：10-19、10-21 为第 1、2 个有效排班日，A 组白班
        assertRecord(records.get(0), LocalDateTime.of(2026, 10, 19, 0, 0), 1, 101L, 11L);
        assertRecord(records.get(1), LocalDateTime.of(2026, 10, 19, 0, 0), 2, 102L, 12L);
        assertRecord(records.get(2), LocalDateTime.of(2026, 10, 21, 0, 0), 1, 101L, 11L);
        assertRecord(records.get(3), LocalDateTime.of(2026, 10, 21, 0, 0), 2, 102L, 12L);
        // 断言：10-22 为第 3 个有效排班日，才换班
        assertRecord(records.get(4), LocalDateTime.of(2026, 10, 22, 0, 0), 1, 102L, 11L);
        assertRecord(records.get(5), LocalDateTime.of(2026, 10, 22, 0, 0), 2, 101L, 12L);
    }

    @Test
    public void testGenerateTeamShiftRecords_dayMethod_firstDayHoliday() {
        // 准备参数
        Long planId = 3011L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 21);
        // mock 方法：两班倒、按天倒班 1 天，首日 10-19 为节假日
        mockPlan(planId, MesCalShiftTypeEnum.TWO, MesCalShiftMethodEnum.DAY, 1, startDate, endDate);
        mockTwoShifts(planId, "08:00", "17:00", "20:00", "05:00");
        mockTeams(planId, 101L, 102L);
        mockHolidays(startDate, endDate, buildHoliday(startDate, MesCalHolidayTypeEnum.HOLIDAY));

        // 调用
        teamShiftService.generateTeamShiftRecords(planId);

        // 断言：从 10-20 开始排班，10-20 为第 1 个有效排班日，不换班
        List<MesCalTeamShiftDO> records = getRecords(planId);
        assertEquals(4, records.size());
        assertRecord(records.get(0), LocalDateTime.of(2026, 10, 20, 0, 0), 1, 101L, 11L);
        assertRecord(records.get(1), LocalDateTime.of(2026, 10, 20, 0, 0), 2, 102L, 12L);
        assertRecord(records.get(2), LocalDateTime.of(2026, 10, 21, 0, 0), 1, 102L, 11L);
        assertRecord(records.get(3), LocalDateTime.of(2026, 10, 21, 0, 0), 2, 101L, 12L);
    }

    @Test
    public void testGenerateTeamShiftRecords_allHoliday() {
        // 准备参数
        Long planId = 3012L;
        LocalDate startDate = LocalDate.of(2026, 10, 19);
        LocalDate endDate = LocalDate.of(2026, 10, 20);
        // mock 方法：两班倒、计划范围内全部为节假日
        mockPlan(planId, MesCalShiftTypeEnum.TWO, MesCalShiftMethodEnum.DAY, 1, startDate, endDate);
        mockTwoShifts(planId, "08:00", "20:00", "20:00", "08:00");
        mockTeams(planId, 101L, 102L);
        mockHolidays(startDate, endDate, buildHoliday(startDate, MesCalHolidayTypeEnum.HOLIDAY),
                buildHoliday(endDate, MesCalHolidayTypeEnum.HOLIDAY));

        // 调用
        teamShiftService.generateTeamShiftRecords(planId);

        // 断言：不生成排班记录
        assertTrue(teamShiftMapper.selectListByPlanId(planId).isEmpty());
    }

    @Test
    public void testGenerateTeamShiftRecords_weekMethod_holidayOnWeekStart() {
        // 10-18 周日、10-19 周一为节假日、10-20 周二
        assertRotateAcrossHoliday(3013L, MesCalShiftMethodEnum.WEEK,
                LocalDate.of(2026, 10, 18), LocalDate.of(2026, 10, 19));
    }

    @Test
    public void testGenerateTeamShiftRecords_monthMethod_holidayOnMonthStart() {
        // 09-30、10-01 月初为节假日、10-02
        assertRotateAcrossHoliday(3014L, MesCalShiftMethodEnum.MONTH,
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 10, 1));
    }

    @Test
    public void testGenerateTeamShiftRecords_quarterMethod_holidayOnQuarterStart() {
        // 06-30、07-01 季度初为节假日、07-02
        assertRotateAcrossHoliday(3015L, MesCalShiftMethodEnum.QUARTER,
                LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 1));
    }

    /**
     * 断言按周/月/季度倒班时，周期起点恰逢节假日也照常换班
     *
     * 默认两班倒班次覆盖全天，换班前一天夜班的班组，隔着节假日停班一天再上白班，留有 24 小时休息
     */
    private void assertRotateAcrossHoliday(Long planId, MesCalShiftMethodEnum shiftMethod,
                                           LocalDate startDate, LocalDate holidayDate) {
        // 准备参数
        LocalDate endDate = holidayDate.plusDays(1);
        // mock 方法：两班倒、默认班次覆盖全天，周期起点为节假日
        mockPlan(planId, MesCalShiftTypeEnum.TWO, shiftMethod, null, startDate, endDate);
        mockTwoShifts(planId, "08:00", "20:00", "20:00", "08:00");
        mockTeams(planId, 101L, 102L);
        mockHolidays(startDate, endDate, buildHoliday(holidayDate, MesCalHolidayTypeEnum.HOLIDAY));

        // 调用
        teamShiftService.generateTeamShiftRecords(planId);

        // 断言：节假日停班，2 天 × 2 班次 = 4 条排班记录
        List<MesCalTeamShiftDO> records = getRecords(planId);
        assertEquals(4, records.size());
        // 断言：周期起点前，A 组白班、B 组夜班
        assertRecord(records.get(0), startDate.atStartOfDay(), 1, 101L, 11L);
        assertRecord(records.get(1), startDate.atStartOfDay(), 2, 102L, 12L);
        // 断言：周期起点虽为节假日，仍然换班：B 组白班、A 组夜班
        assertRecord(records.get(2), endDate.atStartOfDay(), 1, 102L, 11L);
        assertRecord(records.get(3), endDate.atStartOfDay(), 2, 101L, 12L);
    }

    private void mockPlan(Long planId, MesCalShiftTypeEnum shiftType, MesCalShiftMethodEnum shiftMethod,
                          Integer shiftCount, LocalDate startDate, LocalDate endDate) {
        when(planService.getPlan(planId)).thenReturn(MesCalPlanDO.builder()
                .id(planId).code("PLAN-" + planId).name("排班计划" + planId)
                .calendarType(1).startDate(startDate.atStartOfDay()).endDate(endDate.atStartOfDay())
                .shiftType(shiftType.getType())
                .shiftMethod(shiftMethod != null ? shiftMethod.getMethod() : null)
                .shiftCount(shiftCount).build());
    }

    private void mockTwoShifts(Long planId, String dayStartTime, String dayEndTime,
                               String nightStartTime, String nightEndTime) {
        when(planShiftService.getPlanShiftListByPlanId(planId)).thenReturn(ListUtil.of(
                MesCalPlanShiftDO.builder().id(11L).planId(planId).sort(1).name("白班")
                        .startTime(dayStartTime).endTime(dayEndTime).build(),
                MesCalPlanShiftDO.builder().id(12L).planId(planId).sort(2).name("夜班")
                        .startTime(nightStartTime).endTime(nightEndTime).build()));
    }

    private void mockTeams(Long planId, Long... teamIds) {
        List<MesCalPlanTeamDO> teams = new ArrayList<>();
        for (int i = 0; i < teamIds.length; i++) {
            teams.add(MesCalPlanTeamDO.builder().id(21L + i).planId(planId).teamId(teamIds[i]).build());
        }
        when(planTeamService.getPlanTeamListByPlanId(planId)).thenReturn(teams);
    }

    private void mockHolidays(LocalDate startDate, LocalDate endDate, MesCalHolidayDO... holidays) {
        when(holidayService.getHolidayList(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)))
                .thenReturn(ListUtil.of(holidays));
    }

    private static MesCalHolidayDO buildHoliday(LocalDate day, MesCalHolidayTypeEnum type) {
        return MesCalHolidayDO.builder().day(day.atStartOfDay()).type(type.getType()).build();
    }

    private List<MesCalTeamShiftDO> getRecords(Long planId) {
        List<MesCalTeamShiftDO> records = teamShiftMapper.selectListByPlanId(planId);
        records.sort(Comparator.comparing(MesCalTeamShiftDO::getDay).thenComparing(MesCalTeamShiftDO::getSort));
        return records;
    }

    private static void assertRecord(MesCalTeamShiftDO actual, LocalDateTime day, Integer sort,
                                     Long teamId, Long shiftId) {
        assertEquals(day, actual.getDay());
        assertEquals(sort, actual.getSort());
        assertEquals(teamId, actual.getTeamId());
        assertEquals(shiftId, actual.getShiftId());
    }

}
