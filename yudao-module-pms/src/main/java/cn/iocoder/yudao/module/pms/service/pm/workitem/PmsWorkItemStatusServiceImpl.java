package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemBoardConfigSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusConfigUpdateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusCreateReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusDeleteReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.status.PmsWorkItemStatusSortReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemBoardDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemStatusDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemStatusMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemStatusTypeEnum;
import cn.iocoder.yudao.module.pms.enums.pm.workitem.PmsWorkItemTypeEnum;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_STATUS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_DEFAULT_CANNOT_DELETE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_SORT_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_STATUS_TRANSFER_INVALID;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.WORK_ITEM_TYPE_INVALID;

/**
 * PMS 工作项看板状态 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsWorkItemStatusServiceImpl implements PmsWorkItemStatusService {

    @Resource
    private PmsWorkItemStatusMapper workItemStatusMapper;

    @Resource
    private PmsWorkItemBoardService workItemBoardService;
    @Lazy // 延迟加载，避免循环依赖
    @Resource
    private PmsWorkItemService workItemService;
    @Resource
    private PmsProjectMemberService projectMemberService;

    @Override
    public Long createWorkItemStatus(PmsWorkItemStatusCreateReqVO createReqVO, Long userId) {
        // 1.1 校验项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(createReqVO.getProjectId(), userId);
        // 1.2 校验项目处于进行中
        validateActiveProject(project);
        // 1.3 校验工作项类型可用
        validateWorkItemType(project, createReqVO.getWorkItemType());
        List<PmsWorkItemStatusDO> statuses = getWorkItemStatusList(project.getId(), createReqVO.getWorkItemType());
        if (CollUtil.isEmpty(statuses)) {
            throw exception(WORK_ITEM_STATUS_NOT_EXISTS);
        }
        String name = createReqVO.getName();
        // 1.4 校验状态名称唯一
        validateStatusNameUnique(null, project.getId(), createReqVO.getWorkItemType(), name);

        // 2. 追加自定义状态
        PmsWorkItemStatusDO status = BeanUtils.toBean(createReqVO, PmsWorkItemStatusDO.class)
                .setProjectId(project.getId()).setName(name)
                .setBoardName(null)
                .setDefaultStatus(false).setSort(CollUtil.getLast(statuses).getSort() + 1);
        workItemStatusMapper.insert(status);
        return status.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemStatusConfig(PmsWorkItemStatusConfigUpdateReqVO updateReqVO, Long userId) {
        // 1.1 校验状态存在
        PmsWorkItemStatusDO status = getWorkItemStatus(updateReqVO.getId());
        // 1.2 校验状态所属项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(status.getProjectId(), userId);
        // 1.3 校验项目处于进行中
        validateActiveProject(project);
        String name = updateReqVO.getName();
        // 1.4 校验状态名称唯一
        validateStatusNameUnique(status.getId(), status.getProjectId(), status.getWorkItemType(), name);

        // 2. 更新状态及其全部工作项的语义状态
        workItemStatusMapper.updateById(BeanUtils.toBean(updateReqVO, PmsWorkItemStatusDO.class)
                .setId(status.getId()).setName(name).setBoardName(status.getBoardName()));
        workItemService.updateWorkItemStatusTypeByStatusId(status.getId(), updateReqVO.getStatusType());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefaultWorkItemStatus(Long id, Long userId) {
        // 1.1 校验状态存在
        PmsWorkItemStatusDO status = getWorkItemStatus(id);
        // 1.2 校验状态所属项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(status.getProjectId(), userId);
        // 1.3 校验项目处于进行中
        validateActiveProject(project);

        // 2. 切换当前工作项类型的初始状态
        workItemStatusMapper.updateDefaultStatusByProjectIdAndWorkItemType(
                status.getProjectId(), status.getWorkItemType(), id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemStatusSort(PmsWorkItemStatusSortReqVO sortReqVO, Long userId) {
        // 1.1 校验排序列表中的状态无重复且全部存在
        List<Long> statusIds = sortReqVO.getStatusIds();
        List<PmsWorkItemStatusDO> statuses = workItemStatusMapper.selectByIds(statusIds);
        if (statuses.size() != statusIds.size()) {
            throw exception(WORK_ITEM_STATUS_SORT_INVALID);
        }
        PmsWorkItemStatusDO firstStatus = CollUtil.getFirst(statuses);
        // 1.2 校验状态所属项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(firstStatus.getProjectId(), userId);
        // 1.3 校验项目处于进行中
        validateActiveProject(project);
        // 1.4 校验排序列表完整且属于同一项目和工作项类型
        List<PmsWorkItemStatusDO> allStatuses = workItemStatusMapper.selectListByProjectIdAndWorkItemType(
                firstStatus.getProjectId(), firstStatus.getWorkItemType());
        if (ObjectUtil.notEqual(convertSet(statusIds), convertSet(allStatuses, PmsWorkItemStatusDO::getId))) {
            throw exception(WORK_ITEM_STATUS_SORT_INVALID);
        }

        // 2. 按状态编号稳定加锁并写入请求顺序
        Map<Long, Integer> sortMap = new LinkedHashMap<>();
        for (int index = 0; index < sortReqVO.getStatusIds().size(); index++) {
            sortMap.put(sortReqVO.getStatusIds().get(index), index + 1);
        }
        List<Long> sortedStatusIds = new ArrayList<>(statusIds);
        Collections.sort(sortedStatusIds);
        workItemStatusMapper.updateBatch(convertList(sortedStatusIds,
                statusId -> new PmsWorkItemStatusDO().setId(statusId).setSort(sortMap.get(statusId))));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWorkItemBoardConfig(PmsWorkItemBoardConfigSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验项目和工作项类型可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(saveReqVO.getProjectId(), userId);
        validateActiveProject(project);
        validateWorkItemType(project, saveReqVO.getWorkItemType());
        // 1.2 校验看板列名称和状态映射不重复，且状态全部属于当前项目和工作项类型
        Set<String> boardNames = new LinkedHashSet<>();
        Set<Long> statusIds = new LinkedHashSet<>();
        for (PmsWorkItemBoardConfigSaveReqVO.Board board : saveReqVO.getBoards()) {
            if (!boardNames.add(board.getName())) {
                throw exception(WORK_ITEM_STATUS_SORT_INVALID);
            }
            for (Long statusId : board.getStatusIds()) {
                if (!statusIds.add(statusId)) {
                    throw exception(WORK_ITEM_STATUS_SORT_INVALID);
                }
            }
        }
        Map<Long, PmsWorkItemStatusDO> statusMap = CollUtil.isEmpty(statusIds) ? Collections.emptyMap()
                : convertMap(workItemStatusMapper.selectByIds(statusIds), PmsWorkItemStatusDO::getId);
        if (statusMap.size() != statusIds.size() || statusMap.values().stream().anyMatch(status ->
                ObjectUtil.notEqual(status.getProjectId(), project.getId())
                        || ObjectUtil.notEqual(status.getWorkItemType(), saveReqVO.getWorkItemType()))) {
            throw exception(WORK_ITEM_STATUS_SORT_INVALID);
        }

        // 2.1 读取原看板列，并校验请求中的列编号属于当前项目和工作项类型
        List<PmsWorkItemBoardDO> oldBoards = workItemBoardService
                .getWorkItemBoardList(project.getId(), saveReqVO.getWorkItemType());
        Set<Long> oldBoardIds = convertSet(oldBoards, PmsWorkItemBoardDO::getId);
        Set<Long> newBoardIds = convertSet(saveReqVO.getBoards(), PmsWorkItemBoardConfigSaveReqVO.Board::getId);
        if (newBoardIds.size() != saveReqVO.getBoards().stream().filter(board -> board.getId() != null).count()
                || !oldBoardIds.containsAll(newBoardIds)) {
            throw exception(WORK_ITEM_STATUS_SORT_INVALID);
        }

        // 2.2 对比看板列，保留已有编号，仅增删改实际发生变化的列
        List<PmsWorkItemBoardDO> newBoards = convertList(saveReqVO.getBoards(), board ->
                new PmsWorkItemBoardDO().setId(board.getId()).setProjectId(project.getId())
                        .setWorkItemType(saveReqVO.getWorkItemType()).setName(board.getName())
                        .setSort(saveReqVO.getBoards().indexOf(board) + 1));
        List<List<PmsWorkItemBoardDO>> boardDiff = diffList(oldBoards, newBoards,
                (oldBoard, newBoard) -> oldBoard.getId() != null && oldBoard.getId().equals(newBoard.getId()));
        if (CollUtil.isNotEmpty(boardDiff.get(0))) {
            workItemBoardService.createWorkItemBoardList(boardDiff.get(0));
        }
        if (CollUtil.isNotEmpty(boardDiff.get(1))) {
            workItemBoardService.updateWorkItemBoardList(boardDiff.get(1));
        }
        if (CollUtil.isNotEmpty(boardDiff.get(2))) {
            workItemBoardService.deleteWorkItemBoardList(convertList(boardDiff.get(2), PmsWorkItemBoardDO::getId));
        }

        // 2.3 先清除当前类型的旧状态映射，再按请求批量写入新映射
        workItemStatusMapper.updateBoardNameByProjectIdAndWorkItemType(
                project.getId(), saveReqVO.getWorkItemType(), null);
        for (PmsWorkItemBoardConfigSaveReqVO.Board board : saveReqVO.getBoards()) {
            if (CollUtil.isNotEmpty(board.getStatusIds())) {
                workItemStatusMapper.updateBoardNameByIds(board.getStatusIds(), board.getName());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkItemStatus(PmsWorkItemStatusDeleteReqVO deleteReqVO, Long userId) {
        // 1.1 校验待删除状态存在
        PmsWorkItemStatusDO sourceStatus = getWorkItemStatus(deleteReqVO.getId());
        // 1.2 校验状态所属项目可编辑
        PmsProjectDO project = projectMemberService.validateProjectWritable(sourceStatus.getProjectId(), userId);
        // 1.3 校验项目处于进行中
        validateActiveProject(project);
        // 1.4 校验默认状态不能删除
        if (sourceStatus.getDefaultStatus()) {
            throw exception(WORK_ITEM_STATUS_DEFAULT_CANNOT_DELETE);
        }

        // 2. 存在工作项时迁移到同项目同类型的目标状态
        Long workItemCount = workItemService.getWorkItemCountByStatusId(sourceStatus.getId());
        if (workItemCount > 0 || deleteReqVO.getTransferStatusId() != null) {
            if (deleteReqVO.getTransferStatusId() == null
                    || ObjectUtil.equal(sourceStatus.getId(), deleteReqVO.getTransferStatusId())) {
                throw exception(WORK_ITEM_STATUS_TRANSFER_INVALID);
            }
            PmsWorkItemStatusDO targetStatus = validateWorkItemStatus(
                    deleteReqVO.getTransferStatusId(), sourceStatus.getProjectId(), sourceStatus.getWorkItemType());
            workItemService.transferWorkItemStatus(
                    sourceStatus.getId(), targetStatus.getId(), targetStatus.getStatusType());
        }

        // 3. 删除空状态
        workItemStatusMapper.deleteById(sourceStatus.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initProjectWorkItemStatuses(Long projectId, Integer projectType) {
        // 1. 通用项目初始化任务状态和看板列
        Integer taskType = PmsWorkItemTypeEnum.TASK.getType();
        initWorkItemStatusList(projectId, taskType);
        initWorkItemBoardList(projectId, taskType);
        if (PmsProjectTypeEnum.GENERAL.getType().equals(projectType)) {
            return;
        }

        // 2. 敏捷开发项目同时初始化需求、缺陷状态和对应看板列
        Integer requirementType = PmsWorkItemTypeEnum.REQUIREMENT.getType();
        initWorkItemStatusList(projectId, requirementType);
        initWorkItemBoardList(projectId, requirementType);
        Integer defectType = PmsWorkItemTypeEnum.DEFECT.getType();
        initWorkItemStatusList(projectId, defectType);
        initWorkItemBoardList(projectId, defectType);
    }

    @Override
    public PmsWorkItemStatusDO getWorkItemStatus(Long id) {
        PmsWorkItemStatusDO status = workItemStatusMapper.selectById(id);
        if (status == null) {
            throw exception(WORK_ITEM_STATUS_NOT_EXISTS);
        }
        return status;
    }

    @Override
    public PmsWorkItemStatusDO getWorkItemStatus(Long id, Long userId) {
        // 1. 查询工作项状态
        PmsWorkItemStatusDO status = getWorkItemStatus(id);
        // 2. 校验项目可读
        projectMemberService.validateProjectReadable(status.getProjectId(), userId);
        return status;
    }

    private List<PmsWorkItemStatusDO> initWorkItemStatusList(Long projectId, Integer workItemType) {
        // 1. 查询项目工作项状态和已有系统状态编码
        List<PmsWorkItemStatusDO> statuses = workItemStatusMapper
                .selectListByProjectIdAndWorkItemType(projectId, workItemType);
        List<PmsWorkItemStatusTypeEnum> defaultStatuses = Arrays.asList(PmsWorkItemStatusTypeEnum.PENDING,
                PmsWorkItemStatusTypeEnum.PROCESSING, PmsWorkItemStatusTypeEnum.COMPLETED);
        Set<String> existingSystemCodes = convertSet(statuses, PmsWorkItemStatusDO::getSystemCode);
        if (existingSystemCodes.containsAll(convertSet(defaultStatuses, Enum::name))) {
            return statuses;
        }

        // 2. 补充缺失的系统状态，并重新查询
        initMissingSystemWorkItemStatuses(projectId, workItemType, statuses, defaultStatuses, existingSystemCodes);
        return workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId, workItemType);
    }

    private void initWorkItemBoardList(Long projectId, Integer workItemType) {
        // 1. 项目创建时已完成初始化，重复初始化直接结束
        List<PmsWorkItemBoardDO> boards = workItemBoardService.getWorkItemBoardList(projectId, workItemType);
        if (CollUtil.isNotEmpty(boards)) {
            return;
        }

        // 2. 按初始化后的状态所属列创建独立看板列
        Set<String> boardNames = new LinkedHashSet<>();
        for (PmsWorkItemStatusDO status : getWorkItemStatusList(projectId, workItemType)) {
            if (StrUtil.isBlank(status.getBoardName())
                    || !boardNames.add(status.getBoardName())) {
                continue;
            }
            workItemBoardService.createWorkItemBoard(new PmsWorkItemBoardDO().setProjectId(projectId)
                    .setWorkItemType(workItemType).setName(status.getBoardName()).setSort(boardNames.size()));
        }
    }

    @Override
    public List<PmsWorkItemStatusDO> getWorkItemStatusList(Long projectId, Integer workItemType) {
        return workItemStatusMapper.selectListByProjectIdAndWorkItemType(projectId, workItemType);
    }

    @Override
    public List<PmsWorkItemBoardDO> getWorkItemBoardList(Long projectId, Integer workItemType) {
        return workItemBoardService.getWorkItemBoardList(projectId, workItemType);
    }

    /**
     * 初始化缺失的未开始、进行中和已完成系统状态
     *
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @param statuses 已有状态列表
     * @param defaultStatuses 系统状态类型列表
     * @param existingSystemCodes 已有系统状态编码集合
     */
    private void initMissingSystemWorkItemStatuses(Long projectId, Integer workItemType,
                                                    List<PmsWorkItemStatusDO> statuses,
                                                    List<PmsWorkItemStatusTypeEnum> defaultStatuses,
                                                    Set<String> existingSystemCodes) {
        boolean hasDefaultStatus = CollUtil.findOne(
                statuses, status -> Boolean.TRUE.equals(status.getDefaultStatus())) != null;
        for (int index = 0; index < defaultStatuses.size(); index++) {
            PmsWorkItemStatusTypeEnum statusType = defaultStatuses.get(index);
            if (existingSystemCodes.contains(statusType.name())) {
                continue;
            }
            try {
                workItemStatusMapper.insert(new PmsWorkItemStatusDO().setProjectId(projectId)
                        .setWorkItemType(workItemType).setName(statusType.getName())
                        .setStatusType(statusType.getType()).setSystemCode(statusType.name())
                        .setBoardName(statusType.getName())
                        .setDefaultStatus(!hasDefaultStatus && PmsWorkItemStatusTypeEnum.PENDING.equals(statusType))
                        .setSort(index + 1));
                if (PmsWorkItemStatusTypeEnum.PENDING.equals(statusType)) {
                    hasDefaultStatus = true;
                }
            } catch (DuplicateKeyException ignored) {
                // 其他请求已完成同一默认状态的初始化，继续查询即可
            }
        }
    }

    @Override
    public PmsWorkItemStatusDO getDefaultWorkItemStatus(Long projectId, Integer workItemType) {
        PmsWorkItemStatusDO status = workItemStatusMapper
                .selectDefaultByProjectIdAndWorkItemType(projectId, workItemType);
        if (status == null) {
            throw exception(WORK_ITEM_STATUS_NOT_EXISTS);
        }
        return status;
    }

    @Override
    public List<PmsWorkItemStatusDO> getWorkItemStatusList(Long projectId, Integer workItemType, Long userId) {
        // 1. 校验当前用户可以访问项目
        projectMemberService.validateProjectReadable(projectId, userId);
        // 2. 查询已保存的工作项状态列表，不在读取接口初始化数据
        return getWorkItemStatusList(projectId, workItemType);
    }

    @Override
    public PmsWorkItemStatusDO validateWorkItemStatus(Long id, Long projectId, Integer workItemType) {
        PmsWorkItemStatusDO status = getWorkItemStatus(id);
        if (ObjectUtil.notEqual(projectId, status.getProjectId())
                || ObjectUtil.notEqual(workItemType, status.getWorkItemType())) {
            throw exception(WORK_ITEM_STATUS_INVALID);
        }
        return status;
    }

    @Override
    public Map<Long, PmsWorkItemStatusDO> getWorkItemStatusMap(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return convertMap(workItemStatusMapper.selectByIds(ids), PmsWorkItemStatusDO::getId);
    }

    @Override
    public void deleteWorkItemStatusListByProjectId(Long projectId) {
        workItemBoardService.deleteWorkItemBoardListByProjectId(projectId);
        workItemStatusMapper.deleteByProjectId(projectId);
    }

    /**
     * 校验项目处于进行中
     *
     * @param project 项目
     */
    private void validateActiveProject(PmsProjectDO project) {
        if (ObjectUtil.notEqual(PmsProjectStatusEnum.ACTIVE.getStatus(), project.getStatus())) {
            throw exception(PROJECT_STATUS_INVALID);
        }
    }

    /**
     * 校验项目支持工作项类型
     *
     * @param project 项目
     * @param workItemType 工作项类型
     */
    private void validateWorkItemType(PmsProjectDO project, Integer workItemType) {
        boolean valid = PmsProjectTypeEnum.GENERAL.getType().equals(project.getType())
                ? PmsWorkItemTypeEnum.TASK.getType().equals(workItemType)
                : PmsWorkItemTypeEnum.valueOf(workItemType) != null;
        if (!valid) {
            throw exception(WORK_ITEM_TYPE_INVALID);
        }
    }

    /**
     * 校验同一项目和工作项类型下状态名称唯一
     *
     * @param id 状态编号
     * @param projectId 项目编号
     * @param workItemType 工作项类型
     * @param name 状态名称
     */
    private void validateStatusNameUnique(Long id, Long projectId, Integer workItemType, String name) {
        PmsWorkItemStatusDO status = workItemStatusMapper
                .selectByProjectIdAndWorkItemTypeAndName(projectId, workItemType, name);
        if (status != null && ObjectUtil.notEqual(status.getId(), id)) {
            throw exception(WORK_ITEM_STATUS_NAME_DUPLICATE);
        }
    }

}
