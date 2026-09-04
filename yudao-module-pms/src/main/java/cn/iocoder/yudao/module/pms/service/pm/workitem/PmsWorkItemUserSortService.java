package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;

import java.util.List;

/**
 * PMS 工作项个人排序 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsWorkItemUserSortService {

    /**
     * 更新用户的待规划工作项顺序
     *
     * @param projectId 项目编号
     * @param workItemIds 工作项编号列表
     * @param userId 用户编号
     */
    void updateWorkItemUserSort(Long projectId, List<Long> workItemIds, Long userId);

    /**
     * 按用户保存的顺序排列待规划工作项
     *
     * @param workItems 工作项列表
     * @param projectId 项目编号
     * @param userId 用户编号
     */
    void sortWorkItemList(List<PmsWorkItemDO> workItems, Long projectId, Long userId);

    /**
     * 删除工作项的个人排序
     *
     * @param workItemId 工作项编号
     */
    void deleteWorkItemUserSortByWorkItemId(Long workItemId);

    /**
     * 删除项目的个人排序
     *
     * @param projectId 项目编号
     */
    void deleteWorkItemUserSortByProjectId(Long projectId);

}
