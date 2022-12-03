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

// TODO halfninety：检查下所有的 VO，需要保证 example 都写了哈。
@ApiModel("管理后台 - 秒杀活动创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityCreateReqVO extends SeckillActivityBaseVO {

    @ApiModelProperty(value = "备注", example = "限时秒杀活动")
    private String remark;

    @ApiModelProperty(value = "排序", required = true)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @ApiModelProperty(value = "秒杀时段id", required = true)
    @NotEmpty(message = "参与场次不能为空")
    private List<Long> timeIds;

    /**
     * 商品列表
     */
    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<Product> products;

}
