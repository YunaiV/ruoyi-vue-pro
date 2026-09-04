package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.iteration.PmsIterationDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemActivityDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemLabelDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemActivityMapper;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemDefectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemPriorityEnum;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import javax.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * PMS 工作项动态 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsWorkItemActivityServiceImpl implements PmsWorkItemActivityService {

    @Resource
    private PmsWorkItemActivityMapper activityMapper;

    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsWorkItemService workItemService;
    @Resource
    private PmsWorkItemLabelService workItemLabelService;
    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsIterationService iterationService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public void createWorkItemUpdateActivities(PmsWorkItemDO oldWorkItem, PmsWorkItemDO newWorkItem,
                                               Collection<Long> oldMemberUserIds, Collection<Long> newMemberUserIds,
                                               Long userId) {
        // 1.1 收集动态展示需要的用户、标签、迭代和关联工作项编号
        Set<Long> userIds = new LinkedHashSet<>();
        userIds.addAll(oldMemberUserIds);
        userIds.addAll(newMemberUserIds);
        userIds.addAll(convertSet(Arrays.asList(oldWorkItem.getAssigneeUserId(), newWorkItem.getAssigneeUserId())));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Set<Long> labelIds = new LinkedHashSet<>();
        labelIds.addAll(convertSet(oldWorkItem.getLabelIds()));
        labelIds.addAll(convertSet(newWorkItem.getLabelIds()));
        // 1.2 批量查询动态展示需要的关联数据
        Map<Long, PmsWorkItemLabelDO> labelMap = workItemLabelService.getWorkItemLabelMap(labelIds);
        Set<Long> iterationIds = convertSet(Arrays.asList(oldWorkItem.getIterationId(), newWorkItem.getIterationId()));
        Map<Long, PmsIterationDO> iterationMap = iterationService.getIterationMap(iterationIds);
        Set<Long> relatedWorkItemIds = convertSet(Arrays.asList(oldWorkItem.getParentId(), newWorkItem.getParentId(),
                oldWorkItem.getRelatedRequirementId(), newWorkItem.getRelatedRequirementId()));
        Map<Long, PmsWorkItemDO> relatedWorkItemMap = convertMap(workItemService.getWorkItemList(relatedWorkItemIds),
                PmsWorkItemDO::getId);

        // 2.1 记录每个发生变化的字段，便于动态列表准确还原修改内容
        createWorkItemActivityIfChanged(oldWorkItem, userId, PmsWorkItemActivityContentEnum.WORK_ITEM_NAME_UPDATED,
                oldWorkItem.getName(), newWorkItem.getName(), oldWorkItem.getName(), newWorkItem.getName());
        if (ObjectUtil.notEqual(oldWorkItem.getDescription(), newWorkItem.getDescription())) {
            createWorkItemActivity(oldWorkItem.getProjectId(), oldWorkItem.getId(), userId,
                    PmsWorkItemActivityContentEnum.WORK_ITEM_DESCRIPTION_UPDATED);
        }
        createWorkItemFieldActivityIfChanged(oldWorkItem, userId, "优先级",
                oldWorkItem.getPriority(), newWorkItem.getPriority(),
                getPriorityName(oldWorkItem.getPriority()), getPriorityName(newWorkItem.getPriority()));
        createWorkItemFieldActivityIfChanged(oldWorkItem, userId, "负责人",
                oldWorkItem.getAssigneeUserId(), newWorkItem.getAssigneeUserId(),
                getActivityValue(oldWorkItem.getAssigneeUserId(), userMap, AdminUserRespDTO::getNickname),
                getActivityValue(newWorkItem.getAssigneeUserId(), userMap, AdminUserRespDTO::getNickname));
        createWorkItemActivityIfChanged(oldWorkItem, userId, PmsWorkItemActivityContentEnum.WORK_ITEM_MEMBERS_UPDATED,
                oldMemberUserIds, newMemberUserIds,
                getActivityValues(oldMemberUserIds, userMap, AdminUserRespDTO::getNickname),
                getActivityValues(newMemberUserIds, userMap, AdminUserRespDTO::getNickname));
        createWorkItemFieldActivityIfChanged(oldWorkItem, userId, "所属迭代",
                oldWorkItem.getIterationId(), newWorkItem.getIterationId(),
                getActivityValue(oldWorkItem.getIterationId(), iterationMap, PmsIterationDO::getName),
                getActivityValue(newWorkItem.getIterationId(), iterationMap, PmsIterationDO::getName));
        createWorkItemActivityIfChanged(oldWorkItem, userId, PmsWorkItemActivityContentEnum.WORK_ITEM_PARENT_UPDATED,
                oldWorkItem.getParentId(), newWorkItem.getParentId(),
                getActivityValue(oldWorkItem.getParentId(), relatedWorkItemMap, PmsWorkItemDO::getName),
                getActivityValue(newWorkItem.getParentId(), relatedWorkItemMap, PmsWorkItemDO::getName));
        createWorkItemFieldActivityIfChanged(oldWorkItem, userId, "关联需求",
                oldWorkItem.getRelatedRequirementId(), newWorkItem.getRelatedRequirementId(),
                getActivityValue(oldWorkItem.getRelatedRequirementId(), relatedWorkItemMap, PmsWorkItemDO::getName),
                getActivityValue(newWorkItem.getRelatedRequirementId(), relatedWorkItemMap, PmsWorkItemDO::getName));
        createWorkItemFieldActivityIfChanged(oldWorkItem, userId, "缺陷类型",
                oldWorkItem.getDefectType(), newWorkItem.getDefectType(),
                getDefectTypeName(oldWorkItem.getDefectType()), getDefectTypeName(newWorkItem.getDefectType()));
        createWorkItemFieldActivityIfChanged(oldWorkItem, userId, "开始时间",
                oldWorkItem.getStartTime(), newWorkItem.getStartTime(),
                formatActivityTime(oldWorkItem.getStartTime()), formatActivityTime(newWorkItem.getStartTime()));
        createWorkItemFieldActivityIfChanged(oldWorkItem, userId, "截止时间",
                oldWorkItem.getEndTime(), newWorkItem.getEndTime(),
                formatActivityTime(oldWorkItem.getEndTime()), formatActivityTime(newWorkItem.getEndTime()));
        createWorkItemFieldActivityIfChanged(oldWorkItem, userId, "预估工时",
                oldWorkItem.getEstimatedHours(), newWorkItem.getEstimatedHours(),
                formatActivityValue(oldWorkItem.getEstimatedHours()), formatActivityValue(newWorkItem.getEstimatedHours()));
        createWorkItemFieldActivityIfChanged(oldWorkItem, userId, "进度",
                oldWorkItem.getProgress(), newWorkItem.getProgress(),
                formatActivityValue(oldWorkItem.getProgress()), formatActivityValue(newWorkItem.getProgress()));
        createWorkItemActivityIfChanged(oldWorkItem, userId, PmsWorkItemActivityContentEnum.WORK_ITEM_LABELS_UPDATED,
                convertSet(oldWorkItem.getLabelIds()), convertSet(newWorkItem.getLabelIds()),
                getActivityValues(oldWorkItem.getLabelIds(), labelMap, PmsWorkItemLabelDO::getName),
                getActivityValues(newWorkItem.getLabelIds(), labelMap, PmsWorkItemLabelDO::getName));
        createWorkItemActivityIfChanged(oldWorkItem, userId, PmsWorkItemActivityContentEnum.WORK_ITEM_ATTACHMENTS_UPDATED,
                oldWorkItem.getFileUrls(), newWorkItem.getFileUrls(),
                getActivityFileNames(oldWorkItem.getFileUrls()), getActivityFileNames(newWorkItem.getFileUrls()));
    }

    @Override
    public void createWorkItemFieldActivityIfChanged(PmsWorkItemDO workItem, Long userId, String fieldName,
                                                     Object oldFieldValue, Object newFieldValue,
                                                     String oldValue, String newValue) {
        if (Objects.equals(oldFieldValue, newFieldValue)) {
            return;
        }
        createWorkItemActivity(workItem.getProjectId(), workItem.getId(), userId,
                PmsWorkItemActivityContentEnum.WORK_ITEM_FIELD_UPDATED, fieldName, oldValue, newValue);
    }

    @Override
    public void createWorkItemIterationActivityIfChanged(PmsWorkItemDO workItem, Long userId,
                                                          Long oldIterationId, Long newIterationId) {
        Map<Long, PmsIterationDO> iterationMap = iterationService.getIterationMap(
                convertSet(Arrays.asList(oldIterationId, newIterationId)));
        createWorkItemFieldActivityIfChanged(workItem, userId, "所属迭代", oldIterationId, newIterationId,
                getActivityValue(oldIterationId, iterationMap, PmsIterationDO::getName),
                getActivityValue(newIterationId, iterationMap, PmsIterationDO::getName));
    }

    private void createWorkItemActivityIfChanged(PmsWorkItemDO workItem, Long userId,
                                                 PmsWorkItemActivityContentEnum content,
                                                 Object oldFieldValue, Object newFieldValue,
                                                 String oldValue, String newValue) {
        if (Objects.equals(oldFieldValue, newFieldValue)) {
            return;
        }
        createWorkItemActivity(workItem.getProjectId(), workItem.getId(), userId, content, oldValue, newValue);
    }

    private static String getPriorityName(Integer priority) {
        PmsWorkItemPriorityEnum priorityEnum = PmsWorkItemPriorityEnum.valueOf(priority);
        return priorityEnum == null ? formatActivityValue(priority) : priorityEnum.getName();
    }

    private static String getDefectTypeName(Integer defectType) {
        PmsWorkItemDefectTypeEnum defectTypeEnum = PmsWorkItemDefectTypeEnum.valueOf(defectType);
        return defectTypeEnum == null ? formatActivityValue(defectType) : defectTypeEnum.getName();
    }

    private static String formatActivityTime(LocalDateTime time) {
        return time == null ? "无" : time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private static String formatActivityValue(Object value) {
        return value == null ? "无" : String.valueOf(value);
    }

    private static <T> String getActivityValue(Long id, Map<Long, T> itemMap, Function<T, String> nameFunction) {
        if (id == null) {
            return "无";
        }
        T item = itemMap.get(id);
        return item == null ? "#" + id : nameFunction.apply(item);
    }

    private static <T> String getActivityValues(Collection<Long> ids, Map<Long, T> itemMap,
                                                Function<T, String> nameFunction) {
        if (CollUtil.isEmpty(ids)) {
            return "无";
        }
        return CollUtil.join(convertList(ids, id -> getActivityValue(id, itemMap, nameFunction)), "、");
    }

    private static String getActivityFileNames(List<String> fileUrls) {
        if (CollUtil.isEmpty(fileUrls)) {
            return "无";
        }
        return CollUtil.join(convertList(fileUrls, fileUrl -> StrUtil.subAfter(fileUrl, '/', true)), "、");
    }

    @Override
    public void createWorkItemActivity(Long projectId, Long workItemId, Long operatorUserId,
                                       PmsWorkItemActivityContentEnum content, Object... arguments) {
        activityMapper.insert(new PmsWorkItemActivityDO().setProjectId(projectId).setWorkItemId(workItemId)
                .setOperatorUserId(operatorUserId).setContent(content.format(arguments)));
    }

    @Override
    public List<PmsWorkItemActivityDO> getWorkItemActivityList(Long workItemId) {
        return activityMapper.selectListByWorkItemId(workItemId);
    }

    @Override
    public List<PmsWorkItemActivityDO> getWorkItemActivityListByWorkItemIds(Collection<Long> workItemIds, int limit) {
        if (CollUtil.isEmpty(workItemIds)) {
            return Collections.emptyList();
        }
        return activityMapper.selectListByWorkItemIds(workItemIds, limit);
    }

    @Override
    public void deleteWorkItemActivityListByWorkItemId(Long workItemId) {
        activityMapper.deleteByWorkItemId(workItemId);
    }

    @Override
    public void deleteWorkItemActivityListByProjectId(Long projectId) {
        activityMapper.deleteByProjectId(projectId);
    }

}
