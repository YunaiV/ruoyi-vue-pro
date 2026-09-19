package cn.iocoder.yudao.module.oa.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskFeedbackReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.task.vo.OaTaskSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskLogDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.task.OaTaskReceiverDO;
import cn.iocoder.yudao.module.oa.dal.mysql.task.OaTaskLogMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.task.OaTaskMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.task.OaTaskReceiverMapper;
import cn.iocoder.yudao.module.oa.enums.task.OaTaskStatusEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.TASK_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.TASK_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.TASK_RECEIVER_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.TASK_RECEIVED_DELETE_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.TASK_STATUS_TRANSITION_INVALID;

/**
 * OA 任务 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaTaskServiceImpl implements OaTaskService {

    @Resource
    private OaTaskMapper taskMapper;
    @Resource
    private OaTaskReceiverMapper taskReceiverMapper;
    @Resource
    private OaTaskLogMapper taskLogMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTask(OaTaskSaveReqVO createReqVO, Long userId) {
        // 1. 校验任务接收人
        adminUserApi.validateUserList(createReqVO.getReceiverUserIds());

        // 2. 新增任务
        OaTaskDO task = BeanUtils.toBean(createReqVO, OaTaskDO.class).setPublishTime(LocalDateTime.now());
        taskMapper.insert(task);

        // 3. 新增任务接收人，初始进度与发布人选择的任务状态一致
        taskReceiverMapper.insertBatch(convertList(createReqVO.getReceiverUserIds(), receiverUserId -> new OaTaskReceiverDO()
                .setTaskId(task.getId()).setUserId(receiverUserId).setStatus(task.getStatus())));
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTask(OaTaskSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验任务属于当前发布人
        OaTaskDO task = validateTaskPublisher(updateReqVO.getId(), userId);
        // 1.2 校验任务接收人
        adminUserApi.validateUserList(updateReqVO.getReceiverUserIds());

        // 2. 按发布人表单更新任务信息及总体状态
        taskMapper.updateById(BeanUtils.toBean(updateReqVO, OaTaskDO.class).setPublishTime(LocalDateTime.now()));

        // 3. 更新任务接收人，移除不再接收任务的关系
        List<OaTaskReceiverDO> existingReceivers = taskReceiverMapper.selectListByTaskId(task.getId());
        List<List<Long>> diff = diffList(convertList(existingReceivers, OaTaskReceiverDO::getUserId),
                updateReqVO.getReceiverUserIds(), ObjUtil::equal);
        if (CollUtil.isNotEmpty(diff.get(2))) {
            taskReceiverMapper.deleteByTaskIdAndUserIds(task.getId(), diff.get(2));
        }
        List<OaTaskReceiverDO> addedReceivers = convertList(diff.get(0),
                receiverUserId -> new OaTaskReceiverDO().setTaskId(task.getId()).setUserId(receiverUserId)
                        .setStatus(updateReqVO.getStatus()));
        if (CollUtil.isNotEmpty(addedReceivers)) {
            taskReceiverMapper.insertBatch(addedReceivers);
        }

        // 4. 表单状态同步覆盖全部当前接收人的进度
        taskReceiverMapper.updateStatusByTaskId(task.getId(), updateReqVO.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id, Long userId) {
        // 1. 校验任务属于当前发布人
        validateTaskPublisher(id, userId);

        // 2. 删除反馈、接收人和任务
        taskLogMapper.deleteByTaskId(id);
        taskReceiverMapper.deleteByTaskId(id);
        taskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReceivedTask(Long id, Long userId) {
        // 1.1 校验当前用户是任务接收人
        OaTaskDO task = validateTaskExists(id);
        validateTaskReceiver(id, userId);
        // 1.2 只有发布人已取消任务后，接收人才可删除自己的接收关系
        if (Boolean.FALSE.equals(task.getCanceled())) {
            throw exception(TASK_RECEIVED_DELETE_DENIED);
        }

        // 2. 仅删除当前用户的接收关系，不影响其他接收人和任务本身
        taskReceiverMapper.deleteByTaskIdAndUserId(id, userId);

        // 3. 根据剩余接收人的最低进度更新任务总体状态
        updateTaskOverallStatus(id);
    }

    @Override
    public OaTaskDO getTask(Long id, Long userId) {
        OaTaskDO task = validateTaskExists(id);
        if (ObjUtil.notEqual(task.getCreator(), userId.toString())
                && taskReceiverMapper.selectByTaskIdAndUserId(id, userId) == null) {
            throw exception(TASK_ACCESS_DENIED);
        }
        return task;
    }

    @Override
    public PageResult<OaTaskDO> getPublishedTaskPage(OaTaskPageReqVO pageReqVO, Long userId) {
        return taskMapper.selectPublishedPage(pageReqVO, userId);
    }

    @Override
    public PageResult<OaTaskDO> getReceivedTaskPage(OaTaskPageReqVO pageReqVO, Long userId) {
        List<OaTaskReceiverDO> receivers = taskReceiverMapper.selectListByUserIdAndStatus(
                userId, pageReqVO.getStatus());
        if (CollUtil.isEmpty(receivers)) {
            return PageResult.empty();
        }
        return taskMapper.selectReceivedPage(pageReqVO,
                convertList(receivers, OaTaskReceiverDO::getTaskId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void feedbackTask(OaTaskFeedbackReqVO createReqVO, Long userId) {
        // 1. 校验任务存在且未取消
        OaTaskDO task = validateTaskExists(createReqVO.getTaskId());
        if (Boolean.TRUE.equals(task.getCanceled())) {
            throw exception(TASK_STATUS_TRANSITION_INVALID);
        }

        // 2. 发布人可调整总体状态；接收人只能在提交前反馈，完成由发布人确认
        if (Boolean.TRUE.equals(createReqVO.getPublisher())) {
            if (ObjUtil.notEqual(task.getCreator(), userId.toString())) {
                throw exception(TASK_ACCESS_DENIED);
            }
            taskMapper.updateById(new OaTaskDO().setId(task.getId()).setStatus(createReqVO.getStatus()));
            taskReceiverMapper.updateStatusByTaskId(task.getId(), createReqVO.getStatus());
        } else {
            OaTaskReceiverDO receiver = validateTaskReceiver(task.getId(), userId);
            if (receiver.getStatus() >= OaTaskStatusEnum.SUBMITTED.getStatus()
                    || createReqVO.getStatus() < OaTaskStatusEnum.NEW.getStatus()
                    || createReqVO.getStatus() > OaTaskStatusEnum.SUBMITTED.getStatus()) {
                throw exception(TASK_STATUS_TRANSITION_INVALID);
            }
            taskReceiverMapper.updateById(receiver.setStatus(createReqVO.getStatus()));
            updateTaskOverallStatus(task.getId());
        }

        // 3. 记录状态变化和反馈内容
        taskLogMapper.insert(BeanUtils.toBean(createReqVO, OaTaskLogDO.class).setUserId(userId));
    }

    @Override
    public List<OaTaskReceiverDO> getTaskReceiverList(Collection<Long> taskIds) {
        if (CollUtil.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        return taskReceiverMapper.selectListByTaskIds(taskIds);
    }

    @Override
    public List<OaTaskLogDO> getTaskLogList(Long taskId) {
        return taskLogMapper.selectListByTaskId(taskId);
    }

    @Override
    public Map<Integer, Long> getTaskStatusCountMap(Long userId) {
        return taskReceiverMapper.selectCountMapByUserIdGroupByStatus(userId);
    }

    @Override
    public Map<Long, Long> getCompletedTaskCountMapByUserId() {
        Map<Long, Long> counts = taskMapper.selectCountMapByStatusGroupByCreator(
                OaTaskStatusEnum.COMPLETED.getStatus());
        Map<Long, Long> countMap = new LinkedHashMap<>();
        counts.entrySet().stream().sorted(Map.Entry.<Long, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(5).forEach(entry -> countMap.put(entry.getKey(), entry.getValue()));
        return countMap;
    }

    /**
     * 根据各接收人的最低进度更新任务总体状态
     *
     * @param taskId 任务编号
     */
    private void updateTaskOverallStatus(Long taskId) {
        List<OaTaskReceiverDO> receivers = taskReceiverMapper.selectListByTaskId(taskId);
        if (CollUtil.isEmpty(receivers)) {
            return;
        }
        Integer overallStatus = Collections.min(convertList(receivers, OaTaskReceiverDO::getStatus));
        taskMapper.updateById(new OaTaskDO().setId(taskId).setStatus(overallStatus));
    }

    /**
     * 校验任务属于指定发布人
     *
     * @param id 任务编号
     * @param userId 发布人用户编号
     * @return 任务
     */
    private OaTaskDO validateTaskPublisher(Long id, Long userId) {
        OaTaskDO task = validateTaskExists(id);
        if (ObjUtil.notEqual(task.getCreator(), userId.toString())) {
            throw exception(TASK_ACCESS_DENIED);
        }
        return task;
    }

    /**
     * 校验当前用户是任务接收人
     *
     * @param taskId 任务编号
     * @param userId 用户编号
     * @return 任务接收人
     */
    private OaTaskReceiverDO validateTaskReceiver(Long taskId, Long userId) {
        OaTaskReceiverDO receiver = taskReceiverMapper.selectByTaskIdAndUserId(taskId, userId);
        if (receiver == null) {
            throw exception(TASK_RECEIVER_NOT_EXISTS);
        }
        return receiver;
    }

    /**
     * 校验任务是否存在
     *
     * @param id 任务编号
     * @return 任务
     */
    private OaTaskDO validateTaskExists(Long id) {
        OaTaskDO task = taskMapper.selectById(id);
        if (task == null) {
            throw exception(TASK_NOT_EXISTS);
        }
        return task;
    }

}
