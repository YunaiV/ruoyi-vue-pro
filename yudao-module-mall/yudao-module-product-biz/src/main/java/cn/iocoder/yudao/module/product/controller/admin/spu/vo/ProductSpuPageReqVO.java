package cn.iocoder.yudao.module.product.controller.admin.spu.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 商品 SPU 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSpuPageReqVO extends PageParam {

    @ApiModelProperty(value = "商品名称")
    private String name;

    @ApiModelProperty(value = "商品编码", example = "yudaoyuanma")
    private String code;

    @ApiModelProperty(value = "分类id")
    private Long categoryId;

    @ApiModelProperty(value = "商品品牌编号", example = "1")
    private Long brandId;

    @ApiModelProperty(value = "上下架状态： 0 上架（开启） 1 下架（禁用）")
    private Integer status;

    @ApiModelProperty(value = "销量最小值", example = "1")
    private Integer salesCountMin;

    @ApiModelProperty(value = "销量最大值", example = "1024")
    private Integer salesCountMax;

    @ApiModelProperty(value = "市场价最小值", example = "1")
    private Integer marketPriceMin;

    @ApiModelProperty(value = "市场价最大值", example = "1024")
    private Integer marketPriceMax;

    @ApiModelProperty(value = "tab 状态 null 全部， 0：销售中（上架） 1：仓库中（下架） 2：预警中", example = "1")
    private Integer tabStatus;

}
