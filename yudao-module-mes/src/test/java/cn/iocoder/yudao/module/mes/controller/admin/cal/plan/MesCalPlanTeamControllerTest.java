package cn.iocoder.yudao.module.mes.controller.admin.cal.plan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.cal.plan.vo.team.MesCalPlanTeamRespVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanTeamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import cn.iocoder.yudao.module.mes.service.cal.plan.MesCalPlanTeamService;
import cn.iocoder.yudao.module.mes.service.cal.team.MesCalTeamService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link MesCalPlanTeamController} 的单元测试类
 *
 * @author 芋道源码
 */
public class MesCalPlanTeamControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MesCalPlanTeamController planTeamController;

    @Mock
    private MesCalPlanTeamService planTeamService;
    @Mock
    private MesCalTeamService teamService;

    @Test
    public void testGetPlanTeamListByPlan_empty() {
        // mock 方法
        when(planTeamService.getPlanTeamListByPlanId(1L)).thenReturn(Collections.emptyList());

        // 调用
        CommonResult<List<MesCalPlanTeamRespVO>> result = planTeamController.getPlanTeamListByPlan(1L);
        // 断言：空列表直接返回，不查询班组
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
        verifyNoInteractions(teamService);
    }

    @Test
    public void testGetPlanTeamListByPlan_fillTeam() {
        // mock 方法：班组 20 不存在
        when(planTeamService.getPlanTeamListByPlanId(1L)).thenReturn(List.of(
                MesCalPlanTeamDO.builder().id(3L).planId(1L).teamId(20L).remark("缺失").build(),
                MesCalPlanTeamDO.builder().id(2L).planId(1L).teamId(10L).remark("存在").build()));
        when(teamService.getTeamMap(List.of(20L, 10L))).thenReturn(Map.of(
                10L, MesCalTeamDO.builder().id(10L).code("T001").name("A组").build()));

        // 调用
        CommonResult<List<MesCalPlanTeamRespVO>> result = planTeamController.getPlanTeamListByPlan(1L);
        // 断言：保持原列表顺序；存在的班组回填编码、名称，缺失的班组保持为空
        assertTrue(result.isSuccess());
        List<MesCalPlanTeamRespVO> list = result.getData();
        assertEquals(2, list.size());
        MesCalPlanTeamRespVO missing = list.get(0);
        assertEquals(3L, missing.getId());
        assertEquals(20L, missing.getTeamId());
        assertEquals("缺失", missing.getRemark());
        assertNull(missing.getTeamCode());
        assertNull(missing.getTeamName());
        MesCalPlanTeamRespVO exists = list.get(1);
        assertEquals(2L, exists.getId());
        assertEquals(10L, exists.getTeamId());
        assertEquals("T001", exists.getTeamCode());
        assertEquals("A组", exists.getTeamName());
        verify(teamService).getTeamMap(List.of(20L, 10L));
    }

}
