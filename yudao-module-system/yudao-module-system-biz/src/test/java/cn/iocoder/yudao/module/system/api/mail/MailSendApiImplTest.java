package cn.iocoder.yudao.module.system.api.mail;

import cn.iocoder.yudao.module.system.api.mail.dto.MailSendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.service.mail.MailSendService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailSendApiImplTest {

    @Mock
    private MailSendService mockMailSendService;

    @InjectMocks
    private MailSendApiImpl mailSendApiImplUnderTest;

    @Test
    void testSendSingleMailToAdmin() {
        // Setup
        final MailSendSingleToUserReqDTO reqDTO = new MailSendSingleToUserReqDTO();
        reqDTO.setUserId(0L);
        reqDTO.setMail("mail");
        reqDTO.setTemplateCode("templateCode");
        reqDTO.setTemplateParams(Map.ofEntries(Map.entry("value", "value")));

        when(mockMailSendService.sendSingleMailToAdmin("mail", 0L, "templateCode",
                Map.ofEntries(Map.entry("value", "value")))).thenReturn(0L);

        // Run the test
        final Long result = mailSendApiImplUnderTest.sendSingleMailToAdmin(reqDTO);

        // Verify the results
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void testSendSingleMailToMember() {
        // Setup
        final MailSendSingleToUserReqDTO reqDTO = new MailSendSingleToUserReqDTO();
        reqDTO.setUserId(0L);
        reqDTO.setMail("mail");
        reqDTO.setTemplateCode("templateCode");
        reqDTO.setTemplateParams(Map.ofEntries(Map.entry("value", "value")));

        when(mockMailSendService.sendSingleMailToMember("mail", 0L, "templateCode",
                Map.ofEntries(Map.entry("value", "value")))).thenReturn(0L);

        // Run the test
        final Long result = mailSendApiImplUnderTest.sendSingleMailToMember(reqDTO);

        // Verify the results
        assertThat(result).isEqualTo(0L);
    }
}
