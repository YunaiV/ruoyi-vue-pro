package cn.iocoder.yudao.framework.file.core.client.ftp;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.ftp.FtpMode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FtpFileClientTest {

    @Test
    @Disabled
    public void test() {
        // 创建客户端
        FtpFileClientConfig config = new FtpFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath("/home/ftp");
        config.setHost("kanchai.club");
        config.setPort(221);
        config.setUsername("");
        config.setPassword("");
        config.setMode(FtpMode.Passive.name());
        FtpFileClient client = new FtpFileClient(0L, config);
        client.init();
        // 上传文件
        String path = IdUtil.fastSimpleUUID() + ".jpg";
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String fullPath = client.upload(content, path);
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
