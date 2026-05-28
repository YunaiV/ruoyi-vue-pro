package cn.iocoder.yudao.module.yaya.controller.admin.content.vo;

import cn.iocoder.yudao.module.yaya.service.content.vo.YayaTopicCreateReq;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class YayaTopicSaveReqVO {

    private String legacyUuid;

    @NotNull(message = "seasonId 不能为空")
    private Long seasonId;

    private Long sourceSnapshotId;

    @NotNull(message = "part 不能为空")
    private Integer part;

    @NotBlank(message = "stableKey 不能为空")
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

    public YayaTopicCreateReq toCreateReq() {
        return new YayaTopicCreateReq()
                .setLegacyUuid(legacyUuid)
                .setSeasonId(seasonId)
                .setSourceSnapshotId(sourceSnapshotId)
                .setPart(part)
                .setStableKey(stableKey)
                .setTopicNo(topicNo)
                .setTitleEn(titleEn)
                .setTitleZh(titleZh)
                .setTopicType(topicType)
                .setCategory(category)
                .setPromptEn(promptEn)
                .setPromptZh(promptZh)
                .setDisplayOrder(displayOrder)
                .setReviewStatus(reviewStatus)
                .setPublishStatus(publishStatus)
                .setMetadata(metadata);
    }

}
