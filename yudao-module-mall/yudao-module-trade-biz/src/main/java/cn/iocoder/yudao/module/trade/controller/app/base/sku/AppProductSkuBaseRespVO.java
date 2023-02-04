package cn.iocoder.yudao.module.trade.controller.app.base.sku;

import cn.iocoder.yudao.module.trade.controller.app.base.property.AppProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 商品 SKU 基础 Response VO
 *
 * @author 芋道源码
 */
@Data
public class AppProductSkuBaseRespVO {

    @Schema(description = "主键", required = true, example = "1024")
    private Long id;

    @Schema(description = "商品 SKU 名字", required = true, example = "芋道")
    private String name;

    @Schema(description = "图片地址", example = "https://www.iocoder.cn/xx.png")
    private String picUrl;

    @Schema(description = "库存", required = true, example = "1")
    private Integer stock;

    /**
     * 属性数组
     */
    private List<AppProductPropertyValueDetailRespVO> properties;

}
