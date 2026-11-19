package cn.iocoder.yudao.module.infra.framework.file.core.client.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link S3FileClient} 文件流读写的单元测试
 *
 * @author 芋道源码
 */
public class S3FileClientStreamTest {

    private S3FileClient fileClient;
    private S3Client s3Client;

    @BeforeEach
    public void setUp() {
        // 不建立真实连接：公开访问不需要签名，只注入 SDK 客户端
        S3FileClientConfig config = new S3FileClientConfig();
        config.setDomain("https://static.iocoder.cn");
        config.setBucket("test");
        config.setEnablePublicAccess(true);
        fileClient = new S3FileClient(0L, config);
        s3Client = mock(S3Client.class);
        ReflectionTestUtils.setField(fileClient, "client", s3Client);
    }

    @Test
    public void testUpload_stream() {
        // 准备参数：超过 SDK 默认可回退长度（128KB）的内容
        byte[] content = new byte[512 * 1024];
        Arrays.fill(content, (byte) 'x');
        AtomicReference<byte[]> uploaded = new AtomicReference<>();
        AtomicReference<PutObjectRequest> requestRef = new AtomicReference<>();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenAnswer(invocation -> {
            RequestBody body = invocation.getArgument(1);
            requestRef.set(invocation.getArgument(0));
            // SDK 计算签名或校验和、发送时会多次读取请求体，每次都要得到完整内容
            body.contentStreamProvider().newStream().readAllBytes();
            try (InputStream input = body.contentStreamProvider().newStream()) {
                uploaded.set(input.readAllBytes());
            }
            return PutObjectResponse.builder().build();
        });

        // 调用
        String url = fileClient.upload(new ByteArrayInputStream(content), content.length, "docs/a.txt", "text/plain");

        // 断言：按实际长度上传完整内容，不走字节数组上传
        assertEquals("https://static.iocoder.cn/docs/a.txt", url);
        assertArrayEquals(content, uploaded.get());
        assertEquals(content.length, requestRef.get().contentLength());
        assertEquals("text/plain", requestRef.get().contentType());
        assertEquals("docs/a.txt", requestRef.get().key());
    }

    @Test
    public void testGetStream_returnSdkResponseStream() throws Exception {
        // 准备参数
        InputStream content = spy(new ByteArrayInputStream(new byte[]{1, 2}));
        ResponseInputStream<GetObjectResponse> response = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength(2L).build(), AbortableInputStream.create(content));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(response);

        // 调用
        try (InputStream input = fileClient.getStream("docs/a.txt")) {
            assertSame(response, input);
            assertArrayEquals(new byte[]{1, 2}, input.readAllBytes());
        }

        // 断言：调用方关闭时释放 SDK 响应流
        verify(content).close();
    }

    @Test
    public void testGetContent_closeResponseStream() throws Exception {
        // 准备参数
        InputStream content = spy(new ByteArrayInputStream(new byte[]{1, 2}));
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(new ResponseInputStream<>(
                GetObjectResponse.builder().build(), AbortableInputStream.create(content)));

        // 调用，并断言：字节数组读取完成后关闭响应流
        assertArrayEquals(new byte[]{1, 2}, fileClient.getContent("docs/a.txt"));
        verify(content).close();
    }

}
