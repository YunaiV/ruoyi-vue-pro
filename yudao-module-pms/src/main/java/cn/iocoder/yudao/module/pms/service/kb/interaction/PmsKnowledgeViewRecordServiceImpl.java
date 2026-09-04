package cn.iocoder.yudao.module.pms.service.kb.interaction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.PmsKnowledgeInteractionItemRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.viewrecord.PmsKnowledgeRecentListRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.interaction.PmsKnowledgeViewRecordDO;
import cn.iocoder.yudao.module.pms.dal.mysql.kb.interaction.PmsKnowledgeViewRecordMapper;
import cn.iocoder.yudao.module.pms.enums.kb.PmsKnowledgeObjectTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.library.PmsKnowledgeLibraryMemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * PMS 知识浏览记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsKnowledgeViewRecordServiceImpl implements PmsKnowledgeViewRecordService {

    /**
     * 今天、昨天之外保留的历史自然日数量
     */
    private static final long RECENT_HISTORY_DAYS = 31L;

    @Resource
    private PmsKnowledgeViewRecordMapper viewRecordMapper;

    @Resource
    private PmsKnowledgeInteractionTargetService interactionTargetService;
    @Resource
    private PmsKnowledgeLibraryMemberService libraryMemberService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createViewRecord(Long libraryId, Integer type, Long entityId, Long userId) {
        // 1. 已浏览过同一对象时，更新当前归属和最后访问时间
        LocalDateTime viewTime = LocalDateTime.now();
        if (viewRecordMapper.updateLibraryIdAndCreateTimeByUserIdAndTypeAndEntityId(
                libraryId, viewTime, userId, type, entityId) > 0) {
            return;
        }

        // 2. 首次浏览时创建最近访问记录
        PmsKnowledgeViewRecordDO record = new PmsKnowledgeViewRecordDO()
                .setLibraryId(libraryId).setType(type)
                .setEntityId(entityId).setUserId(userId);
        record.setCreateTime(viewTime);
        viewRecordMapper.insert(record);
    }

    @Override
    public PmsKnowledgeRecentListRespVO getRecentViewRecordList(Long libraryId, Long userId) {
        // 1. 校验指定知识库的读取权限
        if (libraryId != null) {
            libraryMemberService.validateLibraryReadable(libraryId, userId);
        }

        // 2. 查询今天、昨天及此前 30 个自然日的浏览记录（从当天零点回溯 31 天）
        LocalDateTime todayBeginTime = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayBeginTime = todayBeginTime.minusDays(1);
        List<PmsKnowledgeViewRecordDO> records = viewRecordMapper.selectListByUserIdAndCreateTimeAfter(
                userId, todayBeginTime.minusDays(RECENT_HISTORY_DAYS));
        if (libraryId != null) {
            records.removeIf(record -> ObjectUtil.notEqual(libraryId, record.getLibraryId()));
        }
        List<PmsKnowledgeInteractionItemRespVO> items = interactionTargetService.getReadableItemList(
                convertList(records, record -> new PmsKnowledgeInteractionItemRespVO().setId(record.getId())
                        .setType(record.getType()).setEntityId(record.getEntityId())
                        .setLibraryId(record.getLibraryId()).setCreateTime(record.getCreateTime())), userId);

        // 3. 按今天、昨天和此前 30 天分组，同一分组内每个对象只保留最近一次访问
        List<PmsKnowledgeInteractionItemRespVO> todayItems = new ArrayList<>();
        List<PmsKnowledgeInteractionItemRespVO> yesterdayItems = new ArrayList<>();
        List<PmsKnowledgeInteractionItemRespVO> recent30DayItems = new ArrayList<>();
        Set<String> todayKeys = new LinkedHashSet<>();
        Set<String> yesterdayKeys = new LinkedHashSet<>();
        Set<String> recentKeys = new LinkedHashSet<>();
        for (PmsKnowledgeInteractionItemRespVO item : items) {
            String key = item.getType() + ":" + item.getEntityId();
            if (!item.getCreateTime().isBefore(todayBeginTime)) {
                addRecentItem(todayItems, todayKeys, key, item);
            } else if (!item.getCreateTime().isBefore(yesterdayBeginTime)) {
                addRecentItem(yesterdayItems, yesterdayKeys, key, item);
            } else {
                addRecentItem(recent30DayItems, recentKeys, key, item);
            }
        }
        return new PmsKnowledgeRecentListRespVO().setTodayItems(todayItems).setYesterdayItems(yesterdayItems)
                .setRecent30DayItems(recent30DayItems);
    }

    @Override
    public void deleteViewRecordsByLibraryId(Long libraryId) {
        viewRecordMapper.deleteByLibraryId(libraryId);
    }

    @Override
    public void deleteViewRecordsByEntityIds(Collection<Long> folderIds, Collection<Long> documentIds) {
        if (CollUtil.isNotEmpty(folderIds)) {
            viewRecordMapper.deleteByTypeAndEntityIds(Collections.singleton(
                    PmsKnowledgeObjectTypeEnum.FOLDER.getType()), folderIds);
        }
        if (CollUtil.isNotEmpty(documentIds)) {
            viewRecordMapper.deleteByTypeAndEntityIds(Arrays.asList(PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(),
                    PmsKnowledgeObjectTypeEnum.FILE.getType()), documentIds);
        }
    }

    @Override
    public void updateViewRecordLibraryIdByEntityIds(Collection<Long> folderIds, Collection<Long> documentIds,
                                                     Long libraryId) {
        if (CollUtil.isNotEmpty(folderIds)) {
            viewRecordMapper.updateLibraryIdByTypeAndEntityIds(libraryId,
                    Collections.singleton(PmsKnowledgeObjectTypeEnum.FOLDER.getType()), folderIds);
        }
        if (CollUtil.isNotEmpty(documentIds)) {
            viewRecordMapper.updateLibraryIdByTypeAndEntityIds(libraryId,
                    Arrays.asList(PmsKnowledgeObjectTypeEnum.DOCUMENT.getType(), PmsKnowledgeObjectTypeEnum.FILE.getType()), documentIds);
        }
    }

    private void addRecentItem(List<PmsKnowledgeInteractionItemRespVO> items, Set<String> keys,
                               String key, PmsKnowledgeInteractionItemRespVO item) {
        if (keys.add(key)) {
            items.add(item);
        }
    }

}
