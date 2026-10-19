package cn.iocoder.yudao.module.pms.service.pm.workbench;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchCountRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;

/**
 * PMS 工作台 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsWorkbenchService {

    /**
     * 获得当前用户在活跃项目中被分配的工作项分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 工作项分页
     */
    PageResult<PmsWorkItemDO> getWorkbenchWorkItemPage(PmsWorkbenchPageReqVO pageReqVO, Long userId);

    /**
     * 获得当前用户参与的活跃项目的未完成迭代分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 迭代分页
     */
    PageResult<PmsIterationDO> getWorkbenchIterationPage(PmsWorkbenchPageReqVO pageReqVO, Long userId);

    /**
     * 获得工作台待办数量，按工作项类型和迭代统计
     *
     * @param pageReqVO 查询条件
     * @param userId 用户编号
     * @return 工作台数量
     */
    PmsWorkbenchCountRespVO getWorkbenchCount(PmsWorkbenchPageReqVO pageReqVO, Long userId);

}
