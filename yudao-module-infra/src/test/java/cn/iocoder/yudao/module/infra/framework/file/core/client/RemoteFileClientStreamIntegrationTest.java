package cn.iocoder.yudao.module.infra.framework.file.core.client;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.ftp.FtpMode;
import cn.iocoder.yudao.module.infra.framework.file.core.client.ftp.FtpFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.ftp.FtpFileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClient;
import cn.iocoder.yudao.module.infra.framework.file.core.client.sftp.SftpFileClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 远程文件客户端文件流读写的集成测试
 *
 * 通过 {@code -Dinfra.file.integration=true} 开启，复用以下 FTP、SFTP 和 S3 测试服务
 *
 * <pre>{@code
 * docker run -d --name cms-file-ftp -p 32121:21 -p 32130-32139:32130-32139 \
 *   -e FTP_USER=cms -e FTP_PASS=cms-pass -e PASV_ADDRESS=127.0.0.1 \
 *   -e PASV_MIN_PORT=32130 -e PASV_MAX_PORT=32139 fauria/vsftpd
 * docker run -d --name cms-file-sftp -p 32222:22 atmoz/sftp cms:cms-pass:1001::upload
 * docker run -d --name cms-file-s3 -p 39000:9000 \
 *   -e MINIO_ROOT_USER=cms -e MINIO_ROOT_PASSWORD=cms-pass \
 *   minio/minio server /data --address :9000
 * }</pre>
 *
 * @author 芋道源码
 */
@EnabledIfSystemProperty(named = "infra.file.integration", matches = "true")
public class RemoteFileClientStreamIntegrationTest {

    private static final String HOST = "127.0.0.1";
    private static final String USERNAME = "cms";
    private static final String PASSWORD = "cms-pass";
    private static final String S3_BUCKET = "infra-file-client-test";

    @Test
    public void testFtp() throws Exception {
        FtpFileClientConfig config = new FtpFileClientConfig().setBasePath("/infra")
                .setDomain("http://127.0.0.1:48080").setHost(HOST).setPort(32121)
                .setUsername(USERNAME).setPassword(PASSWORD).setMode(FtpMode.Passive.name());
        FtpFileClient client = new FtpFileClient(1L, config);
        client.init();
        executeStreamOperations(client);
        executeFtpMissingOperations(client);
    }

    private static void executeFtpMissingOperations(FtpFileClient client) throws Exception {
        // 准备：已存在的目录中放一个文件
        String directory = "missing-" + IdUtil.fastSimpleUUID();
        String existing = directory + "/exists.txt";
        client.upload("exists".getBytes(), existing, "text/plain");
        try {
            // 文件不存在、目录不存在（550）都返回 null，由下载接口按 404 处理；反复调用验证独立连接都已释放
            for (int i = 0; i < 20; i++) {
                assertNull(client.getStream(directory + "/missing-" + i + ".txt"));
                assertNull(client.getStream("missing-dir-" + IdUtil.fastSimpleUUID() + "/a.txt"));
            }
            // 之后共享连接与独立连接仍可正常读写
            try (InputStream input = client.getStream(existing)) {
                assertArrayEquals("exists".getBytes(), cn.hutool.core.io.IoUtil.readBytes(input));
            }
            assertArrayEquals("exists".getBytes(), client.getContent(existing));
        } finally {
            client.delete(existing);
        }
    }

    @Test
    public void testSftp() throws Exception {
        SftpFileClientConfig config = new SftpFileClientConfig().setBasePath("/upload/infra")
                .setDomain("http://127.0.0.1:48080").setHost(HOST).setPort(32222)
                .setUsername(USERNAME).setPassword(PASSWORD);
        SftpFileClient client = new SftpFileClient(2L, config);
        client.init();
        executeStreamOperations(client);
    }

    @Test
    public void testS3() throws Exception {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setEndpoint("http://" + HOST + ":39000");
        config.setDomain("http://" + HOST + ":39000/" + S3_BUCKET);
        config.setRegion(Region.US_EAST_1.id());
        config.setBucket(S3_BUCKET);
        config.setAccessKey(USERNAME);
        config.setAccessSecret(PASSWORD);
        config.setEnablePathStyleAccess(true);
        config.setEnablePublicAccess(false);
        createS3Bucket(config);
        S3FileClient client = new S3FileClient(3L, config);
        client.init();
        executeStreamOperations(client);
    }

    private static void executeStreamOperations(FileClient client) throws Exception {
        // 1. 上传超过 SDK 默认回退长度和探测上限的文件流
        byte[] content = RandomUtil.randomBytes(5 * 1024 * 1024 + 13);
        String path = "stream-" + IdUtil.fastSimpleUUID() + "/中文 a+b.bin";
        String otherPath = "stream-" + IdUtil.fastSimpleUUID() + "/other.txt";
        try {
            client.upload(new ByteArrayInputStream(content), content.length, path, "application/octet-stream");

            // 2. 读取流未结束时，共享连接上的其它操作不被阻塞，读取完成后内容完整
            try (InputStream input = client.getStream(path)) {
                byte[] head = cn.hutool.core.io.IoUtil.readBytes(input, 1024);
                client.upload("other".getBytes(), otherPath, "text/plain");
                byte[] rest = cn.hutool.core.io.IoUtil.readBytes(input);
                byte[] actual = Arrays.copyOf(head, head.length + rest.length);
                System.arraycopy(rest, 0, actual, head.length, rest.length);
                assertArrayEquals(content, actual);
            }
            // 3. 原字节数组读取接口保持可用
            assertArrayEquals(content, client.getContent(path));
            assertArrayEquals("other".getBytes(), client.getContent(otherPath));
        } finally {
            client.delete(path);
            client.delete(otherPath);
        }
    }

    private static void createS3Bucket(S3FileClientConfig config) {
        S3Configuration serviceConfig = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)
                .build();
        try (S3Client client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(config.getAccessKey(), config.getAccessSecret())))
                .region(Region.of(config.getRegion()))
                .endpointOverride(URI.create(config.getEndpoint()))
                .serviceConfiguration(serviceConfig)
                .build()) {
            client.createBucket(CreateBucketRequest.builder().bucket(config.getBucket()).build());
        } catch (S3Exception ex) {
            if (ex.statusCode() != 409) {
                throw ex;
            }
        }
    }

}
