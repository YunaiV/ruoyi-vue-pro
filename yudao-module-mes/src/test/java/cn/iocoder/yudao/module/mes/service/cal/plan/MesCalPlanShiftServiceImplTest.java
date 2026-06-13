package cn.iocoder.yudao.module.mes.service.cal.plan;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.plan.MesCalPlanShiftDO;
import cn.iocoder.yudao.module.mes.dal.mysql.cal.plan.MesCalPlanShiftMapper;
import cn.iocoder.yudao.module.mes.enums.cal.MesCalShiftTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import jakarta.annotation.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MesCalPlanShiftServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesCalPlanShiftServiceImpl.class)
public class MesCalPlanShiftServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesCalPlanShiftServiceImpl planShiftService;

    @Resource
    private MesCalPlanShiftMapper planShiftMapper;

    @MockitoBean
    private MesCalPlanService planService;

    @Test
    public void testAddDefaultPlanShift_singleShift_use18Clock() {
        // 准备参数
        Long planId = 1001L;

        // 调用
        planShiftService.addDefaultPlanShift(planId, MesCalShiftTypeEnum.SINGLE.getType());

        // 断言：生成 1 条默认班次（白班 08:00~18:00）
        List<MesCalPlanShiftDO> shifts = planShiftMapper.selectListByPlanId(planId);
        assertEquals(1, shifts.size());
        MesCalPlanShiftDO shift = shifts.get(0);
        assertEquals(planId, shift.getPlanId());
        assertEquals(1, shift.getSort());
        assertEquals("白班", shift.getName());
        assertEquals("08:00", shift.getStartTime());
        assertEquals("18:00", shift.getEndTime());
    }

}
