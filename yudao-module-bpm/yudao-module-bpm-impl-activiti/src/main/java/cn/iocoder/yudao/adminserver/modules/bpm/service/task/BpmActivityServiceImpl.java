package cn.iocoder.yudao.adminserver.modules.bpm.service.task;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.task.BpmActivityConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.bpm.controller.task.vo.activity.BpmActivityRespVO;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.*;

/**
 * BPM 活动实例 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
@Validated
public class BpmActivityServiceImpl implements BpmActivityService {

    private static final String FONT_NAME = "宋体";

    @Resource
    private ProcessDiagramGenerator processDiagramGenerator;
    @Resource
    private HistoryService historyService;

    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmTaskService taskService;

    @Override
    public List<BpmActivityRespVO> getActivityListByProcessInstanceId(String processInstanceId) {
        List<HistoricActivityInstance> activityList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).list();
        return BpmActivityConvert.INSTANCE.convertList(activityList);
    }

    @Override
    public byte[] generateHighlightDiagram(String processInstanceId) {
        // 获得流程实例
        HistoricProcessInstance processInstance = processInstanceService.getHistoricProcessInstance(processInstanceId);
        if (processInstance == null) {
            throw exception(PROCESS_INSTANCE_NOT_EXISTS);
        }
        // 获得流程定义的 BPMN 模型
        BpmnModel bpmnModel = processDefinitionService.getBpmnModel(processInstance.getProcessDefinitionId());
        if (bpmnModel == null) {
            throw exception(PROCESS_DEFINITION_BPMN_MODEL_NOT_EXISTS);
        }

        // 如果流程已经结束，则无进行中的任务，无法高亮
        // 如果流程未结束，才需要高亮
        List<String> highLightedActivities = Collections.emptyList();
        if (processInstance.getEndTime() == null) {
            List<Task> tasks = taskService.getTasksByProcessInstanceId(processInstanceId);
            highLightedActivities = CollectionUtils.convertList(tasks, Task::getTaskDefinitionKey);
        }

        // 生成高亮流程图
        InputStream inputStream = processDiagramGenerator.generateDiagram(bpmnModel, highLightedActivities, Collections.emptyList(),
                FONT_NAME, FONT_NAME, FONT_NAME);
        return IoUtil.readBytes(inputStream);
    }

}
