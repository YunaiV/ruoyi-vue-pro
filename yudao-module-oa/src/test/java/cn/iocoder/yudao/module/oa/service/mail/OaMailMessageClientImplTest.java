package cn.iocoder.yudao.module.oa.service.mail;

import cn.hutool.core.lang.Pair;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import jakarta.mail.Message;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.MAIL_ATTACHMENT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaMailMessageClientImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class OaMailMessageClientImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private OaMailMessageClientImpl mailMessageClient;

    @Mock
    private Message message;

    @Test
    public void testGetAttachment_nestedAndLeadingZeroPath() throws Exception {
        // mock 邮件数据
        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setText("synthetic attachment", "UTF-8");
        attachment.setFileName("sample.txt");
        attachment.setDisposition(Part.ATTACHMENT);
        MimeMultipart inner = new MimeMultipart();
        inner.addBodyPart(attachment);
        MimeBodyPart container = new MimeBodyPart();
        container.setContent(inner);
        MimeMultipart outer = new MimeMultipart();
        outer.addBodyPart(container);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        mimeMessage.setContent(outer);
        mimeMessage.saveChanges();
        // 准备参数
        String path = "00.0";

        // 调用
        Pair<String, byte[]> result = mailMessageClient.getAttachment(mimeMessage, path);
        // 断言
        assertEquals("sample.txt", result.getKey());
        assertArrayEquals("synthetic attachment".getBytes(StandardCharsets.UTF_8), result.getValue());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {".0", "0.", "0..1", "-1", "0 1", "0\n", "١"})
    public void testGetAttachment_invalidPath(String path) {
        // 准备参数：路径不合法时，不应读取邮件内容

        // 调用，并断言异常
        assertServiceException(() -> mailMessageClient.getAttachment(message, path), MAIL_ATTACHMENT_NOT_EXISTS);
        verifyNoInteractions(message);
    }

    @Test
    public void testGetAttachment_longInvalidPath() {
        // 准备参数
        String path = "0.".repeat(10_000);

        // 调用，并断言异常
        assertServiceException(() -> mailMessageClient.getAttachment(message, path), MAIL_ATTACHMENT_NOT_EXISTS);
        verifyNoInteractions(message);
    }

    @Test
    public void testGetAttachment_longValidPath() throws Exception {
        // 准备参数
        String path = "0.".repeat(10_000) + "0";
        // mock message 的方法
        when(message.isMimeType("multipart/*")).thenReturn(false);

        // 调用，并断言异常
        assertServiceException(() -> mailMessageClient.getAttachment(message, path), MAIL_ATTACHMENT_NOT_EXISTS);
        verify(message).isMimeType("multipart/*");
        verifyNoMoreInteractions(message);
    }

    @Test
    public void testGetAttachment_emptyPath() throws Exception {
        // mock 邮件数据
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        mimeMessage.setText("root attachment", "UTF-8");
        mimeMessage.setDisposition(Part.ATTACHMENT);
        mimeMessage.setFileName("root.txt");
        mimeMessage.saveChanges();
        // 准备参数
        String path = "";

        // 调用
        Pair<String, byte[]> result = mailMessageClient.getAttachment(mimeMessage, path);
        // 断言
        assertEquals("root.txt", result.getKey());
        assertArrayEquals("root attachment".getBytes(StandardCharsets.UTF_8), result.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "999999999999999999999"})
    public void testGetAttachment_outOfRangeAndOverflow(String path) throws Exception {
        // mock 邮件数据
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(new MimeBodyPart());
        mimeMessage.setContent(multipart);

        // 调用，并断言异常
        assertServiceException(() -> mailMessageClient.getAttachment(mimeMessage, path), MAIL_ATTACHMENT_NOT_EXISTS);
    }
}
