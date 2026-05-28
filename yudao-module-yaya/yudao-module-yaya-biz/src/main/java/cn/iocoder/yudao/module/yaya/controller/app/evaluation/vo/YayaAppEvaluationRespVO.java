package cn.iocoder.yudao.module.yaya.controller.app.evaluation.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class YayaAppEvaluationRespVO {

    private Long id;
    private Long recordingId;
    private Long topicId;
    private Long aiTaskId;
    private String status;
    private String provider;
    private String model;
    private Double scoreOverall;
    private Map<String, Object> scores;
    private Map<String, Object> report;
    private String polishPackStatus;

}
