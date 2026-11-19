package cn.iocoder.yudao.module.infra.framework.file.core.client.sftp;

import cn.hutool.extra.ssh.JschRuntimeException;
import cn.hutool.extra.ssh.Sftp;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SftpFileClient} 文件流读写的单元测试：使用同一会话下的独立通道，关闭时断开
 *
 * @author 芋道源码
 */
public class SftpFileClientStreamTest {

    private SftpFileClient client;
    private Sftp sharedSftp;
    private Sftp streamSftp;

    @BeforeEach
    public void setUp() {
        // 不建立真实连接：跳过构造方法，只注入配置和共享会话
        client = mock(SftpFileClient.class, CALLS_REAL_METHODS);
        sharedSftp = mock(Sftp.class);
        streamSftp = mock(Sftp.class);
        SftpFileClientConfig config = new SftpFileClientConfig();
        config.setBasePath("/upload");
        config.setDomain("http://127.0.0.1:48080");
        ReflectionTestUtils.setField(client, "id", 0L);
        ReflectionTestUtils.setField(client, "config", config);
        ReflectionTestUtils.setField(client, "sftp", sharedSftp);
        doReturn(streamSftp).when(client).openStreamSftp();
    }

    @Test
    public void testGetStream_dedicatedChannelClosedOnClose() throws Exception {
        // 准备参数：独立通道返回文件流
        ChannelSftp channel = mock(ChannelSftp.class);
        when(streamSftp.getClient()).thenReturn(channel);
        when(channel.get("/upload" + File.separator + "docs/a.txt")).thenReturn(new ByteArrayInputStream(new byte[]{1, 2}));

        // 调用
        try (InputStream input = client.getStream("docs/a.txt")) {
            assertArrayEquals(new byte[]{1, 2}, cn.hutool.core.io.IoUtil.readBytes(input));
            verify(streamSftp, never()).close();
        }

        // 断言：关闭流时断开独立通道，不使用共享通道读取，也不整文件读取
        verify(streamSftp).close();
        verifyNoInteractions(sharedSftp);
        verify(client, never()).getContent(anyString());
    }

    @Test
    public void testGetStream_closeChannelWhenFileMissing() throws Exception {
        // 准备参数：文件不存在
        ChannelSftp channel = mock(ChannelSftp.class);
        when(streamSftp.getClient()).thenReturn(channel);
        when(channel.get(anyString())).thenThrow(new SftpException(ChannelSftp.SSH_FX_NO_SUCH_FILE, "No such file"));

        // 调用，并断言：抛出异常并断开独立通道
        assertThrows(JschRuntimeException.class, () -> client.getStream("docs/missing.txt"));
        verify(streamSftp).close();
    }

    @Test
    public void testUpload_streamOnDedicatedChannel() {
        // 准备参数
        InputStream content = new ByteArrayInputStream(new byte[]{1, 2, 3});
        String dir = "/upload" + File.separator + "docs/";

        // 调用
        String url = client.upload(content, 3, "docs/a.txt", "text/plain");

        // 断言：创建目录后通过独立通道边读边写，完成后断开，不使用共享通道
        assertEquals("http://127.0.0.1:48080/admin-api/infra/file/0/get/docs/a.txt", url);
        verify(streamSftp).mkDirs(dir);
        verify(streamSftp).upload(dir, "a.txt", content);
        verify(streamSftp).close();
        verifyNoInteractions(sharedSftp);
    }

    @Test
    public void testUpload_closeChannelWhenFailed() {
        // 准备参数：上传中断
        when(streamSftp.upload(anyString(), anyString(), any(InputStream.class)))
                .thenThrow(new JschRuntimeException("Broken pipe"));

        // 调用，并断言：异常向上抛出，独立通道已断开
        assertThrows(JschRuntimeException.class, () -> client.upload(new ByteArrayInputStream(new byte[]{1}),
                1, "docs/a.txt", "text/plain"));
        verify(streamSftp).close();
    }

}
