package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 秒杀活动更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityUpdateReqVO extends SeckillActivityBaseVO {

    @ApiModelProperty(value = "秒杀活动编号", required = true, example = "224")
    @NotNull(message = "秒杀活动编号不能为空")
    private Long id;

    @ApiModelProperty(value = "备注", example = "限时秒杀活动")
    private String remark;

    @ApiModelProperty(value = "排序", required = true)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @ApiModelProperty(value = "秒杀时段id", required = true)
    @NotEmpty(message = "秒杀时段id不能为空")
    private List<Long> timeIds;

    /**
     * 商品列表
     */
    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<Product> products;

}
