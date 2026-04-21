package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import cn.iocoder.yudao.module.deepay.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Deepay 商品展示接口。
 *
 * <p>
 * <ul>
 *   <li>GET /product/{chainCode}      —— Thymeleaf 渲染页面，供用户在浏览器直接访问</li>
 *   <li>GET /api/product/{chainCode}  —— JSON 数据接口，供未来前端/小程序调用</li>
 * </ul>
 * </p>
 */
@Tag(name = "Deepay - 商品展示")
@Controller
public class DeepayProductPageController {

    @Resource
    private ProductService productService;

    // ----------------------------------------------------------------
    // 1. 页面接口：GET /product/{chainCode}  →  Thymeleaf HTML
    // ----------------------------------------------------------------

    @GetMapping("/product/{chainCode}")
    public String productPage(@PathVariable String chainCode, Model model) {
        DeepayStyleChainDO product = productService.getByChainCode(chainCode);

        if (product == null) {
            model.addAttribute("title", "商品不存在");
            model.addAttribute("chainCode", chainCode);
            model.addAttribute("image", null);
            model.addAttribute("description", "该链码对应的商品不存在，请确认链接是否正确。");
            // price 设为 null，模板中通过 th:if 判断不渲染价格区域
            model.addAttribute("price", null);
            return "product";
        }

        model.addAttribute("chainCode", product.getChainCode());
        model.addAttribute("title", product.getTitle());
        model.addAttribute("image", product.getImageUrl());
        model.addAttribute("description", product.getDescription());
        model.addAttribute("price", product.getPrice());
        return "product";
    }

    // ----------------------------------------------------------------
    // 2. JSON 接口：GET /api/product/{chainCode}  →  JSON
    // ----------------------------------------------------------------

    @GetMapping("/api/product/{chainCode}")
    @ResponseBody
    @Operation(summary = "根据链码获取商品 JSON 信息")
    public CommonResult<Map<String, Object>> productJson(@PathVariable String chainCode) {
        DeepayStyleChainDO product = productService.getByChainCode(chainCode);

        if (product == null) {
            return CommonResult.error(404, "商品不存在：" + chainCode);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("chainCode", product.getChainCode());
        resp.put("image", product.getImageUrl());
        resp.put("title", product.getTitle());
        resp.put("description", product.getDescription());
        resp.put("price", product.getPrice());
        resp.put("link", "https://deepay.link/" + product.getChainCode());
        return success(resp);
    }

}

