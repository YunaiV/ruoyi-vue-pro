package cn.iocoder.yudao.module.product.controller.admin.sku.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@ApiModel("管理后台 - 商品sku分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductSkuPageReqVO extends PageParam {

    @ApiModelProperty(value = "spu编号")
    private Long spuId;

    @ApiModelProperty(value = "规格值数组-json格式， [{propertId: , valueId: }, {propertId: , valueId: }]")
    private String properties;

    @ApiModelProperty(value = "销售价格，单位：分")
    private Integer price;

    @ApiModelProperty(value = "原价， 单位： 分")
    private Integer originalPrice;

    @ApiModelProperty(value = "成本价，单位： 分")
    private Integer costPrice;

    @ApiModelProperty(value = "条形码")
    private String barCode;

    @ApiModelProperty(value = "图片地址")
    private String picUrl;

    @ApiModelProperty(value = "状态： 0-正常 1-禁用")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;

}
