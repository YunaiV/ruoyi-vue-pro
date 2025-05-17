package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.recrod;

import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 砍价记录的分页项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BargainRecordPageItemRespVO extends BargainRecordBaseVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "22901")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-07-01 23:59:59")
    private LocalDateTime createTime;

    @Schema(description = "帮砍次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer helpCount;

    // ========== 用户相关 ==========

    @Schema(description = "用户昵称", example = "老芋艿")
    private String nickname;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xxx.jpg")
    private String avatar;

    // ========== 活动相关 ==========

    private BargainActivityRespVO activity;

}
