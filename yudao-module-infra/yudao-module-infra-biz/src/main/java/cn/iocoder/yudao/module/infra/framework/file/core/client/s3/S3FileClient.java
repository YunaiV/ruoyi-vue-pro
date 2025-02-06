package cn.iocoder.yudao.module.infra.framework.file.core.client.s3;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.module.infra.framework.file.core.client.AbstractFileClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

/**
 * 基于 S3 协议的文件客户端，实现 MinIO、阿里云、腾讯云、七牛云、华为云等云服务
 *
 * @author 芋道源码
 */
public class S3FileClient extends AbstractFileClient<S3FileClientConfig> {

    private S3Client client;

    public S3FileClient(Long id, S3FileClientConfig config) {
        super(id, config);
    }

    /**
     * 动态创建 S3Presigner
     *
     * @param endpoint  节点地址
     * @param accessKey 访问 Key
     * @param secretKey 访问 Secret
     * @return S3Presigner
     */
    private static S3Presigner createPresigner(String endpoint, String accessKey, String secretKey) {
        return S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    /**
     * 生成动态的预签名上传 URL
     *
     * @param bucket    存储 Bucket
     * @param path      相对路径
     * @param duration  过期时间
     * @param endpoint  节点地址
     * @param accessKey 访问 Key
     * @param secretKey 访问 Secret
     * @return 生成的上传 URL
     */
    public static String getPresignedUrl(String bucket, String path, Duration duration,
                                         String endpoint, String accessKey, String secretKey) {
        try (S3Presigner presigner = createPresigner(endpoint, accessKey, secretKey)) {
            return presigner.presignPutObject(PutObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .putObjectRequest(b -> b.bucket(bucket).key(path))
                    .build()).url().toString();
        }
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
    protected void doInit() {
        // 补全 domain
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(buildDomain());
        }
        // 初始化 S3 客户端
        client = S3Client.builder()
                .credentialsProvider(buildCredentials())
                .region(Region.of(config.getEndpoint())) // 这里随便填，SDK 需要
                .endpointOverride(URI.create(buildEndpoint()))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()) //  Path-style 访问
                .build();
    }

    /**
     * 基于 config 秘钥，构建 S3 客户端的认证信息
     *
     * @return S3 客户端的认证信息
     */
    private StaticCredentialsProvider buildCredentials() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(config.getAccessKey(), config.getAccessSecret()));
    }

    /**
     * 节点地址补全协议头
     *
     * @return 节点地址
     */
    private String buildEndpoint() {
        // 如果已经是 http 或者 https，则不进行拼接
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return config.getEndpoint();
        }
        return StrUtil.format("https://{}", config.getEndpoint());
    }

    @Override
    public String upload(byte[] content, String path, String type) {
        // 构造 PutObjectRequest
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .contentType(type)
                .contentLength((long) content.length)
                .build();

        // 上传文件
        client.putObject(putRequest, RequestBody.fromBytes(content));
        // 拼接返回路径
        return config.getDomain() + "/" + path;
    }

    @Override
    public void delete(String path) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .build();
        client.deleteObject(deleteRequest);
    }

    @Override
    public byte[] getContent(String path) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .build();

        return IoUtil.readBytes(client.getObject(getRequest));
    }

    @Override
    public FilePresignedUrlRespDTO getPresignedObjectUrl(String path) {
        String presignedUrl = getPresignedUrl(
                config.getBucket(),
                path,
                Duration.ofMinutes(10),
                config.getEndpoint(),
                config.getAccessKey(),
                config.getAccessSecret()
        );

        return new FilePresignedUrlRespDTO(presignedUrl, config.getDomain() + "/" + path);
    }

}
