package cn.iocoder.yudao.module.pms.dal.mysql.kb.recycle;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.recycle.PmsKnowledgeRecycleRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsKnowledgeRecycleRecordMapper extends BaseMapperX<PmsKnowledgeRecycleRecordDO> {

    default PmsKnowledgeRecycleRecordDO selectByTypeAndEntityId(Integer type, Long entityId) {
        return selectOne(PmsKnowledgeRecycleRecordDO::getType, type,
                PmsKnowledgeRecycleRecordDO::getEntityId, entityId);
    }

    default List<PmsKnowledgeRecycleRecordDO> selectListByLibraryId(Long libraryId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeRecycleRecordDO>()
                .eq(PmsKnowledgeRecycleRecordDO::getLibraryId, libraryId)
                .orderByDesc(PmsKnowledgeRecycleRecordDO::getDeleteTime)
                .orderByDesc(PmsKnowledgeRecycleRecordDO::getId));
    }

    default List<PmsKnowledgeRecycleRecordDO> selectListByTypeAndDeleteUserId(Integer type, Long userId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeRecycleRecordDO>()
                .eq(PmsKnowledgeRecycleRecordDO::getType, type)
                .eq(PmsKnowledgeRecycleRecordDO::getDeleteUserId, userId)
                .orderByDesc(PmsKnowledgeRecycleRecordDO::getDeleteTime)
                .orderByDesc(PmsKnowledgeRecycleRecordDO::getId));
    }

    default List<PmsKnowledgeRecycleRecordDO> selectListByDeleteTimeBefore(LocalDateTime deleteTime) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeRecycleRecordDO>()
                .le(PmsKnowledgeRecycleRecordDO::getDeleteTime, deleteTime)
                .orderByAsc(PmsKnowledgeRecycleRecordDO::getDeleteTime)
                .orderByAsc(PmsKnowledgeRecycleRecordDO::getId));
    }

    default void deleteByLibraryId(Long libraryId) {
        delete(PmsKnowledgeRecycleRecordDO::getLibraryId, libraryId);
    }

    default void deleteByTypeAndEntityIds(Collection<Integer> types, Collection<Long> entityIds) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeRecycleRecordDO>()
                .in(PmsKnowledgeRecycleRecordDO::getType, types)
                .in(PmsKnowledgeRecycleRecordDO::getEntityId, entityIds));
    }

}
