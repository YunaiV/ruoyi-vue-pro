package cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 秒杀活动更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillActivityUpdateReqVO extends SeckillActivityBaseVO {

    @Schema(description = "秒杀活动编号", required = true, example = "224")
    @NotNull(message = "秒杀活动编号不能为空")
    private Long id;

    @Schema(description = "备注", example = "限时秒杀活动")
    private String remark;

    @Schema(description = "排序", required = true, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "秒杀时段id", required = true, example = "1,3")
    @NotEmpty(message = "秒杀时段id不能为空")
    private List<Long> timeIds;

    /**
     * 商品列表
     */
    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<Product> products;

}
