package cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class YayaAppEvaluationCreateReqVO {

    @NotNull(message = "录音编号不能为空")
    private Long recordingId;
    private Long topicId;
    private Map<String, Object> options;

}
