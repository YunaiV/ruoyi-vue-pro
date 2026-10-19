package cn.iocoder.yudao.module.pms.dal.mysql.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectGroupRelationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface PmsProjectGroupRelationMapper extends BaseMapperX<PmsProjectGroupRelationDO> {

    default PmsProjectGroupRelationDO selectByUserIdAndProjectId(Long userId, Long projectId) {
        return selectFirstOne(PmsProjectGroupRelationDO::getUserId, userId,
                PmsProjectGroupRelationDO::getProjectId, projectId);
    }

    default List<PmsProjectGroupRelationDO> selectListByUserIdAndProjectIds(
            Long userId, Collection<Long> projectIds) {
        return selectList(new LambdaQueryWrapperX<PmsProjectGroupRelationDO>()
                .eq(PmsProjectGroupRelationDO::getUserId, userId)
                .in(PmsProjectGroupRelationDO::getProjectId, projectIds));
    }

    default List<PmsProjectGroupRelationDO> selectListByUserIdAndGroupId(Long userId, Long groupId) {
        return selectList(new LambdaQueryWrapperX<PmsProjectGroupRelationDO>()
                .eq(PmsProjectGroupRelationDO::getUserId, userId)
                .eq(PmsProjectGroupRelationDO::getGroupId, groupId));
    }

    /**
     * 统计用户在指定项目集合中各分组的项目数量，使用 DISTINCT 避免重复关系影响统计
     *
     * @param userId 用户编号
     * @param projectIds 项目编号集合
     * @return 分组编号 -> 项目数量
     */
    default Map<Long, Integer> selectGroupCountMapByUserIdAndProjectIds(Long userId, Collection<Long> projectIds) {
        List<Map<String, Object>> rows = selectMaps(new QueryWrapperX<PmsProjectGroupRelationDO>()
                .select("group_id AS groupId", "COUNT(DISTINCT project_id) AS count")
                .eq("user_id", userId)
                .in("project_id", projectIds)
                .groupBy("group_id"));
        Map<Long, Integer> groupCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            groupCountMap.put(((Number) row.get("groupId")).longValue(),
                    ((Number) row.get("count")).intValue());
        }
        return groupCountMap;
    }

    /**
     * 统计用户在指定项目集合中已分组的项目数量
     *
     * @param userId 用户编号
     * @param projectIds 项目编号集合
     * @return 已分组项目数量
     */
    default int selectProjectCountByUserIdAndProjectIds(Long userId, Collection<Long> projectIds) {
        Map<String, Object> row = CollUtil.getFirst(selectMaps(new QueryWrapperX<PmsProjectGroupRelationDO>()
                .select("COUNT(DISTINCT project_id) AS count")
                .eq("user_id", userId)
                .in("project_id", projectIds)));
        return row == null ? 0 : ((Number) row.get("count")).intValue();
    }

    default void deleteByUserIdAndGroupId(Long userId, Long groupId) {
        delete(new LambdaQueryWrapperX<PmsProjectGroupRelationDO>()
                .eq(PmsProjectGroupRelationDO::getUserId, userId)
                .eq(PmsProjectGroupRelationDO::getGroupId, groupId));
    }

    default void deleteByUserIdAndProjectId(Long userId, Long projectId) {
        delete(new LambdaQueryWrapperX<PmsProjectGroupRelationDO>()
                .eq(PmsProjectGroupRelationDO::getUserId, userId)
                .eq(PmsProjectGroupRelationDO::getProjectId, projectId));
    }

    default void deleteByProjectId(Long projectId) {
        delete(PmsProjectGroupRelationDO::getProjectId, projectId);
    }

}
