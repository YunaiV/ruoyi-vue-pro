package cn.iocoder.yudao.module.activity.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 活动 Response VO")
@Data
public class AppActivityRespVO {

    private Long id;
    private String shortCode;
    private String title;
    private Long creatorId;

    private String venueName;
    private String venueAddress;
    private BigDecimal venueLat;
    private BigDecimal venueLng;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer courtCount;
    private Integer plannedCount;
    private Integer currentCount;

    private String mode;
    private String rotation;
    private Integer matchDuration;

    private BigDecimal aaAmount;
    private String remark;

    private String status;
    private LocalDateTime createdAt;

    /** 详情接口才填，列表接口不填 */
    private List<MemberItem> members;

    @Data
    public static class MemberItem {
        private Long userId;
        private String placeholderName;
        private Integer gender;
        private String levelAtJoin;
        private String status;
        private String joinSource;
        private LocalDateTime joinedAt;
    }

}
