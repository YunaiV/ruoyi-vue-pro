package cn.iocoder.yudao.module.yaya.controller.app.practice.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YayaAppPracticeTopicRespVO {

    private Long id;
    private Long seasonId;
    private Integer part;
    private String stableKey;
    private Integer topicNo;
    private String titleEn;
    private String titleZh;
    private String topicType;
    private String category;
    private Integer displayOrder;
    private Boolean favorite;
    private Boolean practiced;

}
