package cn.iocoder.yudao.module.bpm.service.task.trigger.http;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmTriggerTypeEnum;
import cn.iocoder.yudao.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

/**
 * BPM HTTP 回调触发器
 *
 * @author jason
 */
@Component
@Slf4j
public class BpmHttpCallbackTrigger extends BpmAbstractHttpRequestTrigger {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTriggerTypeEnum getType() {
        return BpmTriggerTypeEnum.HTTP_CALLBACK;
    }

    @Override
    public void execute(String processInstanceId, String param) {
        // 1. 解析 http 请求配置
        BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting setting = JsonUtils.parseObject(param,
                BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting.class);
        if (setting == null) {
            log.error("[execute][流程({}) HTTP 回调触发器配置为空]", processInstanceId);
            return;
        }

        // 2.1 设置请求头
        ProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        MultiValueMap<String, String> headers = buildHttpHeaders(processInstance, setting.getHeader());
        // 2.2 设置请求体
        MultiValueMap<String, String> body = buildHttpBody(processInstance, setting.getBody());
        // 重要：回调请求 taskDefineKey 需要传给被调用方，用于回调执行
        body.add("taskDefineKey", setting.getCallbackTaskDefineKey());

        // 3. 发起请求
        sendHttpRequest(setting.getUrl(), headers, body);
    }
}
