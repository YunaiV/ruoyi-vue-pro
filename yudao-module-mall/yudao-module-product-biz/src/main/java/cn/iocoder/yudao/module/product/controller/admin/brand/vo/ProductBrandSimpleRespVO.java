package cn.iocoder.yudao.module.product.controller.admin.brand.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO @puhui999：class 类的开始和结束，都要有一个空行哈。
@Schema(description = "管理后台 - 品牌精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBrandSimpleRespVO {
    @Schema(description = "品牌编号", required = true, example = "1024")
    private Long id;
    @Schema(description = "品牌名称", required = true, example = "芋道")
    private String name;
}
