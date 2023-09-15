package cn.iocoder.yudao.module.product.controller.admin.sku;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 商品 SKU")
@RestController
@RequestMapping("/product/sku")
@Validated
public class ProductSkuController {

}
