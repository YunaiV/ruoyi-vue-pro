package cn.iocoder.yudao.module.crm.service.contract.listener;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.bpm.api.listener.BpmResultListenerApi;
import cn.iocoder.yudao.module.bpm.api.listener.dto.BpmResultListenerRespDTO;
import cn.iocoder.yudao.module.bpm.enums.task.BpmProcessInstanceResultEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
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
        boolean currentTaskFinish = isEndResult(event.getResult());
        if (!currentTaskFinish) {
            return;
        }
        if (ObjUtil.equal(event.getResult(), BpmProcessInstanceResultEnum.APPROVE.getResult())) {
            event.setResult(CrmAuditStatusEnum.APPROVE.getStatus());
        }
        if (ObjUtil.equal(event.getResult(), BpmProcessInstanceResultEnum.REJECT.getResult())) {
            event.setResult(CrmAuditStatusEnum.REJECT.getStatus());
        }
        if (ObjUtil.equal(event.getResult(), BpmProcessInstanceResultEnum.CANCEL.getResult())) {
            event.setResult(CrmAuditStatusEnum.CANCEL.getStatus());
        }
        contractService.updateContractAuditStatus(event);
    }

    /**
     * 判断该结果是否处于 End 最终结果
     *
     * @param result 结果
     * @return 是否
     */
    public static boolean isEndResult(Integer result) {
        return ObjectUtils.equalsAny(result, BpmProcessInstanceResultEnum.APPROVE.getResult(),
                BpmProcessInstanceResultEnum.REJECT.getResult(), BpmProcessInstanceResultEnum.CANCEL.getResult());
    }

}
