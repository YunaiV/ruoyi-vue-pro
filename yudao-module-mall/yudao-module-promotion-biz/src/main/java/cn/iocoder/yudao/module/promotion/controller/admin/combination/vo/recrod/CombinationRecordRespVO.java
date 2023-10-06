package cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.recrod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 拼团记录 Response VO")
@Data
public class CombinationRecordRespVO {

    @Schema(description = "拼团记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "拼团活动编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long activityId;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "花开富贵")
    private String nickname;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String avatar;

    @Schema(description = "团长编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long headId;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime expireTime;

    @Schema(description = "可参团人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer userSize;

    @Schema(description = "已参团人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer userCount;

    @Schema(description = "拼团状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "商品名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "我是大黄豆")
    private String spuName;

    @Schema(description = "商品图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/1.png")
    private String picUrl;

    @Schema(description = "是否虚拟成团", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean virtualGroup;

    @Schema(description = "开始时间 (订单付款后开始的时间)", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "结束时间（成团时间/失败时间）", requiredMode = Schema.RequiredMode.REQUIRED)
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

}
