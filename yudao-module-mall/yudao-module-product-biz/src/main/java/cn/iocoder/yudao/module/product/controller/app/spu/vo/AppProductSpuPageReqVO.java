package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.AssertTrue;

@ApiModel("用户 App - 商品 SPU 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppProductSpuPageReqVO extends PageParam {

    public static final String SORT_FIELD_PRICE = "price";
    public static final String SORT_FIELD_SALES_COUNT = "salesCount";

    @ApiModelProperty(value = "分类编号", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "关键字", example = "好看")
    private String keyword;

    @ApiModelProperty(value = "排序字段", example = "price", notes = "参见 AppSpuPageReqVO.SORT_FIELD_XXX 常量")
    private String sortField;

    @ApiModelProperty(value = "排序方式", example = "true", notes = "true - 升序；false - 降序")
    private Boolean sortAsc;

    @AssertTrue(message = "排序字段不合法")
    @JsonIgnore
    public boolean isSortFieldValid() {
        if (StrUtil.isEmpty(sortField)) {
            return true;
        }
        return StrUtil.equalsAny(sortField, SORT_FIELD_PRICE, SORT_FIELD_SALES_COUNT);
    }

}
