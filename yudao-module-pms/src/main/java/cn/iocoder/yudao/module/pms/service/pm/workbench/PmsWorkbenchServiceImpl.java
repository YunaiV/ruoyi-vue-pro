package cn.iocoder.yudao.module.pms.service.pm.workbench;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchCountRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * PMS 工作台 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsWorkbenchServiceImpl implements PmsWorkbenchService {

    @Resource
    private PmsWorkItemService workItemService;
    @Resource
    private PmsIterationService iterationService;

    @Resource
    private PmsProjectMemberService projectMemberService;

    @Override
    public PageResult<PmsWorkItemDO> getWorkbenchWorkItemPage(PmsWorkbenchPageReqVO pageReqVO, Long userId) {
        // 1. 获得当前用户参与的活跃项目
        List<Long> projectIds = projectMemberService.getActiveProjectIdListByUserId(userId);
        if (CollUtil.isEmpty(projectIds) || pageReqVO.getProjectId() != null
                && !projectIds.contains(pageReqVO.getProjectId())) {
            return PageResult.empty();
        }

        // 2. 查询当前用户被分配的未完成工作项分页
        return workItemService.getAssignedWorkItemPage(pageReqVO, projectIds, userId);
    }

    @Override
    public PageResult<PmsIterationDO> getWorkbenchIterationPage(PmsWorkbenchPageReqVO pageReqVO, Long userId) {
        // 1. 获得当前用户参与的活跃项目
        List<Long> projectIds = projectMemberService.getActiveProjectIdListByUserId(userId);
        if (CollUtil.isEmpty(projectIds) || pageReqVO.getProjectId() != null
                && !projectIds.contains(pageReqVO.getProjectId())) {
            return PageResult.empty();
        }

        // 2. 查询活跃项目的未完成迭代分页
        return iterationService.getUncompletedIterationPage(pageReqVO, projectIds, userId);
    }

    @Override
    public PmsWorkbenchCountRespVO getWorkbenchCount(PmsWorkbenchPageReqVO pageReqVO, Long userId) {
        // 1. 获得当前用户参与的活跃项目
        List<Long> projectIds = projectMemberService.getActiveProjectIdListByUserId(userId);
        if (CollUtil.isEmpty(projectIds) || pageReqVO.getProjectId() != null
                && !projectIds.contains(pageReqVO.getProjectId())) {
            return new PmsWorkbenchCountRespVO().setRequirementCount(0).setTaskCount(0)
                    .setDefectCount(0).setIterationCount(0);
        }

        // 2. 按工作项类型和迭代统计当前用户的待办数量
        Map<Integer, Long> typeCountMap = workItemService.getAssignedWorkItemTypeCountMap(pageReqVO, projectIds, userId);
        int iterationCount = iterationService.getUncompletedIterationCount(pageReqVO, projectIds, userId).intValue();
        return new PmsWorkbenchCountRespVO()
                .setRequirementCount(typeCountMap.getOrDefault(PmsWorkItemTypeEnum.REQUIREMENT.getType(), 0L).intValue())
                .setTaskCount(typeCountMap.getOrDefault(PmsWorkItemTypeEnum.TASK.getType(), 0L).intValue())
                .setDefectCount(typeCountMap.getOrDefault(PmsWorkItemTypeEnum.DEFECT.getType(), 0L).intValue())
                .setIterationCount(iterationCount);
    }

}
