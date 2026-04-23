package cn.iocoder.yudao.module.deepay.service;

import java.util.List;

/**
 * 图片 OSS 持久化服务（STEP 24）。
 *
 * <p>AI 返回的图片 URL 是临时地址（可能过期），
 * 本服务将图片下载并上传到持久化存储（阿里云 OSS / S3 / Cloudflare R2），
 * 返回永久可访问的 URL。</p>
 */
public interface OssService {

    /**
     * 下载单张图片并上传到 OSS，返回永久 URL。
     * 失败时返回原始 URL（降级，不影响主流程）。
     *
     * @param imageUrl AI 返回的临时图片 URL
     * @return OSS 永久 URL 或原始 URL（降级）
     */
    String persist(String imageUrl);

    /**
     * 批量持久化，返回与入参等长的 URL 列表（顺序一致）。
     */
    List<String> persistAll(List<String> imageUrls);

}
