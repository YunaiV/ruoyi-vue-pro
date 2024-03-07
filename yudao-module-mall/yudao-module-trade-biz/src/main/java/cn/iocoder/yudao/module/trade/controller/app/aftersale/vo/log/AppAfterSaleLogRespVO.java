package cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - App 交易售后日志 Response VO")
@Data
public class AppAfterSaleLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20669")
    private Long id;

    @Schema(description = "操作明细", requiredMode = Schema.RequiredMode.REQUIRED, example = "维权完成，退款金额：¥37776.00")
    private String content;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
