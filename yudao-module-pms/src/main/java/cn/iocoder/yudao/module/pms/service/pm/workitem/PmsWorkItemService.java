package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemBoardReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemBoardRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemImportExcelVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemImportRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemIterationUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemNameUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemPlanningSortReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.workitem.PmsWorkItemSortReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * PMS 工作项 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsWorkItemService {

    /**
     * 创建工作项
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     * @return 工作项编号
     */
    Long createWorkItem(PmsWorkItemSaveReqVO saveReqVO, Long userId);

    /**
     * 更新工作项
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     */
    void updateWorkItem(PmsWorkItemSaveReqVO saveReqVO, Long userId);

    /**
     * 更新工作项名称
     *
     * @param updateReqVO 名称更新信息
     * @param userId 用户编号
     */
    void updateWorkItemName(PmsWorkItemNameUpdateReqVO updateReqVO, Long userId);

    /**
     * 更新工作项状态
     *
     * @param updateReqVO 状态更新信息
     * @param userId 用户编号
     */
    void updateWorkItemStatus(PmsWorkItemStatusUpdateReqVO updateReqVO, Long userId);

    /**
     * 更新指定状态下全部工作项的语义状态
     *
     * @param statusId 状态编号
     * @param statusType 语义状态
     */
    void updateWorkItemStatusTypeByStatusId(Long statusId, Integer statusType);

    /**
     * 获得指定状态下的工作项数量
     *
     * @param statusId 状态编号
     * @return 工作项数量
     */
    Long getWorkItemCountByStatusId(Long statusId);

    /**
     * 将指定状态下的工作项迁移到目标状态
     *
     * @param sourceStatusId 原状态编号
     * @param targetStatusId 目标状态编号
     * @param targetStatus 目标语义状态
     */
    void transferWorkItemStatus(Long sourceStatusId, Long targetStatusId, Integer targetStatus);

    /**
     * 规划工作项到迭代，或移回待规划
     *
     * @param updateReqVO 所属迭代更新信息
     * @param userId 用户编号
     */
    void updateWorkItemIteration(PmsWorkItemIterationUpdateReqVO updateReqVO, Long userId);

    /**
     * 更新看板列内工作项顺序
     *
     * @param sortReqVO 排序信息
     * @param userId 用户编号
     */
    void updateWorkItemSort(PmsWorkItemSortReqVO sortReqVO, Long userId);

    /**
     * 更新待规划或迭代内工作项顺序
     *
     * @param sortReqVO 排序信息
     * @param userId 用户编号
     */
    void updateWorkItemPlanningSort(PmsWorkItemPlanningSortReqVO sortReqVO, Long userId);

    /**
     * 归档工作项
     *
     * @param id 工作项编号
     * @param userId 用户编号
     */
    void archiveWorkItem(Long id, Long userId);

    /**
     * 将工作项移入回收站
     *
     * @param id 工作项编号
     * @param userId 用户编号
     */
    void recycleWorkItem(Long id, Long userId);

    /**
     * 恢复工作项
     *
     * @param id 工作项编号
     * @param userId 用户编号
     */
    void restoreWorkItem(Long id, Long userId);

    /**
     * 彻底删除回收站中的工作项
     *
     * @param id 工作项编号
     * @param userId 用户编号
     */
    void deleteWorkItem(Long id, Long userId);

    /**
     * 删除项目的全部工作项、参与人及状态
     *
     * @param projectId 项目编号
     */
    void deleteWorkItemListByProjectId(Long projectId);

    /**
     * 清空工作项的所属迭代
     *
     * @param iterationId 迭代编号
     */
    void clearWorkItemIterationId(Long iterationId);

    /**
     * 获得工作项
     *
     * @param id 工作项编号
     * @param userId 用户编号
     * @return 工作项
     */
    PmsWorkItemDO getWorkItem(Long id, Long userId);

    /**
     * 获得可编辑的正常工作项
     *
     * @param id 工作项编号
     * @param userId 用户编号
     * @return 工作项
     */
    PmsWorkItemDO getWritableWorkItem(Long id, Long userId);

    /**
     * 获得工作项分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 工作项分页
     */
    PageResult<PmsWorkItemDO> getWorkItemPage(PmsWorkItemPageReqVO pageReqVO, Long userId);

    /**
     * 获得项目工作项语义状态数量 Map
     *
     * @param projectIds 项目编号集合
     * @return 项目编号到状态数量 Map
     */
    Map<Long, Map<Integer, Long>> getProjectWorkItemStatusCountMap(Collection<Long> projectIds);

    /**
     * 获得工作项列表
     *
     * @param pageReqVO 查询条件
     * @param userId 用户编号
     * @return 工作项列表
     */
    List<PmsWorkItemDO> getWorkItemList(PmsWorkItemPageReqVO pageReqVO, Long userId);

    /**
     * 获得项目的正常工作项列表
     *
     * @param projectId 项目编号
     * @return 工作项列表
     */
    List<PmsWorkItemDO> getActiveWorkItemListByProjectId(Long projectId);

    /**
     * 获得迭代的正常工作项列表
     *
     * @param iterationId 迭代编号
     * @return 工作项列表
     */
    List<PmsWorkItemDO> getActiveWorkItemListByIterationId(Long iterationId);

    /**
     * 获得迭代工作项语义状态数量 Map
     *
     * @param iterationIds 迭代编号集合
     * @return 迭代编号到状态数量 Map
     */
    Map<Long, Map<Integer, Long>> getIterationWorkItemStatusCountMap(Collection<Long> iterationIds);

    /**
     * 获得分配给用户的未完成工作项分页
     *
     * @param pageReqVO 工作台分页条件
     * @param projectIds 可访问项目编号集合
     * @param userId 用户编号
     * @return 工作项分页
     */
    PageResult<PmsWorkItemDO> getAssignedWorkItemPage(PmsWorkbenchPageReqVO pageReqVO,
                                                       Collection<Long> projectIds, Long userId);

    /**
     * 获得分配给用户的未完成工作项类型数量 Map
     *
     * @param pageReqVO 工作台查询条件
     * @param projectIds 可访问项目编号集合
     * @param userId 用户编号
     * @return 工作项类型与数量 Map
     */
    Map<Integer, Long> getAssignedWorkItemTypeCountMap(PmsWorkbenchPageReqVO pageReqVO,
                                                       Collection<Long> projectIds, Long userId);

    /**
     * 导入工作项
     *
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @param importRows 导入数据
     * @param userId 用户编号
     * @return 导入结果
     */
    PmsWorkItemImportRespVO importWorkItemList(Long projectId, Integer workItemType,
                                               List<PmsWorkItemImportExcelVO> importRows, Long userId);

    /**
     * 获得工作项看板
     *
     * @param queryReqVO 看板查询
     * @param userId 用户编号
     * @return 看板列列表
     */
    List<PmsWorkItemBoardRespVO> getWorkItemBoard(PmsWorkItemBoardReqVO queryReqVO, Long userId);

    /**
     * 获得工作项参与人用户编号 Map
     *
     * @param workItemIds 工作项编号集合
     * @return 工作项参与人用户编号 Map
     */
    Map<Long, List<Long>> getWorkItemMemberUserIdListMap(Collection<Long> workItemIds);

    /**
     * 获得指定编号的工作项列表
     *
     * @param ids 工作项编号集合
     * @return 工作项列表
     */
    List<PmsWorkItemDO> getWorkItemList(Collection<Long> ids);

    /**
     * 获得工作项 Map
     *
     * @param ids 工作项编号集合
     * @return 工作项 Map
     */
    default Map<Long, PmsWorkItemDO> getWorkItemMap(Collection<Long> ids) {
        return convertMap(getWorkItemList(ids), PmsWorkItemDO::getId);
    }

}
