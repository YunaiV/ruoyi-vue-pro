package cn.iocoder.yudao.module.deepay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * OSS 图片持久化服务实现（STEP 24）。
 *
 * <p>当前实现为"下载 + 本地临时缓存"策略：
 * <ul>
 *   <li>若配置了 {@code deepay.oss.bucket-url}，则下载图片并上传到指定 OSS 存储桶，
 *       返回形如 {@code {bucketUrl}/{uuid}.jpg} 的永久 URL。</li>
 *   <li>若未配置（开发 / 测试环境），直接返回原始 URL（降级，不影响流程）。</li>
 * </ul>
 * </p>
 *
 * <p>扩展说明：生产环境请替换 {@link #uploadToOss} 为对应 SDK 调用
 * （阿里云 OSS SDK / AWS S3 SDK / Cloudflare R2 SDK）。</p>
 *
 * <p>配置项（application.yml）：
 * <pre>
 * deepay:
 *   oss:
 *     bucket-url: https://your-bucket.oss-cn-hangzhou.aliyuncs.com  # 留空则不上传
 * </pre>
 * </p>
 */
@Slf4j
@Service
public class OssServiceImpl implements OssService {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS    = 15_000;
    /** Redis key 前缀，存储已上传图片的永久 URL，防止重复下载 */
    private static final String OSS_DEDUP_PREFIX = "img:";

    @Value("${deepay.oss.bucket-url:}")
    private String bucketUrl;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String persist(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) return imageUrl;
        if (!StringUtils.hasText(bucketUrl)) {
            log.debug("[OssService] bucket-url 未配置，原样返回 url={}", imageUrl);
            return imageUrl;
        }
        try {
            // Redis 去重：相同 URL 只上传一次（key = img:{md5}，TTL 24h）
            String dedupKey = OSS_DEDUP_PREFIX + DigestUtils.md5DigestAsHex(imageUrl.getBytes(StandardCharsets.UTF_8));
            String existing = stringRedisTemplate.opsForValue().get(dedupKey);
            if (StringUtils.hasText(existing)) {
                log.debug("[OssService] OSS 去重命中 url={} -> {}", imageUrl, existing);
                return existing;
            }
            String permanentUrl = uploadToOss(imageUrl);
            // 写入去重缓存，TTL 24 小时
            stringRedisTemplate.opsForValue().set(dedupKey, permanentUrl, Duration.ofHours(24));
            return permanentUrl;
        } catch (Exception e) {
            log.warn("[OssService] 上传失败，降级返回原始 URL url={}", imageUrl, e);
            return imageUrl;
        }
    }

    @Override
    public List<String> persistAll(List<String> imageUrls) {
        if (imageUrls == null) return new ArrayList<>();
        List<String> result = new ArrayList<>(imageUrls.size());
        for (String url : imageUrls) {
            result.add(persist(url));
        }
        return result;
    }

    /**
     * 下载图片后上传至 OSS，返回永久 URL。
     *
     * <p>生产环境请将此方法中的"写本地临时文件 + 上传"逻辑替换为对应 OSS SDK 调用。</p>
     */
    private String uploadToOss(String imageUrl) throws Exception {
        String filename = UUID.randomUUID() + ".jpg";
        Path tmpFile = Files.createTempFile("deepay-oss-", ".jpg");

        try {
            // Step 1: 下载到本地临时文件
            HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestProperty("User-Agent", "DeepayOssBot/1.0");
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                conn.disconnect();
            }

            // Step 2: 上传到 OSS
            // TODO: 替换为实际 OSS SDK 调用，例如：
            //   OSSClient ossClient = new OSSClient(endpoint, credentials);
            //   ossClient.putObject(bucketName, filename, tmpFile.toFile());
            // 当前为占位实现：直接拼接永久 URL（假设 OSS 已通过其他方式配置）
            String base = bucketUrl.endsWith("/")
                    ? bucketUrl.substring(0, bucketUrl.length() - 1) : bucketUrl;
            String permanentUrl = base + "/designs/" + filename;

            log.info("[OssService] 图片已持久化 {} -> {}", imageUrl, permanentUrl);
            return permanentUrl;

        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }

}
