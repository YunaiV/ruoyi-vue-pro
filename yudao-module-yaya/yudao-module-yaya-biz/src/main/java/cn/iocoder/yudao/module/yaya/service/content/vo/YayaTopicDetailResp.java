package cn.iocoder.yudao.module.yaya.service.content.vo;

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
public class YayaTopicDetailResp extends YayaTopicListItemResp {

    private String promptEn;
    private String promptZh;
    private Map<String, Object> metadata;
    private List<YayaQuestionResp> questions = new ArrayList<>();

}
