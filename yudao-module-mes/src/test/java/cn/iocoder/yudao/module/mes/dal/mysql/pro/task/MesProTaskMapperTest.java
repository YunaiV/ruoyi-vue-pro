package cn.iocoder.yudao.module.mes.dal.mysql.pro.task;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.route.MesProRouteProcessMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MesProTaskMapper#selectPage(MesProTaskPageReqVO)} 的单元测试
 *
 * @author 芋道源码
 */
public class MesProTaskMapperTest extends BaseDbUnitTest {

    @Resource
    private MesProTaskMapper taskMapper;

    @Resource
    private MesProRouteProcessMapper routeProcessMapper;

    /**
     * 创建 Task 时统一设置 BigDecimal 字段为 2 位小数，避免 H2 decimal(14,2) 精度不匹配
     */
    private MesProTaskDO createTaskPojo(Consumer<MesProTaskDO> consumer) {
        return randomPojo(MesProTaskDO.class, o -> {
            o.setQuantity(new BigDecimal("10.00"));
            o.setProducedQuantity(new BigDecimal("5.00"));
            o.setQualifyQuantity(new BigDecimal("4.00"));
            o.setUnqualifyQuantity(new BigDecimal("1.00"));
            o.setChangedQuantity(new BigDecimal("0.00"));
            consumer.accept(o);
        });
    }

    // ==================== selectPage 基础过滤 ====================

    @Test
    public void testSelectPage_byCode() {
        MesProTaskDO matchTask = createTaskPojo(o -> {
            o.setCode("PT202501010001");
            o.setStatus(0);
        });
        taskMapper.insert(matchTask);
        taskMapper.insert(cloneIgnoreId(matchTask, o -> o.setCode("OTHER_CODE")));

        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        reqVO.setCode("PT2025");
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(1, result.getTotal());
        assertPojoEquals(matchTask, result.getList().get(0));
    }

    @Test
    public void testSelectPage_byName() {
        MesProTaskDO matchTask = createTaskPojo(o -> {
            o.setName("注塑任务-A");
            o.setStatus(0);
        });
        taskMapper.insert(matchTask);
        taskMapper.insert(cloneIgnoreId(matchTask, o -> o.setName("OTHER_NAME")));

        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        reqVO.setName("注塑");
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(1, result.getTotal());
        assertPojoEquals(matchTask, result.getList().get(0));
    }

    @Test
    public void testSelectPage_byWorkOrderId() {
        MesProTaskDO matchTask = createTaskPojo(o -> {
            o.setWorkOrderId(100L);
            o.setStatus(0);
        });
        taskMapper.insert(matchTask);
        taskMapper.insert(cloneIgnoreId(matchTask, o -> o.setWorkOrderId(999L)));

        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        reqVO.setWorkOrderId(100L);
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(1, result.getTotal());
        assertPojoEquals(matchTask, result.getList().get(0));
    }

    @Test
    public void testSelectPage_byStatus() {
        MesProTaskDO matchTask = createTaskPojo(o -> o.setStatus(1));
        taskMapper.insert(matchTask);
        taskMapper.insert(cloneIgnoreId(matchTask, o -> o.setStatus(2)));

        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        reqVO.setStatus(1);
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(1, result.getTotal());
        assertPojoEquals(matchTask, result.getList().get(0));
    }

    @Test
    public void testSelectPage_byStatuses() {
        MesProTaskDO task1 = createTaskPojo(o -> o.setStatus(1));
        MesProTaskDO task2 = createTaskPojo(o -> o.setStatus(2));
        MesProTaskDO task3 = createTaskPojo(o -> o.setStatus(3));
        taskMapper.insert(task1);
        taskMapper.insert(task2);
        taskMapper.insert(task3);

        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        reqVO.setStatuses(ListUtil.of(1, 2));
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(2, result.getTotal());
    }

    @Test
    public void testSelectPage_noFilter() {
        MesProTaskDO task1 = createTaskPojo(o -> o.setStatus(0));
        MesProTaskDO task2 = createTaskPojo(o -> o.setStatus(1));
        taskMapper.insert(task1);
        taskMapper.insert(task2);

        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(2, result.getTotal());
    }

    // ==================== selectPage + checkFlag (MPJ LEFT JOIN) ====================

    @Test
    public void testSelectPage_checkFlagTrue() {
        Long routeId = 10L;
        Long processId1 = 100L;
        Long processId2 = 200L;

        MesProTaskDO task1 = createTaskPojo(o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId1);
            o.setStatus(0);
        });
        MesProTaskDO task2 = createTaskPojo(o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId2);
            o.setStatus(0);
        });
        taskMapper.insert(task1);
        taskMapper.insert(task2);

        routeProcessMapper.insert(randomPojo(MesProRouteProcessDO.class, o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId1);
            o.setCheckFlag(true);
        }));
        routeProcessMapper.insert(randomPojo(MesProRouteProcessDO.class, o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId2);
            o.setCheckFlag(false);
        }));

        // 查询：checkFlag = true，只返回 task1
        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        reqVO.setCheckFlag(true);
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(1, result.getTotal());
        assertPojoEquals(task1, result.getList().get(0));
    }

    @Test
    public void testSelectPage_checkFlagFalse() {
        Long routeId = 20L;
        Long processId1 = 300L;
        Long processId2 = 400L;

        MesProTaskDO task1 = createTaskPojo(o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId1);
            o.setStatus(0);
        });
        MesProTaskDO task2 = createTaskPojo(o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId2);
            o.setStatus(0);
        });
        taskMapper.insert(task1);
        taskMapper.insert(task2);

        routeProcessMapper.insert(randomPojo(MesProRouteProcessDO.class, o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId1);
            o.setCheckFlag(true);
        }));
        routeProcessMapper.insert(randomPojo(MesProRouteProcessDO.class, o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId2);
            o.setCheckFlag(false);
        }));

        // 查询：checkFlag = false，只返回 task2
        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        reqVO.setCheckFlag(false);
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(1, result.getTotal());
        assertPojoEquals(task2, result.getList().get(0));
    }

    @Test
    public void testSelectPage_checkFlagWithOtherFilter() {
        Long routeId = 30L;
        Long processId = 500L;

        MesProTaskDO taskMatch = createTaskPojo(o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId);
            o.setCode("PT-MATCH-001");
            o.setStatus(0);
        });
        MesProTaskDO taskNoMatch = createTaskPojo(o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId);
            o.setCode("OTHER-CODE");
            o.setStatus(0);
        });
        taskMapper.insert(taskMatch);
        taskMapper.insert(taskNoMatch);

        routeProcessMapper.insert(randomPojo(MesProRouteProcessDO.class, o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId);
            o.setCheckFlag(true);
        }));

        // 查询：checkFlag=true 且 code like 'PT-MATCH'
        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        reqVO.setCheckFlag(true);
        reqVO.setCode("PT-MATCH");
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(1, result.getTotal());
        assertPojoEquals(taskMatch, result.getList().get(0));
    }

    @Test
    public void testSelectPage_checkFlagNull_noJoin() {
        Long routeId = 40L;
        Long processId = 600L;

        MesProTaskDO task = createTaskPojo(o -> {
            o.setRouteId(routeId);
            o.setProcessId(processId);
            o.setStatus(0);
        });
        taskMapper.insert(task);

        MesProTaskPageReqVO reqVO = new MesProTaskPageReqVO();
        PageResult<MesProTaskDO> result = taskMapper.selectPage(reqVO);

        assertEquals(1, result.getTotal());
        assertPojoEquals(task, result.getList().get(0));
    }

}
