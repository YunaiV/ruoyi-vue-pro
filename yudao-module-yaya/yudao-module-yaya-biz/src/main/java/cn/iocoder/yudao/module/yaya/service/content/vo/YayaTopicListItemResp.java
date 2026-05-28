package cn.iocoder.yudao.module.yaya.service.content.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class YayaTopicListItemResp {

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
    private String reviewStatus;
    private String publishStatus;

}
