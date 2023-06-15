package cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Schema(description = "用户 App - 拼团记录精简 Response VO")
@Data
public class AppCombinationRecordSimpleRespVO {

    @Schema(description = "拼团记录编号", required = true, example = "1024")
    private Long id;

    @Schema(description = "用户昵称", required = true, example = "1024")
    private String nickname;

    @Schema(description = "用户头像", required = true, example = "1024")
    private String avatar;

    @Schema(description = "过期时间", required = true)
    private Date expireTime;

    @Schema(description = "可参团人数", required = true, example = "10")
    private Integer userSize;

    @Schema(description = "已参团人数", required = true, example = "5")
    private Integer userCount;

}
