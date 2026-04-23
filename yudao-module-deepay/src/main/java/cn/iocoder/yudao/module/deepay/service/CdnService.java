package cn.iocoder.yudao.module.deepay.service;

import java.util.List;

/**
 * CDN 图片同步服务。
 *
 * <p>将 FLUX 生成的原始图片 URL 同步（或重写）为 CDN 地址，防止 H5 裂图。
 * 未配置 CDN 时直接返回原始 URL（降级透传）。</p>
 */
public interface CdnService {

    /**
     * 将单张图片同步到 CDN 并返回 CDN 地址。
     *
     * @param originalUrl 原始图片 URL
     * @return CDN 地址；CDN 不可用时返回 originalUrl
     */
    String syncToCdn(String originalUrl);

    /**
     * 批量同步图片，顺序与入参列表对应。
     *
     * @param originalUrls 原始图片 URL 列表
     * @return CDN 地址列表；对应位置不可用时保留原始 URL
     */
    List<String> syncAllToCdn(List<String> originalUrls);

}
