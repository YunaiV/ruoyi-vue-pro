package cn.iocoder.yudao.module.bpm.api.listener;

import cn.iocoder.yudao.module.bpm.api.listener.dto.BpmResultListenerRespDTO;

// TODO @芋艿：后续改成支持 RPC
/**
 * 业务流程实例的结果发生变化的监听器 Api
 *
 * @author HUIHUI
 */
public interface BpmResultListenerApi {

    /**
     * 监听的流程定义 Key
     *
     * @return 返回监听的流程定义 Key
     */
    String getProcessDefinitionKey();

    /**
     * 处理事件
     *
     * @param event 事件
     */
    void onEvent(BpmResultListenerRespDTO event);

}
