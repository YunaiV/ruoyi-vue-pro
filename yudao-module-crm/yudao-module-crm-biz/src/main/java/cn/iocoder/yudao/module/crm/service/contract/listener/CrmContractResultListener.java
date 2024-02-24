package cn.iocoder.yudao.module.crm.service.contract.listener;

import cn.iocoder.yudao.module.bpm.event.BpmProcessInstanceResultEvent;
import cn.iocoder.yudao.module.bpm.event.BpmProcessInstanceResultEventListener;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 合同审批的结果的监听器实现类
 *
 * @author HUIHUI
 */
@Component
public class CrmContractResultListener extends BpmProcessInstanceResultEventListener {

    @Resource
    private CrmContractService contractService;

    @Override
    public String getProcessDefinitionKey() {
        return CrmContractServiceImpl.BPM_PROCESS_DEFINITION_KEY;
    }

    @Override
    protected void onEvent(BpmProcessInstanceResultEvent event) {
        contractService.updateContractAuditStatus(Long.parseLong(event.getBusinessKey()), event.getResult());
    }

}
