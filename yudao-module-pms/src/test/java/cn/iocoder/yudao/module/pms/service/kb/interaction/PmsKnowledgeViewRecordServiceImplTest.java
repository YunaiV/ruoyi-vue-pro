package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.viewrecord.PmsKnowledgeRecentListRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeViewRecordDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeViewRecordMapper;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeViewRecordServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsKnowledgeViewRecordServiceImpl.class)
public class PmsKnowledgeViewRecordServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsKnowledgeViewRecordServiceImpl viewRecordService;

    @Resource
    private PmsKnowledgeViewRecordMapper viewRecordMapper;

    @MockitoBean
    private PmsKnowledgeInteractionTargetService interactionTargetService;
    @MockitoBean
    private PmsKnowledgeLibraryMemberService libraryMemberService;

    @Test
    public void testCreateViewRecord() {
        // mock 数据
        Long userId = randomLongId();
        Long sourceLibraryId = randomLongId();
        Long targetLibraryId = randomLongId();
        Long documentId = randomLongId();
        Integer type = PmsKnowledgeObjectTypeEnum.DOCUMENT.getType();

        // 调用
        viewRecordService.createViewRecord(sourceLibraryId, type, documentId, userId);
        LocalDateTime firstViewTime = CollUtil.getFirst(viewRecordMapper.selectList()).getCreateTime();
        viewRecordService.createViewRecord(targetLibraryId, type, documentId, userId);

        // 断言
        assertEquals(1, viewRecordMapper.selectList().size());
        PmsKnowledgeViewRecordDO record = CollUtil.getFirst(viewRecordMapper.selectList());
        assertEquals(targetLibraryId, record.getLibraryId());
        assertEquals(type, record.getType());
        assertEquals(documentId, record.getEntityId());
        assertEquals(userId, record.getUserId());
        assertFalse(record.getCreateTime().isBefore(firstViewTime));
    }

    @Test
    public void testGetRecentList_groupsAndDeduplicates() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        Long folderId = randomLongId();
        LocalDateTime todayTime = LocalDateTime.now();
        LocalDateTime yesterdayTime = LocalDateTime.now().toLocalDate().atStartOfDay().minusHours(1);
        viewRecordMapper.insert(randomViewRecordDO(userId, libraryId,
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), folderId, todayTime));
        viewRecordMapper.insert(randomViewRecordDO(userId, libraryId,
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), randomLongId(), yesterdayTime));
        when(interactionTargetService.getReadableItemList(anyCollection(), eq(userId))).thenReturn(Arrays.asList(
                new PmsKnowledgeInteractionItemRespVO().setId(randomLongId())
                        .setType(PmsKnowledgeObjectTypeEnum.FOLDER.getType()).setEntityId(folderId)
                        .setCreateTime(todayTime),
                new PmsKnowledgeInteractionItemRespVO().setId(randomLongId())
                        .setType(PmsKnowledgeObjectTypeEnum.DOCUMENT.getType()).setEntityId(randomLongId())
                        .setCreateTime(yesterdayTime)));

        // 调用
        PmsKnowledgeRecentListRespVO recentItems = viewRecordService.getRecentViewRecordList(null, userId);

        // 断言
        assertEquals(1, recentItems.getTodayItems().size());
        assertEquals(folderId, CollUtil.getFirst(recentItems.getTodayItems()).getEntityId());
        assertEquals(1, recentItems.getYesterdayItems().size());
        assertTrue(CollUtil.isEmpty(recentItems.getRecent30DayItems()));
    }

    @Test
    public void testDeleteViewRecordsByEntityIds() {
        // mock 数据
        Long userId = randomLongId();
        Long libraryId = randomLongId();
        Long folderId = randomLongId();
        Long documentId = randomLongId();
        viewRecordMapper.insert(randomViewRecordDO(userId, libraryId,
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), folderId, LocalDateTime.now()));
        viewRecordMapper.insert(randomViewRecordDO(userId, libraryId,
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), documentId, LocalDateTime.now()));

        // 调用
        viewRecordService.deleteViewRecordsByEntityIds(Collections.singleton(folderId),
                Collections.singleton(documentId));

        // 断言
        assertTrue(CollUtil.isEmpty(viewRecordMapper.selectListByUserIdAndCreateTimeAfter(
                userId, LocalDateTime.now().minusDays(1))));
    }

    @Test
    public void testUpdateViewRecordLibraryIdByEntityIds() {
        // mock 数据
        Long userId = randomLongId();
        Long sourceLibraryId = randomLongId();
        Long targetLibraryId = randomLongId();
        Long entityId = randomLongId();
        PmsKnowledgeViewRecordDO folderRecord = randomViewRecordDO(userId, sourceLibraryId,
                PmsKnowledgeObjectTypeEnum.FOLDER.getType(), entityId, LocalDateTime.now());
        viewRecordMapper.insert(folderRecord);
        PmsKnowledgeViewRecordDO fileRecord = randomViewRecordDO(userId, sourceLibraryId,
                PmsKnowledgeObjectTypeEnum.FILE.getType(), entityId, LocalDateTime.now());
        viewRecordMapper.insert(fileRecord);
        PmsKnowledgeViewRecordDO otherRecord = randomViewRecordDO(userId, sourceLibraryId,
                PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), randomLongId(), LocalDateTime.now());
        viewRecordMapper.insert(otherRecord);

        // 调用
        viewRecordService.updateViewRecordLibraryIdByEntityIds(Collections.singleton(entityId),
                Collections.singleton(entityId), targetLibraryId);

        // 断言
        assertEquals(targetLibraryId, viewRecordMapper.selectById(folderRecord.getId()).getLibraryId());
        assertEquals(targetLibraryId, viewRecordMapper.selectById(fileRecord.getId()).getLibraryId());
        assertEquals(sourceLibraryId, viewRecordMapper.selectById(otherRecord.getId()).getLibraryId());
    }

    // ========== 随机对象 ==========

    private PmsKnowledgeViewRecordDO randomViewRecordDO(Long userId, Long libraryId, Integer type,
                                                        Long entityId, LocalDateTime createTime) {
        return randomPojo(PmsKnowledgeViewRecordDO.class, record -> record.setId(null).setLibraryId(libraryId)
                .setType(type).setEntityId(entityId).setUserId(userId).setCreateTime(createTime));
    }

}
