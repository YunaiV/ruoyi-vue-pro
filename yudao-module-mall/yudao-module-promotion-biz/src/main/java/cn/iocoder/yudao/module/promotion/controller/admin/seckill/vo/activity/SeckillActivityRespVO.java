package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 秒杀活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityRespVO extends SeckillActivityBaseVO {

    @Schema(description = "秒杀活动id", required = true, example = "1")
    private Long id;

    @Schema(description = "付款订单数", required = true, example = "1")
    private Integer orderCount;

    @Schema(description = "付款人数", required = true, example = "1")
    private Integer userCount;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "秒杀时段id", required = true, example = "1,3")
    private List<Long> timeIds;

    @Schema(description = "排序", required = true, example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "限时秒杀活动")
    private String remark;

    @Schema(description = "活动状态", example = "进行中")
    private Integer status;

}
