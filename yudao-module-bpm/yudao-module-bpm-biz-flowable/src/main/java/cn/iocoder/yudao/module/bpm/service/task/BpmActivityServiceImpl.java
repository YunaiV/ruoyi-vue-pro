package cn.iocoder.yudao.module.bpm.service.task;

import cn.hutool.core.io.IoUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.activity.BpmActivityRespVO;
import cn.iocoder.yudao.module.bpm.convert.task.BpmActivityConvert;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import lombok.extern.slf4j.Slf4j;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_DEFINITION_BPMN_MODEL_NOT_EXISTS;
import static cn.iocoder.yudao.module.bpm.enums.ErrorCodeConstants.PROCESS_INSTANCE_NOT_EXISTS;

/**
 * BPM 活动实例 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
@Validated
public class BpmActivityServiceImpl implements BpmActivityService {

    @Resource
    private HistoryService historyService;

    @Override
    public List<BpmActivityRespVO> getActivityListByProcessInstanceId(String processInstanceId) {
        List<HistoricActivityInstance> activityList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).list();
        return BpmActivityConvert.INSTANCE.convertList(activityList);
    }



}
