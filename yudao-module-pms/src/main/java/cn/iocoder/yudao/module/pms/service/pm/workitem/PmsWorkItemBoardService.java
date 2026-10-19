package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemBoardDO;

import java.util.Collection;
import java.util.List;

/**
 * PMS 工作项看板列 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsWorkItemBoardService {

    /**
     * 获得项目指定工作项类型的看板列
     *
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @return 看板列列表
     */
    List<PmsWorkItemBoardDO> getWorkItemBoardList(Long projectId, Integer workItemType);

    /**
     * 创建看板列
     *
     * @param board 看板列
     */
    void createWorkItemBoard(PmsWorkItemBoardDO board);

    /**
     * 批量创建看板列
     *
     * @param boards 看板列列表
     */
    void createWorkItemBoardList(Collection<PmsWorkItemBoardDO> boards);

    /**
     * 批量更新看板列
     *
     * @param boards 看板列列表
     */
    void updateWorkItemBoardList(Collection<PmsWorkItemBoardDO> boards);

    /**
     * 删除指定看板列
     *
     * @param ids 看板列编号集合
     */
    void deleteWorkItemBoardList(Collection<Long> ids);

    /**
     * 删除项目的全部看板列
     *
     * @param projectId 项目编号
     */
    void deleteWorkItemBoardListByProjectId(Long projectId);

}
