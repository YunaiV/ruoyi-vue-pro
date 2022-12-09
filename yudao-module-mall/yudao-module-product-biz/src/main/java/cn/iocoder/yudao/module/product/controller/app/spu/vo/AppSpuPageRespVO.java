package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(title = "App - 商品spu分页 Request VO")
@Data
public class AppSpuPageRespVO  {

    @Schema(title = "主键", required = true, example = "1")
    private Long id;

    @Schema(title = "商品名称")
    private String name;

    @Schema(title = "卖点", required = true)
    @NotNull(message = "卖点不能为空")
    private String sellPoint;

    @Schema(title = "描述", required = true)
    @NotNull(message = "描述不能为空")
    private String description;

    @Schema(title = "分类id", required = true)
    @NotNull(message = "分类id不能为空")
    private Long categoryId;

    @Schema(title = "商品主图地址,* 数组，以逗号分隔,最多上传15张", required = true)
    @NotNull(message = "商品主图地址,* 数组，以逗号分隔,最多上传15张不能为空")
    private List<String> picUrls;

    @Schema(title = "排序字段", required = true)
    @NotNull(message = "排序字段不能为空")
    private Integer sort;

    @Schema(title = "点赞初始人数")
    private Integer likeCount;

    @Schema(title = "价格 单位使用：分")
    private Integer price;

    @Schema(title = "库存数量")
    private Integer quantity;

    @Schema(title = "上下架状态： 0 上架（开启） 1 下架（禁用）")
    private Integer status;

}
