package cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeViewRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsKnowledgeViewRecordMapper extends BaseMapperX<PmsKnowledgeViewRecordDO> {

    default List<PmsKnowledgeViewRecordDO> selectListByUserIdAndCreateTimeAfter(
            Long userId, LocalDateTime beginTime) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeViewRecordDO>()
                .eq(PmsKnowledgeViewRecordDO::getUserId, userId)
                .ge(PmsKnowledgeViewRecordDO::getCreateTime, beginTime)
                .orderByDesc(PmsKnowledgeViewRecordDO::getCreateTime)
                .orderByDesc(PmsKnowledgeViewRecordDO::getId));
    }

    default int updateLibraryIdAndCreateTimeByUserIdAndTypeAndEntityId(
            Long libraryId, LocalDateTime createTime, Long userId, Integer type, Long entityId) {
        PmsKnowledgeViewRecordDO updateObj = new PmsKnowledgeViewRecordDO().setLibraryId(libraryId);
        updateObj.setCreateTime(createTime);
        return update(updateObj,
                new LambdaQueryWrapperX<PmsKnowledgeViewRecordDO>()
                        .eq(PmsKnowledgeViewRecordDO::getUserId, userId)
                        .eq(PmsKnowledgeViewRecordDO::getType, type)
                        .eq(PmsKnowledgeViewRecordDO::getEntityId, entityId));
    }

    default void deleteByLibraryId(Long libraryId) {
        delete(PmsKnowledgeViewRecordDO::getLibraryId, libraryId);
    }

    default void deleteByTypeAndEntityIds(Collection<Integer> types, Collection<Long> entityIds) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeViewRecordDO>()
                .in(PmsKnowledgeViewRecordDO::getType, types)
                .in(PmsKnowledgeViewRecordDO::getEntityId, entityIds));
    }

    default void updateLibraryIdByTypeAndEntityIds(Long libraryId, Collection<Integer> types,
                                                    Collection<Long> entityIds) {
        update(new PmsKnowledgeViewRecordDO().setLibraryId(libraryId),
                new LambdaQueryWrapperX<PmsKnowledgeViewRecordDO>()
                        .in(PmsKnowledgeViewRecordDO::getType, types)
                        .in(PmsKnowledgeViewRecordDO::getEntityId, entityIds));
    }

}
