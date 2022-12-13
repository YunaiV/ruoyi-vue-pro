package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@ApiModel("管理后台 - 秒杀活动 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityRespVO extends SeckillActivityBaseVO {

    @ApiModelProperty(value = "秒杀活动id", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "付款订单数", required = true, example = "1")
    private Integer orderCount;

    @ApiModelProperty(value = "付款人数", required = true, example = "1")
    private Integer userCount;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "秒杀时段id", required = true, example = "1,3")
    private List<Long> timeIds;

    @ApiModelProperty(value = "排序", required = true, example = "1")
    private Integer sort;

    @ApiModelProperty(value = "备注", example = "限时秒杀活动")
    private String remark;

    @ApiModelProperty(value = "活动状态", example = "进行中")
    private Integer status;

}
