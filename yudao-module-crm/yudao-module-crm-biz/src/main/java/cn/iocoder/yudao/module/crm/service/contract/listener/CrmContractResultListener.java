package cn.iocoder.yudao.module.crm.service.contract.listener;

import cn.iocoder.yudao.module.bpm.api.listener.BpmResultListenerApi;
import cn.iocoder.yudao.module.bpm.api.listener.dto.BpmResultListenerRespDTO;
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
public class CrmContractResultListener implements BpmResultListenerApi {

    @Resource
    private CrmContractService contractService;

    @Override
    public String getProcessDefinitionKey() {
        return CrmContractServiceImpl.CONTRACT_APPROVE;
    }

    @Override
    public void onEvent(BpmResultListenerRespDTO event) {
        contractService.updateContractAuditStatus(event);
    }

}
