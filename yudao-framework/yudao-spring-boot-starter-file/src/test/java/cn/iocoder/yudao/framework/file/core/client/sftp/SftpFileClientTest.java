package cn.iocoder.yudao.framework.file.core.client.sftp;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SftpFileClientTest {

    @Test
    @Disabled
    public void test() {
        // 创建客户端
        SftpFileClientConfig config = new SftpFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath("/home/ftp");
        config.setHost("kanchai.club");
        config.setPort(222);
        config.setUsername("");
        config.setPassword("");
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
