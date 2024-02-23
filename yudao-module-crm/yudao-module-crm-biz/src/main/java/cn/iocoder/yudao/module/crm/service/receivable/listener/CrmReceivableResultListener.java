package cn.iocoder.yudao.module.crm.service.receivable.listener;

import cn.iocoder.yudao.module.bpm.api.listener.BpmResultListenerApi;
import cn.iocoder.yudao.module.bpm.api.listener.dto.BpmResultListenerRespDTO;
import cn.iocoder.yudao.module.crm.service.receivable.CrmReceivableService;
import cn.iocoder.yudao.module.crm.service.receivable.CrmReceivableServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

// TODO @芋艿：后续改成支持 RPC

/**
 * 回款审批的结果的监听器实现类
 *
 * @author HUIHUI
 */
@Component
public class CrmReceivableResultListener implements BpmResultListenerApi {

    @Resource
    private CrmReceivableService receivableService;

    @Override
    public String getProcessDefinitionKey() {
        return CrmReceivableServiceImpl.RECEIVABLE_APPROVE;
    }

    @Override
    public void onEvent(BpmResultListenerRespDTO event) {
        receivableService.updateReceivableAuditStatus(event);
    }

}
