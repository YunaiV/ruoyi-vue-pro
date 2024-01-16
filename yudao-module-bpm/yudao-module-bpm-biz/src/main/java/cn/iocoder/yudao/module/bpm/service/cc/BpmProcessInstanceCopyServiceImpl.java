package cn.iocoder.yudao.module.bpm.service.cc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCMyPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCPageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCReqVO;
import cn.iocoder.yudao.module.bpm.convert.cc.BpmProcessInstanceCopyConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.cc.BpmProcessInstanceCopyMapper;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfoProcessorChain;
import cn.iocoder.yudao.module.bpm.service.cc.dto.BpmDelegateExecutionDTO;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.bpm.util.FlowableUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
public class BpmProcessInstanceCopyServiceImpl implements BpmProcessInstanceCopyService {
    @Resource
    private BpmProcessInstanceCopyMapper processInstanceCopyMapper;

    /**
     * 和flowable有关的，查询流程名用的
     */
    @Resource
    private RuntimeService runtimeService;

    /**
     * 找抄送人用的
     */
    @Resource
    private BpmCandidateSourceInfoProcessorChain processorChain;

    @Resource
    @Lazy // 解决循环依赖
    private BpmTaskService taskService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public BpmProcessInstanceCopyVO queryById(Long copyId) {
        BpmProcessInstanceCopyDO bpmProcessInstanceCopyDO = processInstanceCopyMapper.selectById(copyId);
        return BpmProcessInstanceCopyConvert.INSTANCE.convert(bpmProcessInstanceCopyDO);
    }

    @Override
    public boolean makeCopy(BpmCandidateSourceInfo sourceInfo) {
        if (null == sourceInfo) {
            return false;
        }

        DelegateExecution executionEntity = new BpmDelegateExecutionDTO(sourceInfo.getProcessInstanceId());
        Set<Long> ccCandidates = processorChain.calculateTaskCandidateUsers(executionEntity, sourceInfo);
        if (CollUtil.isEmpty(ccCandidates)) {
            log.warn("相关抄送人不存在 {}", sourceInfo.getTaskId());
            return false;
        } else {
            BpmProcessInstanceCopyDO copyDO = new BpmProcessInstanceCopyDO();
            // 调用
            // 设置任务id
            copyDO.setTaskId(sourceInfo.getTaskId());
            copyDO.setProcessInstanceId(sourceInfo.getProcessInstanceId());
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(sourceInfo.getProcessInstanceId())
                    .singleResult();
            if (null == processInstance) {
                log.warn("相关流程实例不存在 {}", sourceInfo.getTaskId());
                return false;
            }
            copyDO.setStartUserId(FlowableUtils.getStartUserIdFromProcessInstance(processInstance));
            copyDO.setName(FlowableUtils.getFlowName(processInstance.getProcessDefinitionId()));
            copyDO.setReason(sourceInfo.getReason());
            copyDO.setCreator(sourceInfo.getCreator());
            copyDO.setCreateTime(LocalDateTime.now());
            List<BpmProcessInstanceCopyDO> copyList = new ArrayList<>(ccCandidates.size());
            for (Long userId : ccCandidates) {
                BpmProcessInstanceCopyDO copy = BpmProcessInstanceCopyConvert.INSTANCE.copy(copyDO);
                copy.setUserId(userId);
                copyList.add(copy);
            }
            return processInstanceCopyMapper.insertBatch(copyList);
        }
    }

    @Override
    public boolean ccProcessInstance(Long loginUserId, BpmProcessInstanceCCReqVO reqVO) {
        // 在能正常审批的情况下抄送流程
        BpmCandidateSourceInfo sourceInfo = new BpmCandidateSourceInfo();
        sourceInfo.setTaskId(reqVO.getTaskKey());
        sourceInfo.setProcessInstanceId(reqVO.getProcessInstanceKey());
        sourceInfo.addRule(reqVO);
        sourceInfo.setCreator(String.valueOf(loginUserId));
        sourceInfo.setReason(reqVO.getReason());
        if (!makeCopy(sourceInfo)) {
            throw new RuntimeException("抄送任务失败");
        }
        return false;
    }

    //获取流程抄送分页
    @Override
    public PageResult<BpmProcessInstanceCCPageItemRespVO> getMyProcessInstanceCCPage(Long loginUserId, BpmProcessInstanceCCMyPageReqVO pageReqVO) {
        // 通过 BpmProcessInstanceExtDO 表，先查询到对应的分页
        PageResult<BpmProcessInstanceCopyDO> pageResult = processInstanceCopyMapper.selectPage(loginUserId, pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(pageResult.getTotal());
        }

        Set<String/* taskId */> taskIds = new HashSet<>();
        Set<String/* processInstaneId */> processInstaneIds = new HashSet<>();
        Set<Long/* userId */> userIds = new HashSet<>();
        for (BpmProcessInstanceCopyDO doItem : pageResult.getList()) {
            taskIds.add(doItem.getTaskId());
            processInstaneIds.add(doItem.getProcessInstanceId());
            userIds.add(doItem.getStartUserId());
            Long userId = Long.valueOf(doItem.getCreator());
            userIds.add(userId);
            doItem.setUserId(userId);
        }

        Map<String, String> taskNameByTaskIds = FlowableUtils.getTaskNameByTaskIds(taskIds);
        Map<String, String> processInstanceNameByTaskIds = FlowableUtils.getProcessInstanceNameByTaskIds(processInstaneIds);

        Map<Long, String> userMap = adminUserApi.getUserList(userIds).stream().collect(Collectors.toMap(
                AdminUserRespDTO::getId, AdminUserRespDTO::getNickname));

        // 转换返回
        return BpmProcessInstanceCopyConvert.INSTANCE.convertPage(pageResult, taskNameByTaskIds, processInstanceNameByTaskIds, userMap);
    }

}
