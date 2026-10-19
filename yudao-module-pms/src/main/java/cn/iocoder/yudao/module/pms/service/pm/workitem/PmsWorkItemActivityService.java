package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemActivityDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum;

import java.util.Collection;
import java.util.List;

/**
 * PMS 工作项动态 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsWorkItemActivityService {

    /**
     * 按字段记录工作项更新动态
     *
     * @param oldWorkItem 更新前工作项
     * @param newWorkItem 更新后工作项
     * @param oldMemberUserIds 更新前参与人用户编号
     * @param newMemberUserIds 更新后参与人用户编号
     * @param userId 操作人编号
     */
    void createWorkItemUpdateActivities(PmsWorkItemDO oldWorkItem, PmsWorkItemDO newWorkItem,
                                        Collection<Long> oldMemberUserIds, Collection<Long> newMemberUserIds,
                                        Long userId);

    /**
     * 字段发生变化时记录工作项动态
     *
     * @param workItem 工作项
     * @param userId 操作人编号
     * @param fieldName 字段名称
     * @param oldFieldValue 旧字段值
     * @param newFieldValue 新字段值
     * @param oldValue 旧展示值
     * @param newValue 新展示值
     */
    void createWorkItemFieldActivityIfChanged(PmsWorkItemDO workItem, Long userId, String fieldName,
                                               Object oldFieldValue, Object newFieldValue,
                                               String oldValue, String newValue);

    /**
     * 按迭代编号变化记录工作项动态
     *
     * @param workItem 工作项
     * @param userId 操作人编号
     * @param oldIterationId 更新前迭代编号
     * @param newIterationId 更新后迭代编号
     */
    void createWorkItemIterationActivityIfChanged(PmsWorkItemDO workItem, Long userId,
                                                   Long oldIterationId, Long newIterationId);

    /**
     * 记录一条工作项动态
     *
     * @param projectId 项目编号
     * @param workItemId 工作项编号
     * @param operatorUserId 操作用户编号
     * @param content 动态内容枚举
     * @param arguments 动态内容参数
     */
    void createWorkItemActivity(Long projectId, Long workItemId, Long operatorUserId,
                                PmsWorkItemActivityContentEnum content, Object... arguments);

    /**
     * 获得工作项的动态列表
     *
     * @param workItemId 工作项编号
     * @return 动态列表
     */
    List<PmsWorkItemActivityDO> getWorkItemActivityList(Long workItemId);

    /**
     * 获得多个工作项的最近动态列表
     *
     * @param workItemIds 工作项编号集合
     * @param limit 最大数量
     * @return 工作项动态列表
     */
    List<PmsWorkItemActivityDO> getWorkItemActivityListByWorkItemIds(Collection<Long> workItemIds, int limit);

    /**
     * 删除工作项的全部动态
     *
     * @param workItemId 工作项编号
     */
    void deleteWorkItemActivityListByWorkItemId(Long workItemId);

    /**
     * 删除项目的全部动态
     *
     * @param projectId 项目编号
     */
    void deleteWorkItemActivityListByProjectId(Long projectId);

}
