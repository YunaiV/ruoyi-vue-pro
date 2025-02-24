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
 * BPM 发送异步 HTTP 请求触发器
 *
 * @author jason
 */
@Component
@Slf4j
public class BpmAsyncHttpRequestTrigger extends BpmAbstractHttpRequestTrigger {

    @Resource
    private BpmProcessInstanceService processInstanceService;

    @Override
    public BpmTriggerTypeEnum getType() {
        return BpmTriggerTypeEnum.ASYNC_HTTP_REQUEST;
    }

    @Override
    public void execute(String processInstanceId, String param) {
        // 1. 解析 http 请求配置
        BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting setting = JsonUtils.parseObject(param,
                BpmSimpleModelNodeVO.TriggerSetting.HttpRequestTriggerSetting.class);
        if (setting == null) {
            log.error("[execute][流程({}) HTTP 异步触发器请求配置为空]", processInstanceId);
            return;
        }

        // 2.1 设置请求头
        ProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        MultiValueMap<String, String> headers = buildHttpHeaders(processInstance, setting.getHeader());
        // 2.2 设置请求体
        MultiValueMap<String, String> body = buildHttpBody(processInstance, setting.getBody());
        // TODO @芋艿：【异步】在看看
        body.add("callbackId", setting.getCallbackId()); // 异步请求 callbackId 需要传给被调用方，用于回调执行

        // 3. 发起请求
        sendHttpRequest(setting.getUrl(), headers, body);
    }
}
