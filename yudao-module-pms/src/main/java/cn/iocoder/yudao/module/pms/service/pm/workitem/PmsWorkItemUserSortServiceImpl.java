package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemUserSortDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemUserSortMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemUserSort(Long projectId, List<Long> workItemIds, Long userId) {
        // 1. 删除原有个人排序
        workItemUserSortMapper.deleteByProjectIdAndUserId(projectId, userId);

        // 2. 按请求顺序保存个人排序
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
