package cn.iocoder.yudao.module.deepay.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * CDN 图片同步服务实现。
 *
 * <p>当前为"前缀替换"策略：将原始 URL 的 host 替换为 CDN 域名。
 * 若未配置 {@code deepay.cdn.base-url}，则原样透传（降级），不影响主流程。</p>
 *
 * <p>配置项（application.yml）：
 * <pre>
 * deepay:
 *   cdn:
 *     base-url: https://cdn.deepay.example.com   # 留空则不替换
 * </pre>
 * </p>
 */
@Slf4j
@Service
public class CdnServiceImpl implements CdnService {

    @Value("${deepay.cdn.base-url:}")
    private String cdnBaseUrl;

    @Override
    public String syncToCdn(String originalUrl) {
        if (!StringUtils.hasText(originalUrl)) {
            return originalUrl;
        }
        if (!StringUtils.hasText(cdnBaseUrl)) {
            log.debug("[CdnService] CDN 未配置，原样透传 url={}", originalUrl);
            return originalUrl;
        }
        try {
            // 简单策略：将 deepay-assets.example.com 替换为 CDN 域名前缀
            String cdnUrl = rewriteHostToCdn(originalUrl);
            log.info("[CdnService] 图片已同步至 CDN: {} -> {}", originalUrl, cdnUrl);
            return cdnUrl;
        } catch (Exception e) {
            log.warn("[CdnService] CDN 同步失败，降级返回原始 URL url={}", originalUrl, e);
            return originalUrl;
        }
    }

    @Override
    public List<String> syncAllToCdn(List<String> originalUrls) {
        List<String> result = new ArrayList<>(originalUrls.size());
        for (String url : originalUrls) {
            result.add(syncToCdn(url));
        }
        return result;
    }

    /**
     * URL 重写：将路径部分追加到 CDN base URL 上。
     * 例：https://deepay-assets.example.com/path/img.jpg
     *  → https://cdn.deepay.example.com/path/img.jpg
     */
    private String rewriteHostToCdn(String originalUrl) {
        // 截取 '/' 之后的路径部分（从第三个 '/' 开始）
        int pathStart = originalUrl.indexOf('/', originalUrl.indexOf("//") + 2);
        if (pathStart == -1) {
            return cdnBaseUrl;
        }
        String path = originalUrl.substring(pathStart);
        String base = cdnBaseUrl.endsWith("/") ? cdnBaseUrl.substring(0, cdnBaseUrl.length() - 1) : cdnBaseUrl;
        return base + path;
    }

}
