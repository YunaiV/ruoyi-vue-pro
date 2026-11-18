package cn.iocoder.yudao.module.ai.framework.ai.core.video;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 视频异步任务的一次查询结果
 *
 * 状态为 {@link AiVideoTaskStatus#SUCCEEDED} 时，{@link #videos} 至少有一个真实结果；
 * 服务商声称成功但没有结果时，客户端会转换为 {@link AiVideoTaskStatus#FAILED}，错误码为 {@link #ERROR_CODE_NO_OUTPUT}
 *
 * @author 芋道源码
 */
@Data
public class AiVideoTaskResult {

    /**
     * 服务商声称成功，但没有视频结果
     */
    public static final String ERROR_CODE_NO_OUTPUT = "NO_OUTPUT";
    /**
     * 内容审核过滤了全部结果（例如 Veo 的 raiMediaFilteredCount）
     */
    public static final String ERROR_CODE_FILTERED = "FILTERED";

    private String taskId;
    private AiVideoTaskStatus status;
    /**
     * 服务商原始状态，便于排查
     */
    private String rawStatus;
    private List<AiVideoOutput> videos = new ArrayList<>();
    private String errorCode;
    /**
     * 错误信息（已脱敏）
     */
    private String errorMessage;
    private String requestId;
    /**
     * 其它信息，例如分辨率、时长、用量、种子、被过滤数量
     */
    private Map<String, String> metadata = new LinkedHashMap<>();

    public boolean isSucceeded() {
        return status == AiVideoTaskStatus.SUCCEEDED;
    }

    public boolean isFinished() {
        return status != null && status.isFinished();
    }

    /**
     * 第一个视频地址，没有时为 null
     */
    public String getFirstVideoUrl() {
        for (AiVideoOutput video : videos) {
            if (video.getUrl() != null && !video.getUrl().isEmpty()) {
                return video.getUrl();
            }
        }
        return null;
    }

    /**
     * 设置为成功；没有真实结果时改为失败
     */
    public AiVideoTaskResult succeedOrFailIfEmpty() {
        boolean hasContent = false;
        for (AiVideoOutput video : videos) {
            hasContent |= video.hasContent();
        }
        if (hasContent) {
            this.status = AiVideoTaskStatus.SUCCEEDED;
        } else {
            this.status = AiVideoTaskStatus.FAILED;
            this.errorCode = ERROR_CODE_NO_OUTPUT;
            this.errorMessage = "服务商返回成功，但没有视频结果";
        }
        return this;
    }

    public AiVideoTaskResult putMetadata(String key, Object value) {
        if (value != null && !String.valueOf(value).isEmpty()) {
            metadata.put(key, String.valueOf(value));
        }
        return this;
    }

}
