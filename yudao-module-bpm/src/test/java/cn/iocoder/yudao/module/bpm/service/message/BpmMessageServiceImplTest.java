package cn.iocoder.yudao.module.bpm.service.message;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.framework.web.config.WebProperties;
import cn.iocoder.yudao.module.bpm.enums.message.BpmMessageEnum;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import cn.iocoder.yudao.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import cn.iocoder.yudao.module.system.api.sms.SmsSendApi;
import cn.iocoder.yudao.module.system.api.sms.dto.send.SmsSendSingleToUserReqDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

/**
 * {@link BpmMessageServiceImpl} 的单元测试
 *
 * @author HUIHUI
 */
public class BpmMessageServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private BpmMessageServiceImpl messageService;

    @Mock
    private SmsSendApi smsSendApi;
    @Mock
    private WebProperties webProperties;
    @Mock
    private WebProperties.Ui adminUi;

    @BeforeEach
    public void setUp() {
        org.mockito.Mockito.when(webProperties.getAdminUi()).thenReturn(adminUi);
        org.mockito.Mockito.when(adminUi.getUrl()).thenReturn("http://localhost:48080");
    }

    @Test
    public void testSendMessageWhenProcessInstanceApprove_success() {
        // 准备参数
        BpmMessageSendWhenProcessInstanceApproveReqDTO reqDTO = new BpmMessageSendWhenProcessInstanceApproveReqDTO()
                .setProcessInstanceId("process-instance-1").setProcessInstanceName("请假申请").setStartUserId(1L);

        // 调用
        messageService.sendMessageWhenProcessInstanceApprove(reqDTO);
        // 断言
        SmsSendSingleToUserReqDTO smsReqDTO = captureSmsRequest();
        assertEquals(1L, smsReqDTO.getUserId());
        assertEquals(BpmMessageEnum.PROCESS_INSTANCE_APPROVE.getSmsTemplateCode(), smsReqDTO.getTemplateCode());
        assertEquals("请假申请", smsReqDTO.getTemplateParams().get("processInstanceName"));
        assertEquals("http://localhost:48080/bpm/process-instance/detail?id=process-instance-1",
                smsReqDTO.getTemplateParams().get("detailUrl"));
    }

    @Test
    public void testSendMessageWhenProcessInstanceReject_success() {
        // 准备参数
        BpmMessageSendWhenProcessInstanceRejectReqDTO reqDTO = new BpmMessageSendWhenProcessInstanceRejectReqDTO()
                .setProcessInstanceId("process-instance-1").setProcessInstanceName("请假申请")
                .setStartUserId(1L).setReason("材料不完整");

        // 调用
        messageService.sendMessageWhenProcessInstanceReject(reqDTO);
        // 断言
        SmsSendSingleToUserReqDTO smsReqDTO = captureSmsRequest();
        assertEquals(1L, smsReqDTO.getUserId());
        assertEquals(BpmMessageEnum.PROCESS_INSTANCE_REJECT.getSmsTemplateCode(), smsReqDTO.getTemplateCode());
        assertEquals("材料不完整", smsReqDTO.getTemplateParams().get("reason"));
    }

    private SmsSendSingleToUserReqDTO captureSmsRequest() {
        ArgumentCaptor<SmsSendSingleToUserReqDTO> captor = ArgumentCaptor.forClass(SmsSendSingleToUserReqDTO.class);
        verify(smsSendApi).sendSingleSmsToAdmin(captor.capture());
        return captor.getValue();
    }

}
