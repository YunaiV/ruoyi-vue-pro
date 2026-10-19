package cn.iocoder.yudao.module.oa.service.officialdoc.listener;

import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import cn.iocoder.yudao.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import cn.iocoder.yudao.module.oa.enums.BpmModelConstants;
import cn.iocoder.yudao.module.oa.service.officialdoc.OaOfficialDocSendService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 公文发文审批结果监听器
 *
 * @author 芋道源码
 */
@Component
public class OaOfficialDocSendStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaOfficialDocSendService officialDocSendService;

    @Override
    protected String getProcessDefinitionKey() {
        return BpmModelConstants.OFFICIAL_DOC_SEND;
    }

    @Override
    protected void onEvent(BpmProcessInstanceStatusEvent event) {
        officialDocSendService.updateOfficialDocSendStatus(Long.parseLong(event.getBusinessKey()), event.getId(), event.getStatus());
    }

}
