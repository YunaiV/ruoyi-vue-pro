package cn.iocoder.yudao.module.yaya.controller.app.practice.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class YayaAppPracticeTopicDetailRespVO extends YayaAppPracticeTopicRespVO {

    private String promptEn;
    private String promptZh;
    private Map<String, Object> metadata;
    private List<Long> completedQuestionIds = new ArrayList<>();
    private List<YayaAppPracticeQuestionRespVO> questions = new ArrayList<>();

}
