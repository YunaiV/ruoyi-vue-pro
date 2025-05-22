package cn.iocoder.yudao.module.infra.framework.file.core.ftp;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.ftp.FtpMode;
import cn.iocoder.yudao.module.infra.framework.file.core.client.ftp.FtpFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.ftp.FtpFileClientConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * {@link FtpFileClient} 集成测试
 *
 * @author 芋道源码
 */
public class FtpFileClientTest {

//    docker run -d \
//            -p 2121:21 -p 30000-30009:30000-30009 \
//            -e FTP_USER=foo \
//            -e FTP_PASS=pass \
//            -e PASV_ADDRESS=127.0.0.1 \
//            -e PASV_MIN_PORT=30000 \
//            -e PASV_MAX_PORT=30009 \
//            -v $(pwd)/ftp-data:/home/vsftpd \
//    fauria/vsftpd

    @Test
    @Disabled
    public void test() {
        // 创建客户端
        FtpFileClientConfig config = new FtpFileClientConfig();
        config.setDomain("http://127.0.0.1:48080");
        config.setBasePath("/home/ftp");
        config.setHost("127.0.0.1");
        config.setPort(2121);
        config.setUsername("foo");
        config.setPassword("pass");
        config.setMode(FtpMode.Passive.name());
        FtpFileClient client = new FtpFileClient(0L, config);
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
