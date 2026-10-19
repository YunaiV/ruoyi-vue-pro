package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemBoardConfigSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusConfigUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusDeleteReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusSortReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemBoardDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemStatusDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * PMS 工作项看板状态 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsWorkItemStatusService {

    /**
     * 创建自定义看板状态，追加到项目工作项类型的状态末尾
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 状态编号
     */
    Long createWorkItemStatus(PmsWorkItemStatusCreateReqVO createReqVO, Long userId);

    /**
     * 更新看板状态配置，并同步其全部工作项的语义状态
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateWorkItemStatusConfig(PmsWorkItemStatusConfigUpdateReqVO updateReqVO, Long userId);

    /**
     * 切换当前工作项类型的初始状态
     *
     * @param id 状态编号
     * @param userId 用户编号
     */
    void updateDefaultWorkItemStatus(Long id, Long userId);

    /**
     * 更新项目工作项类型的状态顺序，要求排序列表完整覆盖全部状态
     *
     * @param sortReqVO 排序信息
     * @param userId 用户编号
     */
    void updateWorkItemStatusSort(PmsWorkItemStatusSortReqVO sortReqVO, Long userId);

    /**
     * 保存工作项看板列及其状态映射
     *
     * @param saveReqVO 看板配置
     * @param userId 用户编号
     */
    void updateWorkItemBoardConfig(PmsWorkItemBoardConfigSaveReqVO saveReqVO, Long userId);

    /**
     * 删除看板状态，存在工作项时先迁移到目标状态，初始状态不允许删除
     *
     * @param deleteReqVO 删除信息
     * @param userId 用户编号
     */
    void deleteWorkItemStatus(PmsWorkItemStatusDeleteReqVO deleteReqVO, Long userId);

    /**
     * 初始化项目支持的工作项状态
     *
     * @param projectId 项目编号
     * @param projectType 项目类型
     */
    void initProjectWorkItemStatuses(Long projectId, Integer projectType);

    /**
     * 获得工作项状态，不存在时抛出异常
     *
     * @param id 状态编号
     * @return 工作项状态
     */
    PmsWorkItemStatusDO getWorkItemStatus(Long id);

    /**
     * 获得用户可读的工作项状态
     *
     * @param id 状态编号
     * @param userId 用户编号
     * @return 工作项状态
     */
    PmsWorkItemStatusDO getWorkItemStatus(Long id, Long userId);

    /**
     * 获得项目指定工作项类型的已保存状态列表
     *
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @return 已存在的状态列表
     */
    List<PmsWorkItemStatusDO> getWorkItemStatusList(Long projectId, Integer workItemType);

    /**
     * 获得项目指定工作项类型的已保存看板列
     *
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @return 看板列列表
     */
    List<PmsWorkItemBoardDO> getWorkItemBoardList(Long projectId, Integer workItemType);

    /**
     * 获得工作项的初始状态
     *
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @return 初始状态
     */
    PmsWorkItemStatusDO getDefaultWorkItemStatus(Long projectId, Integer workItemType);

    /**
     * 获得项目指定工作项类型的看板状态列表
     *
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @param userId 用户编号
     * @return 状态列表
     */
    List<PmsWorkItemStatusDO> getWorkItemStatusList(Long projectId, Integer workItemType, Long userId);

    /**
     * 校验并获得看板状态
     *
     * @param id 状态编号
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @return 看板状态
     */
    PmsWorkItemStatusDO validateWorkItemStatus(Long id, Long projectId, Integer workItemType);

    /**
     * 获得工作项状态 Map
     *
     * @param ids 状态编号集合
     * @return 工作项状态 Map
     */
    Map<Long, PmsWorkItemStatusDO> getWorkItemStatusMap(Collection<Long> ids);

    /**
     * 删除项目的全部工作项状态
     *
     * @param projectId 项目编号
     */
    void deleteWorkItemStatusListByProjectId(Long projectId);

}
