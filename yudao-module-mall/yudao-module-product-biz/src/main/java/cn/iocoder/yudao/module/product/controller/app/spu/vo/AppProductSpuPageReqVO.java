package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.AssertTrue;
import java.util.List;

@Schema(description = "用户 App - 商品 SPU 分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppProductSpuPageReqVO extends PageParam {

    public static final String SORT_FIELD_PRICE = "price";
    public static final String SORT_FIELD_SALES_COUNT = "salesCount";
    public static final String SORT_FIELD_CREATE_TIME = "createTime";

    @Schema(description = "商品 SPU 编号数组", example = "1,3,5")
    private List<Long> ids;

    @Schema(description = "分类编号", example = "1")
    private Long categoryId;

    @Schema(description = "分类编号数组", example = "1,2,3")
    private List<Long> categoryIds;

    @Schema(description = "关键字", example = "好看")
    private String keyword;

    @Schema(description = "排序字段", example = "price") // 参见 AppProductSpuPageReqVO.SORT_FIELD_XXX 常量
    private String sortField;

    @Schema(description = "排序方式", example = "true")
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
