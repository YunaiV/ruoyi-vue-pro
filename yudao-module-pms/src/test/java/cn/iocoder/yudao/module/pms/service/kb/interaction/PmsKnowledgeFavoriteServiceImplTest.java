package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite.PmsKnowledgeFavoritePageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.favorite.PmsKnowledgeFavoriteSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeFavoriteDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeFavoriteMapper;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.KNOWLEDGE_FAVORITE_DUPLICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeFavoriteServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeFavoriteServiceImpl.class)
public class PmsKnowledgeFavoriteServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeFavoriteServiceImpl favoriteService;

    @Resource
    private PmsKnowledgeFavoriteMapper favoriteMapper;

    @MockitoBean
    private PmsKnowledgeInteractionTargetService interactionTargetService;

    @Test
    public void testCreateFavorite_duplicateAndDelete() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        PmsKnowledgeFavoriteSaveReqVO saveReqVO = new PmsKnowledgeFavoriteSaveReqVO()
                .setType(PmsKnowledgeObjectTypeEnum.LIBRARY.getType()).setEntityId(libraryId);
        when(interactionTargetService.validateTargetReadable(saveReqVO.getType(), libraryId, userId))
                .thenReturn(libraryId);

        // 调用创建，并断言重复关注异常
        favoriteService.createFavorite(saveReqVO, userId);
        assertTrue(favoriteService.isFavorite(saveReqVO.getType(), libraryId, userId));
        assertServiceException(() -> favoriteService.createFavorite(saveReqVO, userId),
                KNOWLEDGE_FAVORITE_DUPLICATE);
        assertNotNull(favoriteMapper.selectByUserIdAndTypeAndEntityId(userId, saveReqVO.getType(), libraryId));

        // 调用取消关注，并断言
        favoriteService.deleteFavorite(saveReqVO.getType(), libraryId, userId);
        assertFalse(favoriteService.isFavorite(saveReqVO.getType(), libraryId, userId));
        assertNull(favoriteMapper.selectByUserIdAndTypeAndEntityId(userId, saveReqVO.getType(), libraryId));
    }

    @Test
    public void testGetFavoritePage_filters() {
        // mock 数据
        Long userId = randomLongId();
        PmsKnowledgeFavoriteDO firstFolder = randomFavoriteDO(userId, randomLongId(),
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), randomLongId());
        favoriteMapper.insert(firstFolder);
        PmsKnowledgeFavoriteDO secondFolder = randomFavoriteDO(userId, randomLongId(),
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), randomLongId());
        favoriteMapper.insert(secondFolder);
        PmsKnowledgeFavoriteDO document = randomFavoriteDO(userId, randomLongId(),
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), randomLongId());
        favoriteMapper.insert(document);
        when(interactionTargetService.getReadableItemList(anyCollection(), eq(userId))).thenReturn(Arrays.asList(
                new PmsKnowledgeInteractionItemRespVO().setId(firstFolder.getId())
                        .setType(PmsKnowledgeObjectTypeEnum.FOLDER.getType())
                        .setName("产品资料").setLibraryName("产品知识库"),
                new PmsKnowledgeInteractionItemRespVO().setId(secondFolder.getId())
                        .setType(PmsKnowledgeObjectTypeEnum.FOLDER.getType())
                        .setName("设计资料").setLibraryName("产品知识库"),
                new PmsKnowledgeInteractionItemRespVO().setId(document.getId())
                        .setType(PmsKnowledgeObjectTypeEnum.DOCUMENT.getType())
                        .setName("产品说明").setLibraryName("产品知识库")));
        PmsKnowledgeFavoritePageReqVO pageReqVO = new PmsKnowledgeFavoritePageReqVO()
                .setType(PmsKnowledgeObjectTypeEnum.FOLDER.getType());
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(1);

        // 调用
        PageResult<PmsKnowledgeInteractionItemRespVO> favoritePage = favoriteService.getFavoritePage(pageReqVO,
                userId);

        // 断言
        assertEquals(2, favoritePage.getTotal());
        assertEquals(1, favoritePage.getList().size());
        assertEquals(PmsKnowledgeObjectTypeEnum.FOLDER.getType(),
                CollUtil.getFirst(favoritePage.getList()).getType());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetFavoriteListByLibraryId_filtersLibrary() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        favoriteMapper.insert(randomFavoriteDO(userId, libraryId,
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), randomLongId()));
        favoriteMapper.insert(randomFavoriteDO(userId, randomLongId(),
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), randomLongId()));
        when(interactionTargetService.getReadableItemList(anyCollection(), eq(userId)))
                .thenAnswer(invocation -> (List<PmsKnowledgeInteractionItemRespVO>) invocation.getArgument(0));

        // 调用
        List<PmsKnowledgeInteractionItemRespVO> favoriteList = favoriteService
                .getFavoriteListByLibraryId(libraryId, userId);

        // 断言
        assertEquals(1, favoriteList.size());
        assertEquals(libraryId, CollUtil.getFirst(favoriteList).getLibraryId());
    }

    @Test
    public void testDeleteFavoritesByEntityIds() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        Long folderId = randomLongId();
        Long documentId = randomLongId();
        favoriteMapper.insert(randomFavoriteDO(userId, libraryId,
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), folderId));
        favoriteMapper.insert(randomFavoriteDO(userId, libraryId,
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), documentId));

        // 调用
        favoriteService.deleteFavoritesByEntityIds(Collections.singleton(folderId),
                Collections.singleton(documentId));

        // 断言
        assertTrue(CollUtil.isEmpty(favoriteMapper.selectListByUserId(userId)));
    }

    @Test
    public void testUpdateFavoriteLibraryIdByEntityIds() {
        // mock 数据
        Long userId = randomLongId();
        Long sourceLibraryId = randomLongId();
        Long targetLibraryId = randomLongId();
        Long entityId = randomLongId();
        favoriteMapper.insert(randomFavoriteDO(userId, sourceLibraryId,
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), entityId));
        favoriteMapper.insert(randomFavoriteDO(userId, sourceLibraryId,
                PmsKnowledgeObjectTypeEnum.FILE.getType(), entityId));
        favoriteMapper.insert(randomFavoriteDO(userId, sourceLibraryId,
                PmsKnowledgeObjectTypeEnum.LIBRARY.getType(), entityId));

        // 调用
        favoriteService.updateFavoriteLibraryIdByEntityIds(Collections.singleton(entityId),
                Collections.singleton(entityId), targetLibraryId);

        // 断言
        assertEquals(targetLibraryId, favoriteMapper.selectByUserIdAndTypeAndEntityId(userId,
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), entityId).getLibraryId());
        assertEquals(targetLibraryId, favoriteMapper.selectByUserIdAndTypeAndEntityId(userId,
                PmsKnowledgeObjectTypeEnum.FILE.getType(), entityId).getLibraryId());
        assertEquals(sourceLibraryId, favoriteMapper.selectByUserIdAndTypeAndEntityId(userId,
                PmsKnowledgeObjectTypeEnum.LIBRARY.getType(), entityId).getLibraryId());
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeFavoriteDO randomFavoriteDO(Long userId, Long libraryId, Integer type, Long entityId) {
        return randomPojo(PmsKnowledgeFavoriteDO.class, favorite -> favorite.setId(null).setLibraryId(libraryId)
                .setType(type).setEntityId(entityId).setUserId(userId));
    }

}
