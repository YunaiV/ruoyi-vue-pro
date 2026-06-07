package cn.iocoder.yudao.module.infra.framework.file.core.local;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.infra.framework.file.core.client.local.LocalFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.local.LocalFileClientConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

public class LocalFileClientTest {

    @TempDir
    public File tempDir;

    @Test
    public void testUpload_success() {
        // 准备参数
        LocalFileClient client = createClient();
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);
        String path = "avatar/test.txt";

        // 调用
        String url = client.upload(content, path, "text/plain");

        // 断言
        assertEquals("http://127.0.0.1:48080/admin-api/infra/file/0/get/avatar/test.txt", url);
        assertArrayEquals(content, FileUtil.readBytes(new File(tempDir, path)));
        assertArrayEquals(content, client.getContent(path));

        // 删除
        client.delete(path);
        assertFalse(FileUtil.exist(new File(tempDir, path)));
    }

    @Test
    public void testUpload_encodeUrlPath() {
        // 准备参数
        LocalFileClient client = createClient();
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);
        String path = "avatar/中文 100%+文件.txt";

        // 调用
        String url = client.upload(content, path, "text/plain");

        // 断言
        assertEquals("http://127.0.0.1:48080/admin-api/infra/file/0/get/avatar/%E4%B8%AD%E6%96%87%20100%25+%E6%96%87%E4%BB%B6.txt", url);
        assertArrayEquals(content, FileUtil.readBytes(new File(tempDir, path)));
    }

    @Test
    public void testUpload_pathInvalid() {
        // 准备参数
        LocalFileClient client = createClient();
        byte[] content = "test".getBytes(StandardCharsets.UTF_8);

        // 调用，并断言异常
        assertThrows(ServiceException.class, () -> client.upload(content, "../test.txt", "text/plain"));
        assertFalse(FileUtil.exist(new File(tempDir.getParentFile(), "test.txt")));
    }

    @Test
    @Disabled
    public void test() {
        // 创建客户端
        LocalFileClientConfig config = new LocalFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath("/Users/yunai/file_test");
        LocalFileClient client = new LocalFileClient(0L, config);
        client.init();
        // 上传文件
        String path = IdUtil.fastSimpleUUID() + ".jpg";
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String fullPath = client.upload(content, path, "image/jpeg");
        System.out.println("访问地址：" + fullPath);
        client.delete(path);
    }

    @Test
    @Disabled
    public void testGetContent_notFound() {
        // 创建客户端
        LocalFileClientConfig config = new LocalFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath("/Users/yunai/file_test");
        LocalFileClient client = new LocalFileClient(0L, config);
        client.init();
        // 上传文件
        byte[] content = client.getContent(randomString());
        System.out.println();
    }

    private LocalFileClient createClient() {
        LocalFileClientConfig config = new LocalFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath(tempDir.getAbsolutePath());
        LocalFileClient client = new LocalFileClient(0L, config);
        client.init();
        return client;
    }

}
