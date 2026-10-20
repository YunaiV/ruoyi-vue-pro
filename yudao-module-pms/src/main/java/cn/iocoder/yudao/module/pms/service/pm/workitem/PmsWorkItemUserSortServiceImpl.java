package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemUserSortDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemUserSortMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_STATUS_INVALID;

/**
 * PMS 工作项个人排序 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsWorkItemUserSortServiceImpl implements PmsWorkItemUserSortService {

    @Resource
    private PmsWorkItemUserSortMapper workItemUserSortMapper;

    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsProjectService projectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemUserSort(Long projectId, List<Long> workItemIds, Long userId) {
        // 1.1 锁定项目，避免并发更新个人排序产生重复记录（https://t.zsxq.com/V48Hl）
        PmsProjectDO project = projectService.getProjectForUpdate(projectId);
        // 1.2 校验锁定后的项目状态，避免项目并发回收、删除后继续写入个人排序
        if (project == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
        if (!PmsProjectStatusEnum.ACTIVE.getStatus().equals(project.getStatus())) {
            throw exception(PROJECT_STATUS_INVALID);
        }

        // 2. 删除原有个人排序
        workItemUserSortMapper.deleteByProjectIdAndUserId(projectId, userId);

        // 3. 按请求顺序保存个人排序
        for (int index = 0; index < workItemIds.size(); index++) {
            workItemUserSortMapper.insert(new PmsWorkItemUserSortDO().setProjectId(projectId)
                    .setWorkItemId(workItemIds.get(index)).setUserId(userId).setSort(index + 1));
        }
    }

    @Override
    public void sortWorkItemList(List<PmsWorkItemDO> workItems, Long projectId, Long userId) {
        // 1. 查询用户保存的个人排序
        List<PmsWorkItemUserSortDO> userSorts =
                workItemUserSortMapper.selectListByProjectIdAndUserId(projectId, userId);
        if (CollUtil.isEmpty(userSorts)) {
            return;
        }

        // 2. 转换个人排序 Map，并排列工作项
        Map<Long, Integer> userSortMap = convertMap(userSorts,
                PmsWorkItemUserSortDO::getWorkItemId, PmsWorkItemUserSortDO::getSort);
        workItems.sort(Comparator
                .comparing((PmsWorkItemDO item) -> userSortMap.containsKey(item.getId()) ? 0 : 1)
                .thenComparing(item -> userSortMap.getOrDefault(item.getId(), 0))
                .thenComparing(PmsWorkItemDO::getId, Comparator.reverseOrder()));
    }

    @Override
    public void deleteWorkItemUserSortByWorkItemId(Long workItemId) {
        workItemUserSortMapper.deleteByWorkItemId(workItemId);
    }

    @Override
    public void deleteWorkItemUserSortByProjectId(Long projectId) {
        workItemUserSortMapper.deleteByProjectId(projectId);
    }

}
