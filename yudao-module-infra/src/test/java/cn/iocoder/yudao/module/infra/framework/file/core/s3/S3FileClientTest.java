package cn.iocoder.yudao.module.infra.framework.file.core.s3;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("resource")
public class S3FileClientTest {

    @Test
    public void testPresignGetUrl_publicAccess_encodeUrlPath() {
        // 准备参数
        S3FileClientConfig config = new S3FileClientConfig();
        config.setDomain("https://static.iocoder.cn");
        config.setEnablePublicAccess(true);
        S3FileClient client = new S3FileClient(0L, config);

        // 调用
        String result = client.presignGetUrl("avatar/中文 100%+文件.jpg", 300);

        // 断言
        assertEquals("https://static.iocoder.cn/avatar/%E4%B8%AD%E6%96%87%20100%25+%E6%96%87%E4%BB%B6.jpg", result);
    }

    @Test
    public void testPresignGetUrl_publicAccess_decodeDomainUrl() {
        // 准备参数
        S3FileClientConfig config = new S3FileClientConfig();
        config.setDomain("https://static.iocoder.cn");
        config.setEnablePublicAccess(true);
        S3FileClient client = new S3FileClient(0L, config);

        // 调用
        String result = client.presignGetUrl("https://static.iocoder.cn/avatar/%E4%B8%AD%E6%96%87%20100%25+%E6%96%87%E4%BB%B6.jpg?token=1", 300);

        // 断言
        assertEquals("https://static.iocoder.cn/avatar/%E4%B8%AD%E6%96%87%20100%25+%E6%96%87%E4%BB%B6.jpg", result);
    }

    @Test
    public void testPresignGetUrl_privateAccess_rawPath() {
        // 准备参数
        S3FileClientConfig config = new S3FileClientConfig();
        config.setAccessKey("admin");
        config.setAccessSecret("password");
        config.setBucket("yudao");
        config.setDomain("http://127.0.0.1:9000/yudao");
        config.setEndpoint("http://127.0.0.1:9000");
        config.setEnablePathStyleAccess(true);
        config.setEnablePublicAccess(false);
        S3FileClient client = new S3FileClient(0L, config);
        client.init();

        // 调用
        String result = client.presignGetUrl("avatar/中文 100%+文件.jpg", 300);

        // 断言
        assertTrue(result.contains("/yudao/avatar/%E4%B8%AD%E6%96%87%20100%25"));
        assertTrue(result.contains("%E6%96%87%E4%BB%B6.jpg"));
        assertFalse(result.contains("%25E4%25B8%25AD"));
    }

    @Test
    @Disabled // MinIO，如果要集成测试，可以注释本行
    public void testMinIO() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
        config.setAccessKey("admin");
        config.setAccessSecret("password");
        config.setBucket("yudaoyuanma");
        config.setDomain(null);
        // 默认 9000 endpoint
        config.setEndpoint("http://127.0.0.1:9000");

        // 执行上传
        testExecuteUpload(config);
    }

    @Test
    @Disabled // 阿里云 OSS，如果要集成测试，可以注释本行
    public void testAliyun() throws Exception {
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
    public void testQCloud() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
        config.setAccessKey(System.getenv("QCLOUD_ACCESS_KEY"));
        config.setAccessSecret(System.getenv("QCLOUD_SECRET_KEY"));
        config.setBucket("aoteman-1255880240");
        config.setDomain(null); // 如果有自定义域名，则可以设置。http://tengxun-oss.iocoder.cn
        // 默认上海的 endpoint
        config.setEndpoint("cos.ap-shanghai.myqcloud.com");

        // 执行上传
        testExecuteUpload(config);
    }

    @Test
    @Disabled // 七牛云存储，如果要集成测试，可以注释本行
    public void testQiniu() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
//        config.setAccessKey(System.getenv("QINIU_ACCESS_KEY"));
//        config.setAccessSecret(System.getenv("QINIU_SECRET_KEY"));
        config.setAccessKey("b7yvuhBSAGjmtPhMFcn9iMOxUOY_I06cA_p0ZUx8");
        config.setAccessSecret("kXM1l5ia1RvSX3QaOEcwI3RLz3Y2rmNszWonKZtP");
        config.setBucket("ruoyi-vue-pro");
        config.setDomain("http://test.yudao.iocoder.cn"); // 如果有自定义域名，则可以设置。http://static.yudao.iocoder.cn
        config.setEnablePathStyleAccess(false);
        // 默认上海的 endpoint
        config.setEndpoint("s3-cn-south-1.qiniucs.com");

        // 执行上传
        testExecuteUpload(config);
    }

    @Test
    @Disabled // 七牛云存储（读私有桶），如果要集成测试，可以注释本行
    public void testQiniu_privateGet() {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
//        config.setAccessKey(System.getenv("QINIU_ACCESS_KEY"));
//        config.setAccessSecret(System.getenv("QINIU_SECRET_KEY"));
        config.setAccessKey("b7yvuhBSAGjmtPhMFcn9iMOxUOY_I06cA_p0ZUx8");
        config.setAccessSecret("kXM1l5ia1RvSX3QaOEcwI3RLz3Y2rmNszWonKZtP");
        config.setBucket("ruoyi-vue-pro-private");
        config.setDomain("http://t151glocd.hn-bkt.clouddn.com"); // 如果有自定义域名，则可以设置。http://static.yudao.iocoder.cn
        config.setEnablePathStyleAccess(false);
        // 默认上海的 endpoint
        config.setEndpoint("s3-cn-south-1.qiniucs.com");

        // 校验配置
        ValidationUtils.validate(Validation.buildDefaultValidatorFactory().getValidator(), config);
        // 创建 Client
        S3FileClient client = new S3FileClient(0L, config);
        client.init();
        // 执行生成 URL 签名
        String path = "output.png";
        String presignedUrl = client.presignGetUrl(path, 300);
        System.out.println(presignedUrl);
    }

    @Test
    @Disabled // 华为云存储，如果要集成测试，可以注释本行
    public void testHuaweiCloud() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        // 配置成你自己的
//        config.setAccessKey(System.getenv("HUAWEI_CLOUD_ACCESS_KEY"));
//        config.setAccessSecret(System.getenv("HUAWEI_CLOUD_SECRET_KEY"));
        config.setBucket("yudao");
        config.setDomain(null); // 如果有自定义域名，则可以设置。
        // 默认上海的 endpoint
        config.setEndpoint("obs.cn-east-3.myhuaweicloud.com");

        // 执行上传
        testExecuteUpload(config);
    }

    private void testExecuteUpload(S3FileClientConfig config) {
        // 校验配置
        ValidationUtils.validate(Validation.buildDefaultValidatorFactory().getValidator(), config);
        // 创建 Client
        S3FileClient client = new S3FileClient(0L, config);
        client.init();
        // 上传文件
        String path = IdUtil.fastSimpleUUID() + ".jpg";
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String fullPath = client.upload(content, path, "image/jpeg");
        System.out.println("访问地址：" + fullPath);
        // 读取文件
        if (true) {
            byte[] bytes = client.getContent(path);
            System.out.println("文件内容：" + bytes.length);
        }
        // 删除文件
        if (false) {
            client.delete(path);
        }
    }

}
