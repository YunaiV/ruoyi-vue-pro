package cn.iocoder.yudao.module.infra.framework.file.core.client.ftp;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link FtpFileClient} 文件流读写的单元测试：使用独立连接，关闭时释放
 *
 * @author 芋道源码
 */
public class FtpFileClientStreamTest {

    private FtpFileClient client;
    private Ftp sharedFtp;

    @BeforeEach
    public void setUp() {
        // 不建立真实连接：跳过构造方法，只注入配置和共享连接
        client = mock(FtpFileClient.class, CALLS_REAL_METHODS);
        sharedFtp = mock(Ftp.class);
        FtpFileClientConfig config = new FtpFileClientConfig();
        config.setBasePath("/home/ftp");
        config.setDomain("http://127.0.0.1:48080");
        ReflectionTestUtils.setField(client, "id", 0L);
        ReflectionTestUtils.setField(client, "config", config);
        ReflectionTestUtils.setField(client, "ftp", sharedFtp);
    }

    @Test
    public void testGetStream_dedicatedConnectionReleasedOnClose() throws Exception {
        // 准备参数：独立连接返回数据流
        Ftp streamFtp = mock(Ftp.class);
        FTPClient ftpClient = mock(FTPClient.class);
        doReturn(streamFtp).when(client).createFtp();
        when(streamFtp.cd("/home/ftp/docs/")).thenReturn(true);
        when(streamFtp.getClient()).thenReturn(ftpClient);
        when(ftpClient.retrieveFileStream("a.txt")).thenReturn(new ByteArrayInputStream(new byte[]{1, 2}));

        // 调用
        try (InputStream input = client.getStream("docs/a.txt")) {
            assertArrayEquals(new byte[]{1, 2}, input.readAllBytes());
            verify(streamFtp, never()).close();
        }

        // 断言：关闭流时结束传输并释放独立连接，不占用共享连接，也不整文件读取
        verify(ftpClient).setFileType(FTPClient.BINARY_FILE_TYPE);
        verify(ftpClient).completePendingCommand();
        verify(streamFtp).close();
        verifyNoInteractions(sharedFtp);
        verify(client, never()).getContent(anyString());
    }

    @Test
    public void testGetStream_returnNullWhenFileMissing() throws Exception {
        // 准备参数：文件不存在时数据流为空，服务端回复 550
        Ftp streamFtp = mock(Ftp.class);
        FTPClient ftpClient = mock(FTPClient.class);
        doReturn(streamFtp).when(client).createFtp();
        when(streamFtp.getClient()).thenReturn(ftpClient);
        when(streamFtp.cd("/home/ftp/docs/")).thenReturn(true);
        when(ftpClient.retrieveFileStream("missing.txt")).thenReturn(null);
        when(ftpClient.getReplyCode()).thenReturn(FTPReply.FILE_UNAVAILABLE);

        // 调用，并断言：返回 null 交给调用方按 404 处理，独立连接已释放，不使用共享连接
        assertNull(client.getStream("docs/missing.txt"));
        verify(streamFtp).close();
        verifyNoInteractions(sharedFtp);
    }

    @Test
    public void testGetStream_returnNullWhenDirectoryMissing() throws Exception {
        // 准备参数：目录不存在，切换目录失败且服务端回复 550
        Ftp streamFtp = mock(Ftp.class);
        FTPClient ftpClient = mock(FTPClient.class);
        doReturn(streamFtp).when(client).createFtp();
        when(streamFtp.getClient()).thenReturn(ftpClient);
        when(streamFtp.cd("/home/ftp/missing/")).thenReturn(false);
        when(ftpClient.getReplyCode()).thenReturn(FTPReply.FILE_UNAVAILABLE);

        // 调用，并断言：返回 null，不再读取文件，独立连接已释放
        assertNull(client.getStream("missing/a.txt"));
        verify(ftpClient, never()).retrieveFileStream(anyString());
        verify(streamFtp).close();
    }

    @Test
    public void testGetStream_throwWhenRetrieveFailsForOtherReason() throws Exception {
        // 准备参数：数据连接无法建立（425），不属于文件不存在
        Ftp streamFtp = mock(Ftp.class);
        FTPClient ftpClient = mock(FTPClient.class);
        doReturn(streamFtp).when(client).createFtp();
        when(streamFtp.getClient()).thenReturn(ftpClient);
        when(streamFtp.cd("/home/ftp/docs/")).thenReturn(true);
        when(ftpClient.retrieveFileStream("a.txt")).thenReturn(null);
        when(ftpClient.getReplyCode()).thenReturn(FTPReply.CANNOT_OPEN_DATA_CONNECTION);

        // 调用，并断言：仍按读取失败抛出，独立连接已释放
        assertThrows(FtpException.class, () -> client.getStream("docs/a.txt"));
        verify(streamFtp).close();
    }

    @Test
    public void testGetStream_throwWhenChangeDirectoryFailsForOtherReason() throws Exception {
        // 准备参数：切换目录因未登录（530）失败，不属于目录不存在
        Ftp streamFtp = mock(Ftp.class);
        FTPClient ftpClient = mock(FTPClient.class);
        doReturn(streamFtp).when(client).createFtp();
        when(streamFtp.getClient()).thenReturn(ftpClient);
        when(streamFtp.cd("/home/ftp/docs/")).thenReturn(false);
        when(ftpClient.getReplyCode()).thenReturn(FTPReply.NOT_LOGGED_IN);

        // 调用，并断言：抛出异常，独立连接已释放
        assertThrows(FtpException.class, () -> client.getStream("docs/a.txt"));
        verify(ftpClient, never()).retrieveFileStream(anyString());
        verify(streamFtp).close();
    }

    @Test
    public void testGetStream_releaseConnectionWhenRetrieveThrows() throws Exception {
        // 准备参数：读取时网络中断
        Ftp streamFtp = mock(Ftp.class);
        FTPClient ftpClient = mock(FTPClient.class);
        doReturn(streamFtp).when(client).createFtp();
        when(streamFtp.getClient()).thenReturn(ftpClient);
        when(streamFtp.cd("/home/ftp/docs/")).thenReturn(true);
        when(ftpClient.retrieveFileStream("a.txt")).thenThrow(new IOException("Connection reset"));

        // 调用，并断言：网络异常向上抛出，独立连接已释放
        assertThrows(IOException.class, () -> client.getStream("docs/a.txt"));
        verify(streamFtp).close();
    }

    @Test
    public void testUpload_streamOnDedicatedConnection() throws Exception {
        // 准备参数
        Ftp streamFtp = mock(Ftp.class);
        doReturn(streamFtp).when(client).createFtp();
        InputStream content = new ByteArrayInputStream(new byte[]{1, 2, 3});
        when(streamFtp.upload("/home/ftp/docs/", "a.txt", content)).thenReturn(true);

        // 调用
        String url = client.upload(content, 3, "docs/a.txt", "text/plain");

        // 断言：独立连接边读边写后释放，不使用共享连接
        assertEquals("http://127.0.0.1:48080/admin-api/infra/file/0/get/docs/a.txt", url);
        verify(streamFtp).close();
        verifyNoInteractions(sharedFtp);
    }

    @Test
    public void testUpload_releaseConnectionWhenFailed() throws Exception {
        // 准备参数：上传中断
        Ftp streamFtp = mock(Ftp.class);
        doReturn(streamFtp).when(client).createFtp();
        when(streamFtp.upload(anyString(), anyString(), any(InputStream.class)))
                .thenThrow(new IORuntimeException("Broken pipe"));

        // 调用，并断言：异常向上抛出，独立连接已释放
        assertThrows(IORuntimeException.class, () -> client.upload(new ByteArrayInputStream(new byte[]{1}),
                1, "docs/a.txt", "text/plain"));
        verify(streamFtp).close();
    }

}
