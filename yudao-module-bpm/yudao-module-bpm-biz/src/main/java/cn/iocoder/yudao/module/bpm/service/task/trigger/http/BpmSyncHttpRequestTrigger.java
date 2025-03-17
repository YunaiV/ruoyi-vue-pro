package cn.iocoder.yudao.module.bpm.service.task.trigger.http;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * BPM 发送同步 HTTP 请求触发器
 *
 * @author jason
 */
@Component
@Slf4j
public class BpmSyncHttpRequestTrigger extends BpmAbstractHttpRequestTrigger {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTriggerTypeEnum getType() {
        return BpmTriggerTypeEnum.HTTP_REQUEST;
    }

    @Override
    public void execute(String processInstanceId, String param) {
        // 1. 解析 http 请求配置
        HttpRequestTriggerSetting setting = JsonUtils.parseObject(param, HttpRequestTriggerSetting.class);
        if (setting == null) {
            log.error("[execute][流程({}) HTTP 触发器请求配置为空]", processInstanceId);
            return;
        }

        // 2. 发起请求
        ProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        BpmHttpRequestUtils.executeBpmHttpRequest(processInstance,
                setting.getUrl(), setting.getHeader(), setting.getBody(), true, setting.getResponse());
    }

}
