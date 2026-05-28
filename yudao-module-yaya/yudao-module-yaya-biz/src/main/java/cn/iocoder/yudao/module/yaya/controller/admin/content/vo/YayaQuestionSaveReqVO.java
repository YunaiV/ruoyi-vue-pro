package cn.iocoder.yudao.module.yaya.controller.admin.content.vo;

import cn.iocoder.yudao.module.yaya.service.content.vo.YayaQuestionSaveReq;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class YayaQuestionSaveReqVO {

    private String legacyUuid;
    private String questionRole;
    private String promptEn;
    private String promptZh;
    private List<Object> cueBullets;
    private Integer displayOrder;
    private Integer prepareSeconds;
    private Integer answerSeconds;
    private Map<String, Object> metadata;

    public YayaQuestionSaveReq toSaveReq() {
        return new YayaQuestionSaveReq()
                .setLegacyUuid(legacyUuid)
                .setQuestionRole(questionRole)
                .setPromptEn(promptEn)
                .setPromptZh(promptZh)
                .setCueBullets(cueBullets)
                .setDisplayOrder(displayOrder)
                .setPrepareSeconds(prepareSeconds)
                .setAnswerSeconds(answerSeconds)
                .setMetadata(metadata);
    }

}
