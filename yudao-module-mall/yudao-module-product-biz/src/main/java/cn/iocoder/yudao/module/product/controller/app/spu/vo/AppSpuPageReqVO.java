package cn.iocoder.yudao.module.product.controller.app.spu.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(title = "App - 商品spu分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppSpuPageReqVO extends PageParam {

    @Schema(title = "分类id")
    private Long categoryId;
}
