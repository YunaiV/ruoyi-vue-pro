package cn.iocoder.yudao.module.bpm.framework.bpm.listener;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.api.listener.BpmResultListenerApi;
import cn.iocoder.yudao.module.bpm.api.listener.dto.BpmResultListenerRespDTO;
import cn.iocoder.yudao.module.bpm.framework.bpm.core.event.BpmProcessInstanceResultEvent;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

// TODO @芋艿：后续改成支持 RPC
/**
 * 业务流程结果监听器实现类
 *
 * @author HUIHUI
 */
@Component
public class BpmServiceResultListener implements ApplicationListener<BpmProcessInstanceResultEvent> {

    @Resource
    private List<BpmResultListenerApi> bpmResultListenerApis;

    @Override
    public final void onApplicationEvent(BpmProcessInstanceResultEvent event) {
        bpmResultListenerApis.forEach(bpmResultListenerApi -> {
            if (!StrUtil.equals(event.getProcessDefinitionKey(), bpmResultListenerApi.getProcessDefinitionKey())) {
                return;
            }
            bpmResultListenerApi.onEvent(BeanUtils.toBean(event, BpmResultListenerRespDTO.class));
        });
    }

}
