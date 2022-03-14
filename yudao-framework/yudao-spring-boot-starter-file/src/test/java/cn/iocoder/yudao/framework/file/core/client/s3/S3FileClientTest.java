package cn.iocoder.yudao.framework.file.core.client.s3;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.framework.file.core.client.impl.s3.S3FileClient;
import cn.iocoder.yudao.framework.file.core.client.impl.s3.S3FileClientConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;

public class S3FileClientTest {

    @Test
    @Disabled // 阿里云 OSS，如果要集成测试，可以注释本行
    public void testAliyun() {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
        config.setAccessKey(System.getenv("ALIYUN_ACCESS_KEY"));
        config.setAccessSecret(System.getenv("ALIYUN_SECRET_KEY"));
        config.setBucket("yunai-aoteman");
        config.setDomain(null); // 如果有自定义域名，则可以设置。http://ali-oss.iocoder.cn
        // 默认北京的 endpoint
        config.setEndpoint("oss-cn-beijing.aliyuncs.com");

        // 执行上传
        testExecuteUpload(config);
    }

    @Test
    @Disabled // 腾讯云 COS，如果要集成测试，可以注释本行
    public void testQCloud() {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
        config.setAccessKey(System.getenv("QCLOUD_ACCESS_KEY"));
        config.setAccessSecret(System.getenv("QCLOUD_SECRET_KEY"));
        config.setBucket("aoteman-1255880240");
        config.setDomain(null); // 如果有自定义域名，则可以设置。http://tengxun-oss.iocoder.cn
        // 默认上海的 endpoint
        config.setEndpoint("cos.ap-shanghai.myqcloud.com");
        config.setRegion("ap-shanghai");

        // 执行上传
        testExecuteUpload(config);
    }

    @Test
    @Disabled // 七牛云存储，如果要集成测试，可以注释本行
    public void testQiniu() {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
        config.setAccessKey(System.getenv("QINIU_ACCESS_KEY"));
        config.setAccessSecret(System.getenv("QINIU_SECRET_KEY"));
        config.setBucket("s3-test-yudao");
        config.setDomain("http://r8oo8po1q.hn-bkt.clouddn.com"); // 如果有自定义域名，则可以设置。http://static.yudao.iocoder.cn
        // 默认上海的 endpoint
        config.setEndpoint("s3-cn-south-1.qiniucs.com");

        // 执行上传
        testExecuteUpload(config);
    }

    private void testExecuteUpload(S3FileClientConfig config) {
        // 补全配置
        if (config.getRegion() == null) {
            config.setRegion(StrUtil.subBefore(config.getEndpoint(), '.', false));
        }
        ValidationUtils.validate(Validation.buildDefaultValidatorFactory().getValidator(), config);
        // 创建 Client
        S3FileClient client = new S3FileClient(0L, config);
        client.init();
        // 上传文件
        String path = IdUtil.fastSimpleUUID() + ".jpg";
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String fullPath = client.upload(content, path);
        System.out.println("访问地址：" + fullPath);
        // 读取文件
        if (false) {
            byte[] bytes = client.getContent(path);
            System.out.println("文件内容：" + bytes);
        }
        // 删除文件
        if (false) {
            client.delete(path);
        }
    }

}
