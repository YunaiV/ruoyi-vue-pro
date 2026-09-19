package cn.iocoder.yudao.module.oa.service.officialdoc.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.officialdoc.OaOfficialDocReceiveService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 公文收文审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaOfficialDocReceiveStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaOfficialDocReceiveService officialDocReceiveService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.OFFICIAL_DOC_RECEIVE;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        officialDocReceiveService.updateOfficialDocReceiveStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
