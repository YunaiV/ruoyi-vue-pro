package cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 佣金记录 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BrokerageRecordRespVO extends BrokerageRecordBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28896")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;


    // ========== 用户信息 ==========

    @Schema(description = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String userAvatar;
    @Schema(description = "用户昵称", example = "李四")
    private String userNickname;


    // ========== 来源用户信息 ==========

    @Schema(description = "来源用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String sourceUserAvatar;
    @Schema(description = "来源用户昵称", example = "李四")
    private String sourceUserNickname;
}
