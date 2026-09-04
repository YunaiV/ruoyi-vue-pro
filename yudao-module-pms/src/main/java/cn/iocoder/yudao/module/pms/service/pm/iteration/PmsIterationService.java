package cn.iocoder.yudao.module.pms.service.pm.iteration;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationOverviewRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationStartReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workbench.vo.PmsWorkbenchPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * PMS 项目迭代 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsIterationService {

    /**
     * 创建项目迭代
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     * @return 迭代编号
     */
    Long createIteration(PmsIterationSaveReqVO saveReqVO, Long userId);

    /**
     * 更新项目迭代
     *
     * @param saveReqVO 保存信息
     * @param userId 用户编号
     */
    void updateIteration(PmsIterationSaveReqVO saveReqVO, Long userId);

    /**
     * 开始项目迭代
     *
     * @param startReqVO 开始信息
     * @param userId 用户编号
     */
    void startIteration(PmsIterationStartReqVO startReqVO, Long userId);

    /**
     * 完成项目迭代
     *
     * @param id 迭代编号
     * @param userId 用户编号
     */
    void completeIteration(Long id, Long userId);

    /**
     * 删除项目迭代
     *
     * @param id 迭代编号
     * @param userId 用户编号
     */
    void deleteIteration(Long id, Long userId);

    /**
     * 删除指定项目的全部迭代
     *
     * @param projectId 项目编号
     */
    void deleteIterationListByProjectId(Long projectId);

    /**
     * 获得项目迭代
     *
     * @param id 迭代编号
     * @param userId 用户编号
     * @return 项目迭代
     */
    PmsIterationDO getIteration(Long id, Long userId);

    /**
     * 获得项目迭代概况
     *
     * @param id 迭代编号
     * @param userId 用户编号
     * @return 迭代概况
     */
    PmsIterationOverviewRespVO getIterationOverview(Long id, Long userId);

    /**
     * 获得项目迭代分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 项目迭代分页
     */
    PageResult<PmsIterationDO> getIterationPage(PmsIterationPageReqVO pageReqVO, Long userId);

    /**
     * 获得迭代完成进度 Map
     *
     * @param ids 迭代编号集合
     * @return 迭代编号到完成进度 Map
     */
    Map<Long, Integer> getIterationProgressMap(Collection<Long> ids);

    /**
     * 获得指定编号的项目迭代列表
     *
     * @param ids 迭代编号集合
     * @return 项目迭代列表
     */
    List<PmsIterationDO> getIterationList(Collection<Long> ids);

    /**
     * 获得项目迭代 Map
     *
     * @param ids 迭代编号集合
     * @return 项目迭代 Map
     */
    default Map<Long, PmsIterationDO> getIterationMap(Collection<Long> ids) {
        return convertMap(getIterationList(ids), PmsIterationDO::getId);
    }

    /**
     * 获得指定项目范围内的未完成迭代分页
     *
     * @param pageReqVO 工作台分页条件
     * @param projectIds 项目编号集合
     * @param ownerUserId 负责人用户编号
     * @return 迭代分页
     */
    PageResult<PmsIterationDO> getUncompletedIterationPage(PmsWorkbenchPageReqVO pageReqVO,
                                                           Collection<Long> projectIds, Long ownerUserId);

    /**
     * 获得指定项目范围内的未完成迭代数量
     *
     * @param pageReqVO 工作台查询条件
     * @param projectIds 项目编号集合
     * @param ownerUserId 负责人用户编号
     * @return 迭代数量
     */
    Long getUncompletedIterationCount(PmsWorkbenchPageReqVO pageReqVO,
                                      Collection<Long> projectIds, Long ownerUserId);

    /**
     * 获得指定项目范围内按状态分组的迭代数量
     *
     * @param projectIds 项目编号集合
     * @return 项目编号、状态和数量的映射
     */
    Map<Long, Map<Integer, Long>> getProjectIterationStatusCountMap(Collection<Long> projectIds);

}
