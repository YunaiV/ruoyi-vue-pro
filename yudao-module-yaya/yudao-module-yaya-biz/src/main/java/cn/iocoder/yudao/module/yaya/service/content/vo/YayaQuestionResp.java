package cn.iocoder.yudao.module.yaya.service.content.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class YayaQuestionResp {

    private Long id;
    private String legacyUuid;
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
