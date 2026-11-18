package cn.iocoder.yudao.module.ai.framework.ai.core.video.veo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Veo（Vertex AI）视频生成的请求
 *
 * 生成方式由素材决定：
 * 1. 文生：只有 prompt
 * 2. 图生：image（首帧）
 * 3. 首尾帧：image + lastFrame
 * 4. 参考图：referenceImages（asset，最多 3 张；veo-3.1-generate-001 只能生成 8 秒）；不能与首尾帧混用
 *
 * 图片只支持 base64 字节或 gs:// 地址（Vertex AI 不拉取 http 地址）
 *
 * @author 芋道源码
 */
@Data
public class VertexVeoVideoRequest {

    /**
     * 图片：bytesBase64Encoded 与 gcsUri 二选一
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {

        private String bytesBase64Encoded;
        private String gcsUri;
        /**
         * image/png、image/jpeg
         */
        private String mimeType;

        @Override
        public String toString() {
            return "Image{bytesLength=" + (bytesBase64Encoded == null ? 0 : bytesBase64Encoded.length())
                    + ", gcsUri=" + gcsUri + ", mimeType=" + mimeType + "}";
        }

    }

    /**
     * 模型：veo-3.1-generate-001、veo-3.1-fast-generate-001、veo-3.1-lite-generate-001（Preview）等
     */
    private String model;
    private String prompt;
    private String negativePrompt;
    private Image image;
    private Image lastFrame;
    private List<Image> referenceImages = new ArrayList<>();

    /**
     * 比例：16:9、9:16
     */
    private String aspectRatio;
    /**
     * 时长（秒）：3.1 系列为 4、6、8
     */
    private Integer durationSeconds;
    /**
     * 分辨率：720p、1080p、4k（4k 只有 veo-3.1-generate-001）
     */
    private String resolution;
    private Boolean generateAudio;
    /**
     * 人物生成策略：allow_adult、dont_allow、allow_all
     */
    private String personGeneration;
    /**
     * 生成数量：1 ~ 4
     */
    private Integer sampleCount;
    /**
     * 随机种子：0 ~ 4294967295
     */
    private Long seed;
    /**
     * 输出的 GCS 目录（gs://bucket/path/）；不指定时视频以 base64 内联在查询结果中
     */
    private String storageUri;
    /**
     * 是否增强提示词
     */
    private Boolean enhancePrompt;

}
