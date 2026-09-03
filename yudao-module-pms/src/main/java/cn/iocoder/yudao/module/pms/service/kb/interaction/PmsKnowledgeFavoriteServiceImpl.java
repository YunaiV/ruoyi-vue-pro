package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite.PmsKnowledgeFavoritePageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite.PmsKnowledgeFavoriteSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeFavoriteDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeFavoriteMapper;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_FAVORITE_DUPLICATE;

/**
 * PMS 知识收藏（关注）Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeFavoriteServiceImpl implements PmsKnowledgeFavoriteService {

    @Resource
    private PmsKnowledgeFavoriteMapper favoriteMapper;
    @Resource
    private PmsKnowledgeInteractionTargetService interactionTargetService;

    @Override
    public void createFavorite(PmsKnowledgeFavoriteSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验关注对象可读
        Long libraryId = interactionTargetService.validateTargetReadable(
                saveReqVO.getType(), saveReqVO.getEntityId(), userId);
        // 1.2 校验关注关系不存在
        if (favoriteMapper.selectByUserIdAndTypeAndEntityId(
                userId, saveReqVO.getType(), saveReqVO.getEntityId()) != null) {
            throw exception(KNOWLEDGE_FAVORITE_DUPLICATE);
        }

        // 2. 新增关注关系
        favoriteMapper.insert(BeanUtils.toBean(saveReqVO, PmsKnowledgeFavoriteDO.class)
                .setLibraryId(libraryId).setUserId(userId));
    }

    @Override
    public void deleteFavorite(Integer type, Long entityId, Long userId) {
        // 1. 查询当前用户的关注关系
        PmsKnowledgeFavoriteDO favorite = favoriteMapper.selectByUserIdAndTypeAndEntityId(userId, type, entityId);
        // 2. 存在时删除关注关系，未关注时不做任何处理
        if (favorite != null) {
            favoriteMapper.deleteById(favorite.getId());
        }
    }

    @Override
    public PageResult<PmsKnowledgeInteractionItemRespVO> getFavoritePage(PmsKnowledgeFavoritePageReqVO pageReqVO,
                                                                         Long userId) {
        // 1. 先批量判定全部关注对象的可读性
        List<PmsKnowledgeInteractionItemRespVO> allItems = getReadableFavoriteList(
                favoriteMapper.selectListByUserId(userId), userId);
        if (CollUtil.isEmpty(allItems)) {
            return PageResult.empty();
        }
        Map<Long, PmsKnowledgeInteractionItemRespVO> itemMap = new LinkedHashMap<>();
        allItems.forEach(item -> itemMap.put(item.getId(), item));

        // 2. 将类型、排序和分页交给数据库，再按页拼接已批量解析的展示对象
        PageResult<PmsKnowledgeFavoriteDO> favoritePage = favoriteMapper.selectPage(
                pageReqVO, userId, itemMap.keySet());
        return new PageResult<>(convertList(favoritePage.getList(), favorite -> itemMap.get(favorite.getId())),
                favoritePage.getTotal());
    }

    @Override
    public List<PmsKnowledgeInteractionItemRespVO> getFavoriteListByLibraryId(Long libraryId, Long userId) {
        // 1. 查询指定知识库内当前用户的关注对象
        List<PmsKnowledgeFavoriteDO> favorites = favoriteMapper.selectListByUserIdAndLibraryId(userId, libraryId);
        // 2. 复用统一的对象可读校验和展示字段组装
        return getReadableFavoriteList(favorites, userId);
    }

    /**
     * 将关注记录转换为当前用户可读取的互动对象
     *
     * @param favorites 关注记录
     * @param userId 用户编号
     * @return 可读取的互动对象列表
     */
    private List<PmsKnowledgeInteractionItemRespVO> getReadableFavoriteList(
            List<PmsKnowledgeFavoriteDO> favorites, Long userId) {
        if (CollUtil.isEmpty(favorites)) {
            return Collections.emptyList();
        }
        List<PmsKnowledgeInteractionItemRespVO> items = convertList(favorites, favorite ->
                new PmsKnowledgeInteractionItemRespVO().setId(favorite.getId()).setType(favorite.getType())
                        .setEntityId(favorite.getEntityId()).setLibraryId(favorite.getLibraryId())
                        .setCreateTime(favorite.getCreateTime()));
        return interactionTargetService.getReadableItemList(items, userId);
    }

    @Override
    public Set<Long> getFavoriteEntityIdSet(Integer type, Collection<Long> entityIds, Long userId) {
        if (CollUtil.isEmpty(entityIds)) {
            return Collections.emptySet();
        }
        List<PmsKnowledgeFavoriteDO> favorites = favoriteMapper
                .selectListByUserIdAndTypeAndEntityIds(userId, type, entityIds);
        return convertSet(favorites, PmsKnowledgeFavoriteDO::getEntityId);
    }

    @Override
    public boolean isFavorite(Integer type, Long entityId, Long userId) {
        return favoriteMapper.selectByUserIdAndTypeAndEntityId(userId, type, entityId) != null;
    }

    @Override
    public void deleteFavoritesByLibraryId(Long libraryId) {
        favoriteMapper.deleteByLibraryId(libraryId);
    }

    @Override
    public void deleteFavoritesByEntityIds(Collection<Long> folderIds, Collection<Long> documentIds) {
        if (CollUtil.isNotEmpty(folderIds)) {
            favoriteMapper.deleteByTypeAndEntityIds(
                    Collections.singleton(PmsKnowledgeObjectTypeEnum.FOLDER.getType()), folderIds);
        }
        if (CollUtil.isNotEmpty(documentIds)) {
            favoriteMapper.deleteByTypeAndEntityIds(Arrays.asList(PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(),
                    PmsKnowledgeObjectTypeEnum.FILE.getType()), documentIds);
        }
    }

    @Override
    public void updateFavoriteLibraryIdByEntityIds(Collection<Long> folderIds, Collection<Long> documentIds,
                                                   Long libraryId) {
        if (CollUtil.isNotEmpty(folderIds)) {
            favoriteMapper.updateLibraryIdByTypeAndEntityIds(libraryId,
                    Collections.singleton(PmsKnowledgeObjectTypeEnum.FOLDER.getType()), folderIds);
        }
        if (CollUtil.isNotEmpty(documentIds)) {
            favoriteMapper.updateLibraryIdByTypeAndEntityIds(libraryId,
                    Arrays.asList(PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), PmsKnowledgeObjectTypeEnum.FILE.getType()), documentIds);
        }
    }

}
