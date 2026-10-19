package cn.iocoder.yudao.module.pms.dal.mysql.pm.iteration;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.pms.controller.admin.pm.iteration.vo.PmsIterationPageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface PmsIterationMapper extends BaseMapperX<PmsIterationDO> {

    default PageResult<PmsIterationDO> selectPage(PmsIterationPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<PmsIterationDO>()
                .eq(PmsIterationDO::getProjectId, pageReqVO.getProjectId())
                .likeIfPresent(PmsIterationDO::getName, pageReqVO.getName())
                .eqIfPresent(PmsIterationDO::getStatus, pageReqVO.getStatus())
                .orderByAsc(PmsIterationDO::getSort)
                .orderByDesc(PmsIterationDO::getId));
    }

    default Integer selectMaxSortByProjectId(Long projectId) {
        PmsIterationDO iteration = selectLastOne(new LambdaQueryWrapperX<PmsIterationDO>()
                .select(PmsIterationDO::getSort)
                .eq(PmsIterationDO::getProjectId, projectId)
                .orderByAsc(PmsIterationDO::getSort)
                .orderByAsc(PmsIterationDO::getId));
        return iteration != null ? iteration.getSort() : null;
    }

    default List<PmsIterationDO> selectListByProjectId(Long projectId) {
        return selectList(PmsIterationDO::getProjectId, projectId);
    }

    /**
     * 分页查询当前用户负责的迭代，用于工作台展示
     *
     * @param pageParam 分页参数
     * @param projectIds 当前用户可访问的项目编号集合
     * @param projectId 项目编号，可选
     * @param name 迭代名称，可选
     * @param status 迭代状态，可选
     * @param endTime 结束时间范围，可选
     * @param completedStatus 已完成状态
     * @param ownerUserId 迭代负责人编号
     * @return 迭代分页结果
     */
    default PageResult<PmsIterationDO> selectWorkbenchPage(PageParam pageParam, Collection<Long> projectIds,
                                                            Long projectId, String name,
                                                            Integer status, LocalDateTime[] endTime,
                                                            Integer completedStatus, Long ownerUserId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<PmsIterationDO>()
                .in(PmsIterationDO::getProjectId, projectIds)
                .eqIfPresent(PmsIterationDO::getProjectId, projectId)
                .eq(PmsIterationDO::getOwnerUserId, ownerUserId)
                .likeIfPresent(PmsIterationDO::getName, name)
                .eqIfPresent(PmsIterationDO::getStatus, status)
                .betweenIfPresent(PmsIterationDO::getEndTime, endTime)
                .ne(status == null, PmsIterationDO::getStatus, completedStatus)
                .orderByAsc(PmsIterationDO::getEndTime)
                .orderByDesc(PmsIterationDO::getId));
    }

    /**
     * 统计当前用户负责的迭代数量，用于工作台统计
     *
     * @param projectIds 当前用户可访问的项目编号集合
     * @param projectId 项目编号，可选
     * @param name 迭代名称，可选
     * @param status 迭代状态，可选
     * @param endTime 结束时间范围，可选
     * @param completedStatus 已完成状态
     * @param ownerUserId 迭代负责人编号
     * @return 迭代数量
     */
    default Long selectWorkbenchCount(Collection<Long> projectIds, Long projectId, String name,
                                      Integer status, LocalDateTime[] endTime,
                                      Integer completedStatus, Long ownerUserId) {
        return selectCount(new LambdaQueryWrapperX<PmsIterationDO>()
                .in(PmsIterationDO::getProjectId, projectIds)
                .eqIfPresent(PmsIterationDO::getProjectId, projectId)
                .eq(PmsIterationDO::getOwnerUserId, ownerUserId)
                .likeIfPresent(PmsIterationDO::getName, name)
                .eqIfPresent(PmsIterationDO::getStatus, status)
                .betweenIfPresent(PmsIterationDO::getEndTime, endTime)
                .ne(status == null, PmsIterationDO::getStatus, completedStatus));
    }

    /**
     * 统计各项目下不同状态的迭代数量
     *
     * @param projectIds 项目编号集合
     * @return 项目编号 ->（状态 -> 迭代数量）
     */
    default Map<Long, Map<Integer, Long>> selectStatusCountMapByProjectIds(Collection<Long> projectIds) {
        List<Map<String, Object>> rows = selectMaps(new QueryWrapperX<PmsIterationDO>()
                .select("project_id AS projectId", "status", "COUNT(*) AS count")
                .in("project_id", projectIds)
                .groupBy("project_id", "status"));
        Map<Long, Map<Integer, Long>> projectStatusCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long projectId = ((Number) row.get("projectId")).longValue();
            Integer status = ((Number) row.get("status")).intValue();
            projectStatusCountMap.computeIfAbsent(projectId, key -> new LinkedHashMap<>())
                    .put(status, ((Number) row.get("count")).longValue());
        }
        return projectStatusCountMap;
    }

    default void deleteByProjectId(Long projectId) {
        delete(PmsIterationDO::getProjectId, projectId);
    }

}
