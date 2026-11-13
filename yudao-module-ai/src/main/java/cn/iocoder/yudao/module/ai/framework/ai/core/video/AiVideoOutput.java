package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import lombok.Data;

/**
 * AI 视频的一个生成结果
 *
 * url 通常是带签名、有有效期的临时地址，业务应尽快转存；{@link #toString()} 中会隐藏地址参数
 *
 * @author 芋道源码
 */
@Data
public class AiVideoOutput {

    /**
     * 视频地址（http / https，通常为限时链接）
     */
    private String url;
    /**
     * 带水印的视频地址（可灵等返回）
     */
    private String watermarkUrl;
    /**
     * Google Cloud Storage 地址（Veo 指定 storageUri 时返回），下载需要 GCP 凭证
     */
    private String gcsUri;
    /**
     * 视频字节（Veo 未指定 storageUri 时内联返回）
     */
    private byte[] data;
    /**
     * MIME 类型，例如 video/mp4
     */
    private String mimeType;
    /**
     * 时长（秒）
     */
    private Double durationSeconds;
    private Integer width;
    private Integer height;
    /**
     * 尾帧图片地址（豆包 return_last_frame）
     */
    private String lastFrameUrl;

    /**
     * 是否有真实的视频内容
     */
    public boolean hasContent() {
        return (url != null && !url.isEmpty()) || (gcsUri != null && !gcsUri.isEmpty())
                || (data != null && data.length > 0);
    }

    @Override
    public String toString() {
        return "AiVideoOutput{url=" + AiVideoUtils.maskUrl(url)
                + ", watermarkUrl=" + AiVideoUtils.maskUrl(watermarkUrl)
                + ", gcsUri=" + gcsUri
                + ", dataLength=" + (data == null ? 0 : data.length)
                + ", mimeType=" + mimeType + ", durationSeconds=" + durationSeconds
                + ", width=" + width + ", height=" + height
                + ", lastFrameUrl=" + AiVideoUtils.maskUrl(lastFrameUrl) + "}";
    }

}
