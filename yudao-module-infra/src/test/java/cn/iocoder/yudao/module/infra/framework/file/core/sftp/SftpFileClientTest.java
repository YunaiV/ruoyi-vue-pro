package cn.iocoder.yudao.module.infra.framework.file.core.sftp;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClientConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import cn.hutool.extra.ssh.Sftp;
import cn.hutool.extra.ssh.JschRuntimeException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link SftpFileClient} 集成测试
 *
 * @author 芋道源码
 */
public class SftpFileClientTest {

//    docker run -p 2222:22 -d \
//            -v $(pwd)/sftp-data:/home/foo/upload \
//    atmoz/sftp \
//    foo:pass:1001

    @Test
    @Disabled
    public void test() {
        // 创建客户端
        SftpFileClientConfig config = new SftpFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath("/upload"); // 注意，这个是相对路径，不是实际 linux 上的路径！！！
        config.setHost("127.0.0.1");
        config.setPort(2222);
        config.setUsername("foo");
        config.setPassword("pass");
        SftpFileClient client = new SftpFileClient(0L, config);
        client.init();
        // 上传文件
        String path = IdUtil.fastSimpleUUID() + ".jpg";
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String fullPath = client.upload(content, path, "image/jpeg");
        System.out.println("访问地址：" + fullPath);
        if (false) {
            byte[] bytes = client.getContent(path);
            System.out.println("文件内容：" + bytes);
        }
        if (false) {
            client.delete(path);
        }
    }

    @Test
    public void testGetContent_stream() {
        // 准备参数
        Sftp sftp = mock(Sftp.class);
        SftpFileClient client = buildMockClient(sftp);
        byte[] content = "synthetic-sftp-content".getBytes(StandardCharsets.UTF_8);
        // mock 下载方法
        doAnswer(invocation -> {
            invocation.<OutputStream>getArgument(1).write(content);
            return null;
        }).when(sftp).download(eq("/upload/test.txt"), any(OutputStream.class));

        // 调用
        byte[] result = client.getContent("test.txt");

        // 断言
        assertArrayEquals(content, result);
        verify(sftp).reconnectIfTimeout();
        verify(sftp).download(eq("/upload/test.txt"), any(OutputStream.class));
        verifyNoMoreInteractions(sftp);
    }

    @Test
    public void testGetContent_downloadFailure() {
        // 准备参数
        Sftp sftp = mock(Sftp.class);
        SftpFileClient client = buildMockClient(sftp);
        JschRuntimeException failure = new JschRuntimeException("synthetic failure");
        // mock 下载失败
        doThrow(failure).when(sftp).download(eq("/upload/test.txt"), any(OutputStream.class));

        // 调用，并断言异常
        JschRuntimeException result = assertThrows(JschRuntimeException.class, () -> client.getContent("test.txt"));

        // 断言
        assertSame(failure, result);
        verify(sftp).reconnectIfTimeout();
        verify(sftp).download(eq("/upload/test.txt"), any(OutputStream.class));
        verifyNoMoreInteractions(sftp);
    }

    @Test
    public void testUpload_stream() {
        // 准备参数
        Sftp sftp = mock(Sftp.class);
        SftpFileClient client = buildMockClient(sftp);
        byte[] content = "synthetic-upload-content".getBytes(StandardCharsets.UTF_8);
        // mock 上传方法
        when(sftp.upload(eq("/upload/"), eq("test.txt"), any(InputStream.class))).thenAnswer(invocation -> {
            assertArrayEquals(content, invocation.<InputStream>getArgument(2).readAllBytes());
            return true;
        });

        // 调用
        String result = client.upload(content, "test.txt", "text/plain");

        // 断言
        assertEquals("http://localhost/admin-api/infra/file/0/get/test.txt", result);
        verify(sftp).reconnectIfTimeout();
        verify(sftp).mkDirs("/upload/");
        verify(sftp).upload(eq("/upload/"), eq("test.txt"), any(InputStream.class));
        verifyNoMoreInteractions(sftp);
    }

    @Test
    public void testUpload_failure() {
        // 准备参数
        Sftp sftp = mock(Sftp.class);
        SftpFileClient client = buildMockClient(sftp);
        // mock 上传失败
        when(sftp.upload(eq("/upload/"), eq("test.txt"), any(InputStream.class))).thenReturn(false);

        // 调用，并断言异常
        assertThrows(JschRuntimeException.class,
                () -> client.upload(new byte[0], "test.txt", "text/plain"));

        // 断言
        verify(sftp).reconnectIfTimeout();
        verify(sftp).mkDirs("/upload/");
        verify(sftp).upload(eq("/upload/"), eq("test.txt"), any(InputStream.class));
        verifyNoMoreInteractions(sftp);
    }

    private SftpFileClient buildMockClient(Sftp sftp) {
        SftpFileClientConfig config = new SftpFileClientConfig();
        config.setBasePath("/upload");
        config.setDomain("http://localhost");
        SftpFileClient client = new SftpFileClient(0L, config);
        ReflectionTestUtils.setField(client, "sftp", sftp);
        return client;
    }

}
