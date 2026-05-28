package cn.iocoder.yudao.module.yaya.controller.app.recording.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YayaAppRecordingRespVO {

    private Long id;
    private Long topicId;
    private Long questionId;
    private Long attemptId;
    private String storagePath;
    private String mimeType;
    private Long sizeBytes;
    private Double durationSeconds;
    private String status;

}
