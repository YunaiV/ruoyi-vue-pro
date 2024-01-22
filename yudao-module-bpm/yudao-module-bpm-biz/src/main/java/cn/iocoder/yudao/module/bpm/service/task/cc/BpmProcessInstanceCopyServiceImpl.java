package cn.iocoder.yudao.module.bpm.service.task.cc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyMyPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyPageItemRespVO;
import cn.iocoder.yudao.module.bpm.convert.cc.BpmProcessInstanceCopyConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.cc.BpmProcessInstanceCopyMapper;
import cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTaskAssignRuleTypeEnum;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfoProcessorChain;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.bpm.service.task.cc.dto.BpmDelegateExecutionDTO;
import cn.iocoder.yudao.module.bpm.util.FlowableUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * Flowable流程抄送实现
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
            copyDO.setTaskName(FlowableUtils.getTaskNameByTaskId(sourceInfo.getTaskId()));
            copyDO.setProcessInstanceId(processInstanceId);
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (null == processInstance) {
                log.warn("相关流程实例不存在 {}", sourceInfo.getTaskId());
                return false;
            }
            copyDO.setStartUserId(FlowableUtils.getStartUserIdFromProcessInstance(processInstance));
            copyDO.setProcessInstanceName(processInstance.getName());
            ProcessDefinition processDefinition = FlowableUtils.getProcessDefinition(processInstance.getProcessDefinitionId());
            copyDO.setCategory(processDefinition.getCategory());
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
    public Void createProcessInstanceCopy(Long userId, BpmProcessInstanceCopyCreateReqVO reqVO) {
        if (!ObjectUtil.equal(reqVO.getType(), BpmTaskAssignRuleTypeEnum.USER.getType())) {
            throw new IllegalArgumentException("业务仅支持USER");
        }
        Task task = bpmTaskService.getTask(reqVO.getTaskId());
        if (ObjectUtil.isNull(task)) {
            throw exception(ErrorCodeConstants.TASK_NOT_EXISTS);
        }
        String processInstanceId = task.getProcessInstanceId();
        if (StrUtil.isBlank(processInstanceId)) {
            throw exception(ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS);
        }
        // 在能正常审批的情况下抄送流程
        BpmProcessInstanceCopyDO copyDO = new BpmProcessInstanceCopyDO();
        // 调用
        // 设置任务id
        copyDO.setTaskId(reqVO.getTaskId());
        copyDO.setTaskName(FlowableUtils.getTaskNameByTaskId(reqVO.getTaskId()));
        copyDO.setProcessInstanceId(processInstanceId);
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (null == processInstance) {
            log.warn("相关流程实例不存在 {}", reqVO.getTaskId());
            throw exception(ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS);
        }
        copyDO.setStartUserId(FlowableUtils.getStartUserIdFromProcessInstance(processInstance));
        copyDO.setProcessInstanceName(processInstance.getName());
        ProcessDefinition processDefinition = FlowableUtils.getProcessDefinition(processInstance.getProcessDefinitionId());
        copyDO.setCategory(processDefinition.getCategory());
        copyDO.setReason(reqVO.getReason());
        copyDO.setCreator(String.valueOf(userId));
        copyDO.setCreateTime(LocalDateTime.now());
        List<BpmProcessInstanceCopyDO> copyList = new ArrayList<>(reqVO.getOptions().size());
        for (Long copyUserId : reqVO.getOptions()) {
            BpmProcessInstanceCopyDO copy = BeanUtil.copyProperties(copyDO, BpmProcessInstanceCopyDO.class);
            copy.setUserId(copyUserId);
            copyList.add(copy);
        }
        processInstanceCopyMapper.insertBatch(copyList);
        return null;
    }

    @Override
    public PageResult<BpmProcessInstanceCopyDO> getMyProcessInstanceCopyPage(Long loginUserId, BpmProcessInstanceCopyMyPageReqVO pageReqVO) {
        // 通过 BpmProcessInstanceExtDO 表，先查询到对应的分页
        return processInstanceCopyMapper.selectPage(loginUserId, pageReqVO);
    }

}
