package cn.iocoder.yudao.module.bpm.service.cc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.bpm.convert.cc.BpmProcessInstanceCopyConvert;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.cc.BpmProcessInstanceCopyMapper;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfoProcessorChain;
import cn.iocoder.yudao.module.bpm.service.cc.dto.BpmDelegateExecutionDTO;
import cn.iocoder.yudao.module.bpm.util.FlowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
            //设置任务id
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
            List<BpmProcessInstanceCopyDO> copyList = new ArrayList<>(ccCandidates.size());
            for (Long userId : ccCandidates) {
                BpmProcessInstanceCopyDO copy = BpmProcessInstanceCopyConvert.INSTANCE.copy(copyDO);
                copy.setUserId(userId);
                copyList.add(copy);
            }
            return processInstanceCopyMapper.insertBatch(copyList);
        }
    }

    public List<BpmProcessInstanceCopyVO> queryByProcessId() {
        return null;
    }


}
