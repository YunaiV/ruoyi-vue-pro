package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ApiModel("管理后台 - 商品 SPU 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuPageReqVO extends PageParam {

    @ApiModelProperty(value = "商品名称", example = "yutou")
    private String name;

    @ApiModelProperty(value = "商品编码", example = "yudaoyuanma")
    private String code;

    @ApiModelProperty(value = "分类id", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "商品品牌编号", example = "1")
    private Long brandId;

    @ApiModelProperty(value = "上下架状态", example = "1", notes = "参见 ProductSpuStatusEnum 枚举值")
    private Integer status;

    @ApiModelProperty(value = "销量最小值", example = "1")
    private Integer salesCountMin;

    @ApiModelProperty(value = "销量最大值", example = "1024")
    private Integer salesCountMax;

    @ApiModelProperty(value = "市场价最小值", example = "1")
    private Integer marketPriceMin;

    @ApiModelProperty(value = "市场价最大值", example = "1024")
    private Integer marketPriceMax;

    @ApiModelProperty(value = "是否库存告警", example = "true")
    private Boolean alarmStock;

}
