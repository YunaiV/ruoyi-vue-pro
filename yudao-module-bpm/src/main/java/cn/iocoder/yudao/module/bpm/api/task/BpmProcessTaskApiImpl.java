package cn.iocoder.yudao.module.bpm.api.task;

import cn.iocoder.yudao.module.bpm.service.task.BpmTaskUpdateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 流程任务 Api 实现类
 *
 * @author jason
 */
@Service
@Validated
public class BpmProcessTaskApiImpl implements BpmProcessTaskApi {

    @Resource
    private BpmTaskUpdateService taskUpdateService;

    @Override
    public void triggerTask(String processInstanceId, String taskDefineKey) {
        taskUpdateService.triggerTask(processInstanceId, taskDefineKey);
    }

}
