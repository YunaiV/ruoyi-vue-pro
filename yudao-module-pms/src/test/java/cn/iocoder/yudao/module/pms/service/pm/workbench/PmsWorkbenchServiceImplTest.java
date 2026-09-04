package cn.iocoder.yudao.module.pms.service.pm.workbench;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchCountRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemService;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * {@link PmsWorkbenchServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsWorkbenchServiceImpl.class)
public class PmsWorkbenchServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkbenchServiceImpl workbenchService;
    @MockBean
    private PmsProjectMemberService projectMemberService;
    @MockBean
    private PmsWorkItemService workItemService;
    @MockBean
    private PmsIterationService iterationService;

    @Test
    public void testGetWorkItemPage_filtersOwnerAndCompleted() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = mockActiveProject(userId);
        Long iterationId = randomLongId();
        LocalDateTime[] endTime = new LocalDateTime[]{LocalDateTime.now().minusDays(1), LocalDateTime.now()};
        PmsWorkbenchPageReqVO pageReqVO = new PmsWorkbenchPageReqVO().setProjectId(projectId)
                .setStatus(PmsWorkItemStatusTypeEnum.PROCESSING.getType()).setPriority(2)
                .setIterationId(iterationId).setEndTime(endTime);
        when(workItemService.getAssignedWorkItemPage(
                pageReqVO, Collections.singletonList(projectId), userId))
                .thenReturn(new PageResult<>(Collections.singletonList(
                        randomWorkItemDO(PmsWorkItemTypeEnum.TASK.getType(), PmsWorkItemStatusTypeEnum.PROCESSING.getType())),
                        1L));

        // 调用
        PageResult<PmsWorkItemDO> pageResult = workbenchService.getWorkbenchWorkItemPage(
                pageReqVO, userId);

        // 断言
        assertEquals(1, pageResult.getTotal());
    }

    @Test
    public void testGetCount_success() {
        // mock 数据
        Long userId = randomLongId();
        Long projectId = mockActiveProject(userId);
        PmsWorkbenchPageReqVO pageReqVO = new PmsWorkbenchPageReqVO().setProjectId(projectId)
                .setName("登录").setPriority(2);
        Map<Integer, Long> typeCountMap = new HashMap<>();
        typeCountMap.put(PmsWorkItemTypeEnum.REQUIREMENT.getType(), 1L);
        typeCountMap.put(PmsWorkItemTypeEnum.DEFECT.getType(), 1L);
        when(workItemService.getAssignedWorkItemTypeCountMap(pageReqVO,
                Collections.singletonList(projectId), userId))
                .thenReturn(typeCountMap);
        when(iterationService.getUncompletedIterationCount(pageReqVO,
                Collections.singletonList(projectId), userId)).thenReturn(1L);

        // 调用
        PmsWorkbenchCountRespVO count = workbenchService.getWorkbenchCount(pageReqVO, userId);

        // 断言
        assertEquals(1, count.getRequirementCount());
        assertEquals(1, count.getDefectCount());
        assertEquals(1, count.getIterationCount());
    }

    // ========== 随机对象 ==========

    private Long mockActiveProject(Long userId) {
        Long projectId = randomLongId();
        when(projectMemberService.getActiveProjectIdListByUserId(userId)).thenReturn(Collections.singletonList(projectId));
        return projectId;
    }

    private PmsWorkItemDO randomWorkItemDO(Integer type, Integer status) {
        return new PmsWorkItemDO().setId(randomLongId()).setType(type).setStatus(status);
    }

}
