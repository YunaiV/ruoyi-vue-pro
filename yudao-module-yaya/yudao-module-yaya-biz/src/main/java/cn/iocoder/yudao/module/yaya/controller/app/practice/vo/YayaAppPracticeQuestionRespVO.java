package cn.iocoder.yudao.module.yaya.controller.app.practice.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class YayaAppPracticeQuestionRespVO {

    private Long id;
    private Long topicId;
    private String questionRole;
    private String promptEn;
    private String promptZh;
    private List<Object> cueBullets;
    private Integer displayOrder;
    private Integer prepareSeconds;
    private Integer answerSeconds;
    private Map<String, Object> metadata;

}
