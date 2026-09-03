package cn.iocoder.yudao.module.pms.dal.mysql.kb.library;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeGroupRelationDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface PmsKnowledgeGroupRelationMapper extends BaseMapperX<PmsKnowledgeGroupRelationDO> {

    default PmsKnowledgeGroupRelationDO selectByUserIdAndLibraryId(Long userId, Long libraryId) {
        return selectOne(new LambdaQueryWrapperX<PmsKnowledgeGroupRelationDO>()
                .eq(PmsKnowledgeGroupRelationDO::getUserId, userId)
                .eq(PmsKnowledgeGroupRelationDO::getLibraryId, libraryId));
    }

    default List<PmsKnowledgeGroupRelationDO> selectListByUserIdAndLibraryIds(
            Long userId, Collection<Long> libraryIds) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeGroupRelationDO>()
                .eq(PmsKnowledgeGroupRelationDO::getUserId, userId)
                .in(PmsKnowledgeGroupRelationDO::getLibraryId, libraryIds));
    }

    default List<PmsKnowledgeGroupRelationDO> selectListByUserIdAndGroupId(Long userId, Long groupId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeGroupRelationDO>()
                .eq(PmsKnowledgeGroupRelationDO::getUserId, userId)
                .eq(PmsKnowledgeGroupRelationDO::getGroupId, groupId));
    }

    /**
     * 统计用户在指定知识库集合中各分组的知识库数量，使用 DISTINCT 避免重复关系影响统计
     *
     * @param userId 用户编号
     * @param libraryIds 知识库编号集合
     * @return 分组编号 -> 知识库数量
     */
    default Map<Long, Integer> selectGroupLibraryCountMapByUserIdAndLibraryIds(Long userId,
                                                                                 Collection<Long> libraryIds) {
        List<Map<String, Object>> rows = selectMaps(new QueryWrapperX<PmsKnowledgeGroupRelationDO>()
                .select("group_id AS groupId", "COUNT(DISTINCT library_id) AS count")
                .eq("user_id", userId)
                .in("library_id", libraryIds)
                .groupBy("group_id"));
        Map<Long, Integer> groupCountMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            groupCountMap.put(((Number) row.get("groupId")).longValue(), ((Number) row.get("count")).intValue());
        }
        return groupCountMap;
    }

    /**
     * 统计用户在指定知识库集合中已分组的知识库数量
     *
     * @param userId 用户编号
     * @param libraryIds 知识库编号集合
     * @return 已分组知识库数量
     */
    default int selectGroupedLibraryCountByUserIdAndLibraryIds(Long userId, Collection<Long> libraryIds) {
        Map<String, Object> row = CollUtil.getFirst(selectMaps(new QueryWrapperX<PmsKnowledgeGroupRelationDO>()
                .select("COUNT(DISTINCT library_id) AS count")
                .eq("user_id", userId)
                .in("library_id", libraryIds)));
        return row == null ? 0 : ((Number) row.get("count")).intValue();
    }

    default void deleteByUserIdAndGroupId(Long userId, Long groupId) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeGroupRelationDO>()
                .eq(PmsKnowledgeGroupRelationDO::getUserId, userId)
                .eq(PmsKnowledgeGroupRelationDO::getGroupId, groupId));
    }

    default void deleteByUserIdAndLibraryId(Long userId, Long libraryId) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeGroupRelationDO>()
                .eq(PmsKnowledgeGroupRelationDO::getUserId, userId)
                .eq(PmsKnowledgeGroupRelationDO::getLibraryId, libraryId));
    }

    default void deleteByLibraryId(Long libraryId) {
        delete(PmsKnowledgeGroupRelationDO::getLibraryId, libraryId);
    }

}
