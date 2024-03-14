package cn.iocoder.yudao.module.bpm.service.task.cc;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyMyPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;
import cn.iocoder.yudao.module.bpm.dal.mysql.cc.BpmProcessInstanceCopyMapper;
import cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.yudao.module.bpm.service.task.BpmTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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
    @Lazy
    private BpmTaskService bpmTaskService;
    @Resource
    @Lazy
    private BpmProcessInstanceService bpmProcessInstanceService;

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
