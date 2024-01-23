package cn.iocoder.yudao.module.bpm.service.task.cc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyMyPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.cc.BpmProcessInstanceCopyMapper;
import cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfoProcessorChain;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.bpm.service.task.cc.dto.BpmDelegateExecutionDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 流程抄送 Service 实现类
 *
 * @author kyle
 */
@Service
@Validated
@Slf4j
public class BpmProcessInstanceCopyServiceImpl implements BpmProcessInstanceCopyService {

    @Resource
    private BpmProcessInstanceCopyMapper processInstanceCopyMapper;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private BpmCandidateSourceInfoProcessorChain processorChain;

    @Resource
    @Lazy
    private BpmTaskService bpmTaskService;
    @Resource
    @Lazy
    private BpmProcessInstanceService bpmProcessInstanceService;

    @Override
    public boolean makeCopy(BpmCandidateSourceInfo sourceInfo) {
        if (null == sourceInfo) {
            return false;
        }

        Task task = bpmTaskService.getTask(sourceInfo.getTaskId());
        if (ObjectUtil.isNull(task)) {
            return false;
        }
        String processInstanceId = task.getProcessInstanceId();
        if (StrUtil.isBlank(processInstanceId)) {
            return false;
        }
        DelegateExecution executionEntity = new BpmDelegateExecutionDTO(processInstanceId);
        Set<Long> ccCandidates = processorChain.calculateTaskCandidateUsers(executionEntity, sourceInfo);
        if (CollUtil.isEmpty(ccCandidates)) {
            log.warn("相关抄送人不存在 {}", sourceInfo.getTaskId());
            return false;
        } else {
            BpmProcessInstanceCopyDO copyDO = new BpmProcessInstanceCopyDO();
            // 调用
            // 设置任务id
            copyDO.setTaskId(sourceInfo.getTaskId());
            copyDO.setTaskName(task.getName());
            copyDO.setProcessInstanceId(processInstanceId);
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (null == processInstance) {
                log.warn("相关流程实例不存在 {}", sourceInfo.getTaskId());
                return false;
            }
            copyDO.setStartUserId(Long.parseLong(processInstance.getStartUserId()));
            copyDO.setProcessInstanceName(processInstance.getName());
            copyDO.setCategory(processInstance.getProcessDefinitionCategory());
            copyDO.setReason(sourceInfo.getReason());
            copyDO.setCreator(sourceInfo.getCreator());
            copyDO.setCreateTime(LocalDateTime.now());
            List<BpmProcessInstanceCopyDO> copyList = new ArrayList<>(ccCandidates.size());
            for (Long userId : ccCandidates) {
                BpmProcessInstanceCopyDO copy = BeanUtil.copyProperties(copyDO, BpmProcessInstanceCopyDO.class);
                copy.setUserId(userId);
                copyList.add(copy);
            }
            return processInstanceCopyMapper.insertBatch(copyList);
        }
    }

    @Override
    public void createProcessInstanceCopy(Long userId, BpmProcessInstanceCopyCreateReqVO reqVO) {
        // 1.1 校验任务存在
        Task task = bpmTaskService.getTask(reqVO.getTaskId());
        if (ObjectUtil.isNull(task)) {
            throw exception(ErrorCodeConstants.TASK_NOT_EXISTS);
        }
        // 1.2 校验流程存在
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = bpmProcessInstanceService.getProcessInstance(processInstanceId);
        if (processInstance == null) {
            log.warn("[createProcessInstanceCopy][任务({}) 对应的流程不存在]", reqVO.getTaskId());
            throw exception(ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS);
        }

        // 2. 创建抄送流程
        BpmProcessInstanceCopyDO copy = new BpmProcessInstanceCopyDO()
                .setTaskId(reqVO.getTaskId()).setTaskName(task.getName())
                .setProcessInstanceId(processInstanceId).setStartUserId(Long.valueOf(processInstance.getStartUserId()))
                .setProcessInstanceName(processInstance.getName()).setCategory(processInstance.getProcessDefinitionCategory())
                .setReason(reqVO.getReason());
        processInstanceCopyMapper.insert(copy);
    }

    @Override
    public PageResult<BpmProcessInstanceCopyDO> getMyProcessInstanceCopyPage(Long userId, BpmProcessInstanceCopyMyPageReqVO pageReqVO) {
        return processInstanceCopyMapper.selectPage(userId, pageReqVO);
    }

}
