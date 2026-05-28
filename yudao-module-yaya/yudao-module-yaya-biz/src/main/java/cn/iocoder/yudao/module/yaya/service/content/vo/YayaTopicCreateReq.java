package cn.iocoder.yudao.module.yaya.service.content.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class YayaTopicCreateReq {

    private String legacyUuid;
    private Long seasonId;
    private Long sourceSnapshotId;
    private Integer part;
    private String stableKey;
    private Integer topicNo;
    private String titleEn;
    private String titleZh;
    private String topicType;
    private String category;
    private String promptEn;
    private String promptZh;
    private Integer displayOrder;
    private String reviewStatus;
    private String publishStatus;
    private Map<String, Object> metadata;

}
