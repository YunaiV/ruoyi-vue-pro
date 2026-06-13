package cn.iocoder.yudao.module.mes.service.cal.plan;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.MesCalPlanSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.plan.MesCalPlanMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalPlanStatusEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftMethodEnum;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamShiftService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
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

    @MockitoBean
    private MesCalPlanShiftService planShiftService;
    @MockitoBean
    private MesCalPlanTeamService planTeamService;
    @MockitoBean
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

}
