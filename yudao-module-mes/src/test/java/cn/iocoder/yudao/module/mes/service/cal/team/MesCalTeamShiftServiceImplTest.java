package cn.iocoder.yudao.module.mes.service.cal.team;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamShiftDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.team.MesCalTeamShiftMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftMethodEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanShiftService;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanTeamService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

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

    @MockitoBean
    private MesCalPlanService planService;
    @MockitoBean
    private MesCalPlanShiftService planShiftService;
    @MockitoBean
    private MesCalPlanTeamService planTeamService;

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

        // 断言：3 天 × 3 班次 = 9 条排班记录，班组按天轮转
        List<MesCalTeamShiftDO> records = teamShiftMapper.selectListByPlanId(planId);
        records.sort(Comparator.comparing(MesCalTeamShiftDO::getDay).thenComparing(MesCalTeamShiftDO::getSort));
        assertEquals(9, records.size());
        // 断言：第 1 天（shiftIndex=0）：A 白班、B 中班、C 夜班
        assertRecord(records.get(0), LocalDateTime.of(2026, 4, 1, 0, 0), 1, 101L, 11L);
        assertRecord(records.get(1), LocalDateTime.of(2026, 4, 1, 0, 0), 2, 102L, 12L);
        assertRecord(records.get(2), LocalDateTime.of(2026, 4, 1, 0, 0), 3, 103L, 13L);
        // 断言：第 2 天（shiftIndex=1）：C 白班、A 中班、B 夜班
        assertRecord(records.get(3), LocalDateTime.of(2026, 4, 2, 0, 0), 1, 103L, 11L);
        assertRecord(records.get(4), LocalDateTime.of(2026, 4, 2, 0, 0), 2, 101L, 12L);
        assertRecord(records.get(5), LocalDateTime.of(2026, 4, 2, 0, 0), 3, 102L, 13L);
        // 断言：第 3 天（shiftIndex=2）：B 白班、C 中班、A 夜班
        assertRecord(records.get(6), LocalDateTime.of(2026, 4, 3, 0, 0), 1, 102L, 11L);
        assertRecord(records.get(7), LocalDateTime.of(2026, 4, 3, 0, 0), 2, 103L, 12L);
        assertRecord(records.get(8), LocalDateTime.of(2026, 4, 3, 0, 0), 3, 101L, 13L);
    }

    private static void assertRecord(MesCalTeamShiftDO actual, LocalDateTime day, Integer sort,
                                     Long teamId, Long shiftId) {
        assertEquals(day, actual.getDay());
        assertEquals(sort, actual.getSort());
        assertEquals(teamId, actual.getTeamId());
        assertEquals(shiftId, actual.getShiftId());
    }

}
