package cn.iocoder.yudao.module.infra.framework.file.core.client.s3;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 基于 S3 协议的文件客户端，实现 MinIO、阿里云、腾讯云、七牛云、华为云等云服务
 * <p>
 * S3 协议的客户端，采用亚马逊提供的 software.amazon.awssdk.s3 库
 *
 * @author 芋道源码
 */
public class S3FileClient extends AbstractFileClient<S3FileClientConfig> {

    private AmazonS3Client client;

    public S3FileClient(Long id, S3FileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 补全 domain
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(buildDomain());
        }
        // 初始化客户端
        client = (AmazonS3Client)AmazonS3ClientBuilder.standard()
                .withCredentials(buildCredentials())
                .withEndpointConfiguration(buildEndpointConfiguration())
                .build();
    }

    /**
     * 基于 config 秘钥，构建 S3 客户端的认证信息
     *
     * @return S3 客户端的认证信息
     */
    private AWSStaticCredentialsProvider buildCredentials() {
        return new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(config.getAccessKey(), config.getAccessSecret()));
    }

    /**
     * 构建 S3 客户端的 Endpoint 配置，包括 region、endpoint
     *
     * @return  S3 客户端的 EndpointConfiguration 配置
     */
    private AwsClientBuilder.EndpointConfiguration buildEndpointConfiguration() {
        return new AwsClientBuilder.EndpointConfiguration(config.getEndpoint(),
                null); // 无需设置 region
    }

    /**
     * 基于 bucket + endpoint 构建访问的 Domain 地址
     *
     * @return Domain 地址
     */
    private String buildDomain() {
        // 如果已经是 http 或者 https，则不进行拼接.主要适配 MinIO
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return StrUtil.format("{}/{}", config.getEndpoint(), config.getBucket());
        }
        // 阿里云、腾讯云、华为云都适合。七牛云比较特殊，必须有自定义域名
        return StrUtil.format("https://{}.{}", config.getBucket(), config.getEndpoint());
    }

    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        // 元数据，主要用于设置文件类型
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(type);
        objectMetadata.setContentLength(content.length); // 如果不设置，会有 “ No content length specified for stream data” 警告日志
        // 执行上传
        client.putObject(config.getBucket(),
                path, // 相对路径
                new ByteArrayInputStream(content), // 文件内容
                objectMetadata);

        // 拼接返回路径
        return config.getDomain() + "/" + path;
    }

    @Override
    public void delete(String path) throws Exception {
        client.deleteObject(config.getBucket(), path);
    }

    @Override
    public byte[] getContent(String path) throws Exception {
        S3Object tempS3Object = client.getObject(config.getBucket(), path);
        return IoUtil.readBytes(tempS3Object.getObjectContent());
    }

    @Override
    public FilePresignedUrlRespDTO getPresignedObjectUrl(String path) throws Exception {
        // 设定过期时间为 10 分钟。取值范围：1 秒 ~ 7 天
        Date expiration = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10));
        // 生成上传 URL
        String uploadUrl = String.valueOf(client.generatePresignedUrl(config.getBucket(), path, expiration , HttpMethod.PUT));
        return new FilePresignedUrlRespDTO(uploadUrl, config.getDomain() + "/" + path);
    }

}
