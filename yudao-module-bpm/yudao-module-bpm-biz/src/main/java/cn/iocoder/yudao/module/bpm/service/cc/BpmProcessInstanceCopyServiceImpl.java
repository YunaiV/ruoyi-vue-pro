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
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import cn.iocoder.yudao.module.bpm.util.FlowableUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// TODO @kyle：类注释要写下
@Slf4j // TODO @kyle：按照 @Service、@Validated、@Slf4j，从重要到不重要的顺序；
@Service
@Validated
public class BpmProcessInstanceCopyServiceImpl implements BpmProcessInstanceCopyService {
    @Resource // TODO @kyle：第一个变量，和类之间要有空行；
    private BpmProcessInstanceCopyMapper processInstanceCopyMapper;

    /**
     * 和flowable有关的，查询流程名用的 TODO @kyle：可以不写哈注释；
     */
    @Resource
    private RuntimeService runtimeService;

    /**
     * 找抄送人用的 TODO @kyle：可以不写哈注释；
     */
    @Resource
    private BpmCandidateSourceInfoProcessorChain processorChain;

    // TODO @kyle：多余的变量，可以去掉哈
    @Resource
    @Lazy // 解决循环依赖
    private BpmTaskService bpmTaskService;
    @Resource
    @Lazy // 解决循环依赖
    private BpmProcessInstanceService bpmProcessInstanceService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public BpmProcessInstanceCopyVO queryById(Long copyId) {
        BpmProcessInstanceCopyDO bpmProcessInstanceCopyDO = processInstanceCopyMapper.selectById(copyId);
        return BpmProcessInstanceCopyConvert.INSTANCE.convert(bpmProcessInstanceCopyDO);
    }

    // TODO @kyle：makeCopy 和 ccProcessInstance 的调用关系，感受上反了；
    //  makeCopy 有点像基于规则，查找抄送人，然后创建；
    // ccProcessInstance 是已经有了抄送人，然后创建；
    // 建议的改造：独立基于 processInstanceCopyMapper 做 insert
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
            copyDO.setTaskName(FlowableUtils.getTaskNameByTaskId(sourceInfo.getTaskId()));
            copyDO.setProcessInstanceId(sourceInfo.getProcessInstanceId());
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(sourceInfo.getProcessInstanceId())
                    .singleResult();
            if (null == processInstance) {
                log.warn("相关流程实例不存在 {}", sourceInfo.getTaskId());
                return false;
            }
            copyDO.setStartUserId(FlowableUtils.getStartUserIdFromProcessInstance(processInstance));
            copyDO.setProcessInstanceName(processInstance.getName());
            ProcessDefinition processDefinition = FlowableUtils.getProcessDefinition(processInstance.getProcessDefinitionId());
            copyDO.setProcessDefinitionCategory(processDefinition.getCategory());
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
    public boolean ccProcessInstance(Long userId, BpmProcessInstanceCCReqVO reqVO) {
        // 在能正常审批的情况下抄送流程
        BpmCandidateSourceInfo sourceInfo = new BpmCandidateSourceInfo();
        sourceInfo.setTaskId(reqVO.getTaskKey());
        sourceInfo.setProcessInstanceId(reqVO.getProcessInstanceKey());
        sourceInfo.addRule(reqVO);
        sourceInfo.setCreator(String.valueOf(userId));
        sourceInfo.setReason(reqVO.getReason());
        if (!makeCopy(sourceInfo)) {
            throw new RuntimeException("抄送任务失败");
        }
        return false;
    }

    //获取流程抄送分页 TODO @kyle：接口已经注释，这里不用注释了哈；
    @Override
    public PageResult<BpmProcessInstanceCCPageItemRespVO> getMyProcessInstanceCCPage(Long loginUserId, BpmProcessInstanceCCMyPageReqVO pageReqVO) {
        // 通过 BpmProcessInstanceExtDO 表，先查询到对应的分页
        // TODO @kyle：一般读逻辑，Service 返回 PageResult<BpmProcessInstanceCopyDO> 即可。关联数据的查询和拼接，交给 Controller；目的是：保证 Service 聚焦写逻辑，清晰简洁；
        PageResult<BpmProcessInstanceCopyDO> pageResult = processInstanceCopyMapper.selectPage(loginUserId, pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return new PageResult<>(pageResult.getTotal());
        }

        // TODO @kyle：这种可以简洁点；参考如下
//        Map<String, ProcessInstance> processInstanceMap = bpmProcessInstanceService.getProcessInstanceMap(
//                convertSet(pageResult.getList(), BpmProcessInstanceCopyDO::getProcessInstanceId));

        Set<String/* taskId */> taskIds = new HashSet<>();
        Set<String/* processInstaneId */> processInstaneIds = new HashSet<>();
        Set<Long/* userId */> userIds = new HashSet<>();
        for (BpmProcessInstanceCopyDO doItem : pageResult.getList()) {
            taskIds.add(doItem.getTaskId());
            processInstaneIds.add(doItem.getProcessInstanceId());
            userIds.add(doItem.getStartUserId());
            Long userId = Long.valueOf(doItem.getCreator());
            userIds.add(userId);
        }

        Map<String, String> taskNameByTaskIds = FlowableUtils.getTaskNameByTaskIds(taskIds);
        Map<String, String> processInstanceNameByTaskIds = FlowableUtils.getProcessInstanceNameByTaskIds(processInstaneIds);

        Map<Long, String> userMap = adminUserApi.getUserList(userIds).stream().collect(Collectors.toMap(
                AdminUserRespDTO::getId, AdminUserRespDTO::getNickname));

        // 转换返回
        return BpmProcessInstanceCopyConvert.INSTANCE.convertPage(pageResult, taskNameByTaskIds, processInstanceNameByTaskIds, userMap);
    }

}
