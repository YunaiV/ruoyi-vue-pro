package cn.iocoder.yudao.module.activity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动 DO。字段定义见 docs/5-数据库设计.md §3.1。
 */
@TableName("t_activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDO implements Serializable {

    @TableId
    private Long id;

    /** 6 位活动短码，分享链接用，全局唯一 */
    private String shortCode;
    private String title;
    private Long creatorId;
    private Long clubId;

    private String venueName;
    private String venueAddress;
    private BigDecimal venueLat;
    private BigDecimal venueLng;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** 场地数 */
    private Integer courtCount;
    /** 计划人数 */
    private Integer plannedCount;
    /** 已报名 */
    private Integer currentCount;

    /** MEN_DOUBLES/WOMEN_DOUBLES/MIXED/FREE */
    private String mode;
    /** FIXED/ROTATE */
    private String rotation;
    /** 单场分钟 */
    private Integer matchDuration;

    private BigDecimal aaAmount;
    private String remark;

    private Integer isPublic;
    private Integer visibleRadiusKm;
    private Integer acceptStrangers;
    private Integer needReview;
    /** JSON 字符串：["C1","C2","B1"] */
    private String limitLevels;
    private Integer limitGender;
    private Integer limitStrangersCount;
    private Integer acceptedStrangersCount;

    /** RECRUITING/MATCHED/PLAYING/FINISHED/CANCELLED */
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
