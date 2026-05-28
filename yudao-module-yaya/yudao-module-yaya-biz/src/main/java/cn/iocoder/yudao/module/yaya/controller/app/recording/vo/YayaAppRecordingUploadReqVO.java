package cn.iocoder.yudao.module.yaya.controller.app.recording.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Data
@Accessors(chain = true)
public class YayaAppRecordingUploadReqVO {

    @NotNull(message = "录音文件不能为空")
    private MultipartFile file;
    private Long topicId;
    private Long questionId;
    private Long attemptId;
    private Double durationSeconds;

}
