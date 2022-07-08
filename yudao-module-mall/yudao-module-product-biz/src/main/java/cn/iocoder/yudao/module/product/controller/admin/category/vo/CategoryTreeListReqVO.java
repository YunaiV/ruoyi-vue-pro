package cn.iocoder.yudao.module.product.controller.admin.category.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@ApiModel(value = "管理后台 - 商品分类列表查询 Request VO", description = "参数和 CategoryPageReqVO 是一致的")
public class CategoryTreeListReqVO extends CategoryExportReqVO {

    @ApiModelProperty(value = "分类名称", example = "办公文具")
    private String name;

    @ApiModelProperty(value = "开启状态", example = "0")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "开始创建时间")
    private Date beginCreateTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @ApiModelProperty(value = "结束创建时间")
    private Date endCreateTime;
}
