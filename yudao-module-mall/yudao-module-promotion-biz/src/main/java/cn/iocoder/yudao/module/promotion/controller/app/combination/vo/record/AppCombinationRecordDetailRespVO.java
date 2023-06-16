package cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 拼团记录详细 Response VO")
@Data
public class AppCombinationRecordDetailRespVO {

    @Schema(description = "团长的拼团记录", required = true)
    private AppCombinationRecordRespVO headRecord;

    @Schema(description = "成员的拼团记录", required = true)
    private List<AppCombinationRecordRespVO> memberRecords;

    @Schema(description = "当前用户参团记录对应的订单编号", required = true, example = "1024") // 如果没参团，返回 null
    private Long orderId;

}
