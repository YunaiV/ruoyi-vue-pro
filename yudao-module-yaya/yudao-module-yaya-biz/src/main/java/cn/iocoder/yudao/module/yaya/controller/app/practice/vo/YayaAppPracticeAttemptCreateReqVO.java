package cn.iocoder.yudao.module.yaya.controller.app.practice.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class YayaAppPracticeAttemptCreateReqVO {

    @NotNull
    private Long topicId;
    private String answerText;
    private Integer durationSeconds;
    private Map<String, Object> metadata;

}
