package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.agent.TrendAgent;
import cn.iocoder.yudao.module.deepay.orchestrator.ChainOrchestrator;
import cn.iocoder.yudao.module.deepay.orchestrator.ProductionOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Deepay 商品生成接口。
 *
 * <pre>
 * POST /deepay/run            —— 完整流水线，返回 {image, price, chainCode}
 * POST /deepay/trend          —— 仅运行 TrendAgent，返回参考图列表
 * POST /api/create-product    —— 轻量链码接口（向后兼容）
 * </pre>
 */
@Tag(name = "Deepay - 商品生成")
@RestController
@Validated
public class DeepayProductController {

    @Resource private ProductionOrchestrator productionOrchestrator;
    @Resource private ChainOrchestrator      chainOrchestrator;
    @Resource private TrendAgent             trendAgent;

    // ----------------------------------------------------------------
    // POST /deepay/run  —— 完整流水线
    // ----------------------------------------------------------------

    @PostMapping("/deepay/run")
    @Operation(summary = "完整流水线（keyword → image + price + chainCode）")
    public CommonResult<Map<String, Object>> run(@Valid @RequestBody KeywordReqVO req) {
        Context ctx = new Context();
        ctx.keyword = req.getKeyword();
        productionOrchestrator.run(ctx);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("chainCode",   ctx.chainCode);
        resp.put("image",       ctx.selectedImage);
        resp.put("price",       ctx.price);
        resp.put("title",       ctx.title);
        resp.put("stock",       ctx.stock);
        resp.put("published",   ctx.published);
        resp.put("productId",   ctx.productId);
        resp.put("productUrl",  ctx.productUrl);
        resp.put("paymentId",   ctx.paymentId);
        resp.put("orderId",     ctx.orderId);
        resp.put("patternFile", ctx.patternFile);
        resp.put("techPackUrl", ctx.techPackUrl);
        resp.put("action",      ctx.action);
        resp.put("report",      ctx.analyticsReport);
        return success(resp);
    }

    // ----------------------------------------------------------------
    // POST /deepay/trend  —— 仅找爆款参考图
    // ----------------------------------------------------------------

    @PostMapping("/deepay/trend")
    @Operation(summary = "TrendAgent：根据关键词返回爆款参考图列表")
    public CommonResult<Map<String, Object>> trend(@Valid @RequestBody KeywordReqVO req) {
        Context ctx = new Context();
        ctx.keyword = req.getKeyword();
        trendAgent.run(ctx);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("keyword",         ctx.keyword);
        resp.put("referenceImages", ctx.referenceImages);
        return success(resp);
    }

    // ----------------------------------------------------------------
    // POST /api/create-product  —— 轻量版（向后兼容）
    // ----------------------------------------------------------------

    @PostMapping("/api/create-product")
    @Operation(summary = "轻量版：生成链码 + IBAN（向后兼容）")
    public CommonResult<Map<String, String>> createProduct(@Valid @RequestBody PromptReqVO req) {
        Context ctx = chainOrchestrator.run(req.getPrompt());

        Map<String, String> resp = new LinkedHashMap<>();
        resp.put("chainCode", ctx.chainCode);
        resp.put("image",     ctx.selectedImage);
        resp.put("iban",      ctx.iban);
        resp.put("link",      "https://deepay.link/" + ctx.chainCode);
        return success(resp);
    }

    // ----------------------------------------------------------------
    // Request VOs
    // ----------------------------------------------------------------

    public static class KeywordReqVO {
        @NotBlank(message = "keyword 不能为空")
        private String keyword;

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
    }

    public static class PromptReqVO {
        @NotBlank(message = "prompt 不能为空")
        private String prompt;

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
    }

}


