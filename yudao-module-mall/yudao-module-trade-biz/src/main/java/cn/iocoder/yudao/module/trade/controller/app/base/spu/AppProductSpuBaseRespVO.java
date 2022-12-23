package cn.iocoder.yudao.module.trade.controller.app.base.spu;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 商品 SPU 基础 Response VO
 *
 * @author 芋道源码
 */
@Data
public class AppProductSpuBaseRespVO {

    @ApiModelProperty(value = "主键", required = true, example = "1024")
    private Long id;

    @ApiModelProperty(value = "商品 SPU 名字", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = "商品主图地址", example = "https://www.iocoder.cn/xx.png")
    private List<String> picUrls;

}
