package cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite.PmsKnowledgeFavoritePageReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeFavoriteDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface PmsKnowledgeFavoriteMapper extends BaseMapperX<PmsKnowledgeFavoriteDO> {

    default PageResult<PmsKnowledgeFavoriteDO> selectPage(PmsKnowledgeFavoritePageReqVO pageReqVO,
                                                          Long userId, Collection<Long> readableFavoriteIds) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<PmsKnowledgeFavoriteDO>()
                .eq(PmsKnowledgeFavoriteDO::getUserId, userId)
                .in(PmsKnowledgeFavoriteDO::getId, readableFavoriteIds)
                .eqIfPresent(PmsKnowledgeFavoriteDO::getType, pageReqVO.getType())
                .orderByDesc(PmsKnowledgeFavoriteDO::getCreateTime)
                .orderByDesc(PmsKnowledgeFavoriteDO::getId));
    }

    default PmsKnowledgeFavoriteDO selectByUserIdAndTypeAndEntityId(Long userId, Integer type, Long entityId) {
        return selectOne(new LambdaQueryWrapperX<PmsKnowledgeFavoriteDO>()
                .eq(PmsKnowledgeFavoriteDO::getUserId, userId)
                .eq(PmsKnowledgeFavoriteDO::getType, type)
                .eq(PmsKnowledgeFavoriteDO::getEntityId, entityId));
    }

    default List<PmsKnowledgeFavoriteDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeFavoriteDO>()
                .eq(PmsKnowledgeFavoriteDO::getUserId, userId)
                .orderByDesc(PmsKnowledgeFavoriteDO::getCreateTime)
                .orderByDesc(PmsKnowledgeFavoriteDO::getId));
    }

    default List<PmsKnowledgeFavoriteDO> selectListByUserIdAndLibraryId(Long userId, Long libraryId) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeFavoriteDO>()
                .eq(PmsKnowledgeFavoriteDO::getUserId, userId)
                .eq(PmsKnowledgeFavoriteDO::getLibraryId, libraryId)
                .orderByDesc(PmsKnowledgeFavoriteDO::getCreateTime)
                .orderByDesc(PmsKnowledgeFavoriteDO::getId));
    }

    default List<PmsKnowledgeFavoriteDO> selectListByUserIdAndTypeAndEntityIds(
        Long userId, Integer type, Collection<Long> entityIds) {
        return selectList(new LambdaQueryWrapperX<PmsKnowledgeFavoriteDO>()
                .eq(PmsKnowledgeFavoriteDO::getUserId, userId)
                .eq(PmsKnowledgeFavoriteDO::getType, type)
                .in(PmsKnowledgeFavoriteDO::getEntityId, entityIds));
    }

    default void deleteByLibraryId(Long libraryId) {
        delete(PmsKnowledgeFavoriteDO::getLibraryId, libraryId);
    }

    default void deleteByTypeAndEntityIds(Collection<Integer> types, Collection<Long> entityIds) {
        delete(new LambdaQueryWrapperX<PmsKnowledgeFavoriteDO>()
                .in(PmsKnowledgeFavoriteDO::getType, types)
                .in(PmsKnowledgeFavoriteDO::getEntityId, entityIds));
    }

    default void updateLibraryIdByTypeAndEntityIds(Long libraryId,
                                                   Collection<Integer> types, Collection<Long> entityIds) {
        update(new PmsKnowledgeFavoriteDO().setLibraryId(libraryId),
                new LambdaQueryWrapperX<PmsKnowledgeFavoriteDO>()
                        .in(PmsKnowledgeFavoriteDO::getType, types)
                        .in(PmsKnowledgeFavoriteDO::getEntityId, entityIds));
    }

}
