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
    private BpmTaskService bpmTaskService;

    @Resource
    @Lazy // 解决循环依赖
    private BpmProcessInstanceService bpmProcessInstanceService;
    @Resource
    private AdminUserApi adminUserApi;

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
    public PageResult<BpmProcessInstanceCopyPageItemRespVO> getMyProcessInstanceCopyPage(Long loginUserId, BpmProcessInstanceCopyMyPageReqVO pageReqVO) {
        // 通过 BpmProcessInstanceExtDO 表，先查询到对应的分页
        // TODO @kyle：一般读逻辑，Service 返回 PageResult<BpmProcessInstanceCopyDO> 即可。关联数据的查询和拼接，交给 Controller；目的是：保证 Service 聚焦写逻辑，清晰简洁；
        PageResult<BpmProcessInstanceCopyDO> pageResult = processInstanceCopyMapper.selectPage(loginUserId, pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(pageResult.getTotal());
        }

        Map<String, String> taskNameByTaskIds = bpmTaskService.getTaskNameByTaskIds(convertSet(pageResult.getList(), BpmProcessInstanceCopyDO::getTaskId));
        Map<String, String> processInstanceNameByProcessInstanceIds = bpmTaskService.getProcessInstanceNameByProcessInstanceIds(convertSet(pageResult.getList(), BpmProcessInstanceCopyDO::getProcessInstanceId));

        Set<Long/* userId */> userIds = new HashSet<>();
        for (BpmProcessInstanceCopyDO doItem : pageResult.getList()) {
            userIds.add(doItem.getStartUserId());
            Long userId = Long.valueOf(doItem.getCreator());
            userIds.add(userId);
        }
        Map<Long, String> userMap = adminUserApi.getUserList(userIds).stream().collect(Collectors.toMap(
                AdminUserRespDTO::getId, AdminUserRespDTO::getNickname));

        // 转换返回
        return BpmProcessInstanceCopyConvert.INSTANCE.convertPage(pageResult, taskNameByTaskIds, processInstanceNameByProcessInstanceIds, userMap);
    }

}
