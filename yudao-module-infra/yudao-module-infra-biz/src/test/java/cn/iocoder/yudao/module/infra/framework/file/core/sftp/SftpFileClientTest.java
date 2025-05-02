package cn.iocoder.yudao.module.infra.framework.file.core.sftp;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClientConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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

}
