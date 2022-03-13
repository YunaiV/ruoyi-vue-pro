package cn.iocoder.yudao.framework.file.core.client.impl.s3;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.file.core.client.impl.AbstractFileClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

import static cn.iocoder.yudao.framework.file.core.client.impl.s3.S3FileClientConfig.ENDPOINT_QINIU;

/**
 * 基于 S3 协议，实现 MinIO、阿里云、腾讯云、七牛云、华为云等云服务
 *
 * S3 协议的客户端，采用亚马逊提供的 software.amazon.awssdk.s3 库
 *
 * @author 芋道源码
 */
public class S3FileClient extends AbstractFileClient<S3FileClientConfig> {

    private S3Client client;

    public S3FileClient(Long id, S3FileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 补全 domain
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(createDomain());
        }
        // 初始化客户端
        client = S3Client.builder()
                .serviceConfiguration(sb -> sb.pathStyleAccessEnabled(false) // 关闭路径风格
                .chunkedEncodingEnabled(false)) // 禁用 chunk
                .endpointOverride(createURI()) // 上传地址
                .region(Region.of(config.getRegion())) // Region
                .credentialsProvider(StaticCredentialsProvider.create( // 认证密钥
                        AwsBasicCredentials.create(config.getAccessKey(), config.getAccessSecret())))
                .overrideConfiguration(cb -> cb.addExecutionInterceptor(new S3ModifyPathInterceptor(config.getBucket())))
                .build();
    }

    /**
     * 基于 endpoint 构建调用云服务的 URI 地址
     *
     * @return URI 地址
     */
    private URI createURI() {
        String uri;
        // 如果是七牛，无需拼接 bucket
        if (config.getEndpoint().contains(ENDPOINT_QINIU)) {
            uri = StrUtil.format("https://{}", config.getEndpoint());
        } else {
            uri = StrUtil.format("https://{}.{}", config.getBucket(), config.getEndpoint());
        }
        return URI.create(uri);
    }

    /**
     * 基于 bucket + endpoint 构建访问的 Domain 地址
     *
     * @return Domain 地址
     */
    private String createDomain() {
        return StrUtil.format("https://{}.{}", config.getBucket(), config.getEndpoint());
    }

    @Override
    public String upload(byte[] content, String path) {
        // 执行上传
        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(config.getBucket()) // bucket 必须传递
                .key(path); // 相对路径作为 key
        client.putObject(request.build(), RequestBody.fromBytes(content));
        // 拼接返回路径
        return config.getDomain() + "/" + path;
    }

    @Override
    public void delete(String path) {

    }

    @Override
    public byte[] getContent(String path) {
        return new byte[0];
    }

}
