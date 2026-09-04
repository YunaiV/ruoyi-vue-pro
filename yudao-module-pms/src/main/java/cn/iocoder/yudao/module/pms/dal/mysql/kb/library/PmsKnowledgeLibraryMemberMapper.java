package cn.iocoder.yudao.module.pms.dal.mysql.kb.library;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.library.PmsKnowledgeLibraryMemberDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Mapper
public interface PmsKnowledgeLibraryMemberMapper extends BaseMapperX<PmsKnowledgeLibraryMemberDO> {

    default PmsKnowledgeLibraryMemberDO selectByLibraryIdAndUserId(Long libraryId, Long userId) {
        return selectOne(PmsKnowledgeLibraryMemberDO::getLibraryId, libraryId,
                PmsKnowledgeLibraryMemberDO::getUserId, userId);
    }

    default PmsKnowledgeLibraryMemberDO selectByLibraryIdAndDeptId(Long libraryId, Long deptId) {
        return selectOne(PmsKnowledgeLibraryMemberDO::getLibraryId, libraryId,
                PmsKnowledgeLibraryMemberDO::getDeptId, deptId);
    }

    default List<PmsKnowledgeLibraryMemberDO> selectListByDeptIds(Collection<Long> deptIds) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeLibraryMemberDO>()
                .in(PmsKnowledgeLibraryMemberDO::getDeptId, deptIds));
    }

    default List<PmsKnowledgeLibraryMemberDO> selectListByLibraryId(Long libraryId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeLibraryMemberDO>()
                .eq(PmsKnowledgeLibraryMemberDO::getLibraryId, libraryId)
                .orderByAsc(PmsKnowledgeLibraryMemberDO::getLevel)
                .orderByAsc(PmsKnowledgeLibraryMemberDO::getId));
    }

    default List<PmsKnowledgeLibraryMemberDO> selectListByLibraryIds(Collection<Long> libraryIds) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeLibraryMemberDO>()
                .in(PmsKnowledgeLibraryMemberDO::getLibraryId, libraryIds)
                .orderByAsc(PmsKnowledgeLibraryMemberDO::getLibraryId)
                .orderByAsc(PmsKnowledgeLibraryMemberDO::getLevel)
                .orderByAsc(PmsKnowledgeLibraryMemberDO::getId));
    }

    default List<Long> selectLibraryIdListByUserIdOrDeptId(Long userId, Long deptId) {
        LambdaQueryWrapperX<PmsKnowledgeLibraryMemberDO> query = new LambdaQueryWrapperX<>();
        query.eq(PmsKnowledgeLibraryMemberDO::getUserId, userId);
        if (deptId != null) {
            query.or().eq(PmsKnowledgeLibraryMemberDO::getDeptId, deptId);
        }
        return convertList(selectList(query), PmsKnowledgeLibraryMemberDO::getLibraryId);
    }

    default void deleteByLibraryIdAndLevels(Long libraryId, Collection<Integer> levels) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeLibraryMemberDO>()
                .eq(PmsKnowledgeLibraryMemberDO::getLibraryId, libraryId)
                .in(PmsKnowledgeLibraryMemberDO::getLevel, levels));
    }

    default void deleteByLibraryId(Long libraryId) {
        delete(PmsKnowledgeLibraryMemberDO::getLibraryId, libraryId);
    }

    default void deleteByLibraryIdAndUserId(Long libraryId, Long userId) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeLibraryMemberDO>()
                .eq(PmsKnowledgeLibraryMemberDO::getLibraryId, libraryId)
                .eq(PmsKnowledgeLibraryMemberDO::getUserId, userId));
    }

}
