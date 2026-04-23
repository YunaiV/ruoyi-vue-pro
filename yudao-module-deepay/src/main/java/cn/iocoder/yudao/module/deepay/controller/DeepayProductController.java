package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.agent.TrendAgent;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.orchestrator.ChainOrchestrator;
import cn.iocoder.yudao.module.deepay.orchestrator.ProductionOrchestrator;
import cn.iocoder.yudao.module.deepay.service.FxRateService;
import cn.iocoder.yudao.module.deepay.service.UserCurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Deepay 商品生成 + 查询接口。
 *
 * <pre>
 * POST /deepay/run              —— 完整流水线，返回 {image, price, paymentUrl …}
 * POST /deepay/trend            —— 仅运行 TrendAgent，返回参考图列表
 * GET  /deepay/product/{chain}  —— 查询商品，自动按 IP 返回本地化价格
 * POST /api/create-product      —— 轻量链码接口（向后兼容）
 * </pre>
 */
@Tag(name = "Deepay - 商品生成")
@RestController
@Validated
public class DeepayProductController {

    @Resource private ProductionOrchestrator productionOrchestrator;
    @Resource private ChainOrchestrator      chainOrchestrator;
    @Resource private TrendAgent             trendAgent;
    @Resource private DeepayProductMapper    productMapper;
    @Resource private FxRateService          fxRateService;
    @Resource private UserCurrencyService    userCurrencyService;

    // ----------------------------------------------------------------
    // POST /deepay/run  —— 完整流水线
    // ----------------------------------------------------------------

    @PostMapping("/deepay/run")
    @Operation(summary = "完整流水线（Phase 10：keyword/画像 → AI生成 → 上架 → 支付链接）")
    public CommonResult<Map<String, Object>> run(@Valid @RequestBody RunReqVO req,
                                                  HttpServletRequest httpReq) {
        Context ctx = new Context();
        ctx.keyword    = req.getKeyword();
        ctx.customerId = req.getCustomerId();
        ctx.category   = req.getCategory();
        ctx.style      = req.getStyle();
        ctx.crowd      = req.getCrowd();
        ctx.market     = req.getMarket();
        ctx.priceLevel = req.getPriceLevel();
        ctx.designSelectMode = req.getDesignSelectMode() != null ? req.getDesignSelectMode() : "AI";

        // IP → 货币识别（后端统一，不信任前端）
        ctx.userCurrency = userCurrencyService.detect(httpReq);

        if (ctx.keyword == null && ctx.category != null) {
            ctx.keyword = ctx.category;
        }

        productionOrchestrator.run(ctx);

        // 展示价（以用户货币表示）
        BigDecimal displayPrice = ctx.displayPrice != null ? ctx.displayPrice :
                (ctx.basePrice != null ? fxRateService.convert(ctx.basePrice, ctx.userCurrency) :
                        ctx.price);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("chainCode",     ctx.chainCode);
        resp.put("image",         ctx.selectedImage);
        // 价格
        resp.put("basePrice",     ctx.basePrice);         // EUR 基准价（后台用）
        resp.put("displayPrice",  displayPrice);          // 用户货币展示价
        resp.put("currency",      ctx.userCurrency);
        resp.put("price",         ctx.price);             // 向后兼容
        // 商品
        resp.put("title",         ctx.title);
        resp.put("description",   ctx.description);
        resp.put("stock",         ctx.stock);
        resp.put("published",     ctx.published);
        resp.put("productId",     ctx.productId);
        resp.put("productUrl",    ctx.productUrl);
        resp.put("channels",      ctx.channels);
        // 支付
        resp.put("paymentId",     ctx.paymentId);
        resp.put("orderId",       ctx.orderId);
        // Phase 9
        resp.put("patternFile",   ctx.patternFile);
        resp.put("techPackUrl",   ctx.techPackUrl);
        resp.put("finalDesign",   ctx.finalDesign);
        // 决策
        resp.put("action",        ctx.action);
        resp.put("report",        ctx.analyticsReport);
        // 问答
        resp.put("pendingQuestion", ctx.pendingQuestion);
        resp.put("pendingField",    ctx.pendingField);
        resp.put("selectionImages", ctx.selectionImages);
        resp.put("styleCombos",     ctx.styleCombos);
        return success(resp);
    }

    // ----------------------------------------------------------------
    // GET /deepay/product/{chainCode}  —— 商品详情 + 本地化价格
    // ----------------------------------------------------------------

    @GetMapping("/deepay/product/{chainCode}")
    @Operation(summary = "查询商品详情，自动按 IP 返回本地化价格")
    public CommonResult<Map<String, Object>> getProduct(
            @PathVariable String chainCode,
            HttpServletRequest httpReq) {

        DeepayProductDO product = productMapper.selectByChainCode(chainCode);
        if (product == null) {
            return CommonResult.error(404, "商品不存在: " + chainCode);
        }

        // IP → 货币（后端统一，不信任前端）
        String currency = userCurrencyService.detect(httpReq);

        // basePrice 优先，fallback 到 price
        BigDecimal basePriceEur = product.getBasePrice() != null
                ? product.getBasePrice() : product.getPrice();
        BigDecimal displayPrice = fxRateService.convert(basePriceEur, currency);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("chainCode",    chainCode);
        resp.put("title",        product.getTitle());
        resp.put("description",  product.getDescription());
        resp.put("category",     product.getCategory());
        resp.put("status",       product.getStatus());
        resp.put("stock",        product.getStock());
        resp.put("soldCount",    product.getSoldCount());
        resp.put("channel",      product.getChannel());
        resp.put("image",        product.getMainImage());
        // 价格（本地化）
        resp.put("basePrice",    basePriceEur);     // EUR（内部用）
        resp.put("price",        displayPrice);     // 展示价（用户货币）
        resp.put("currency",     currency);
        // 汇率快照（前端展示用）
        resp.put("exchangeRate", fxRateService.getRate(currency));
        return success(resp);
    }

    // ----------------------------------------------------------------
    // GET /deepay/fx/rates  —— 查询所有支持的汇率
    // ----------------------------------------------------------------

    @GetMapping("/deepay/fx/rates")
    @Operation(summary = "查询所有汇率（EUR基准）")
    public CommonResult<Map<String, Object>> getFxRates() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("base",  "EUR");
        resp.put("rates", fxRateService.getCacheSnapshot());
        return success(resp);
    }

    // ----------------------------------------------------------------
    // POST /deepay/trend  —— 仅找爆款参考图
    // ----------------------------------------------------------------

    @PostMapping("/deepay/trend")
    @Operation(summary = "TrendAgent：根据关键词 / 画像返回爆款参考图列表")
    public CommonResult<Map<String, Object>> trend(@Valid @RequestBody RunReqVO req) {
        Context ctx = new Context();
        ctx.keyword  = req.getKeyword();
        ctx.category = req.getCategory();
        ctx.style    = req.getStyle();
        ctx.crowd    = req.getCrowd();
        if (ctx.keyword == null && ctx.category != null) ctx.keyword = ctx.category;
        trendAgent.run(ctx);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("keyword",         ctx.keyword);
        resp.put("category",        ctx.category);
        resp.put("trendImages",     ctx.trendImages);
        resp.put("referenceImages", ctx.referenceImages);
        return success(resp);
    }

    // ----------------------------------------------------------------
    // POST /api/create-product  —— 轻量版（向后兼容）
    // ----------------------------------------------------------------

    @PostMapping("/api/create-product")
    @Operation(summary = "一句话生成完整商品信息（chainCode + image + title + description + price + link）")
    public CommonResult<Map<String, Object>> createProduct(@Valid @RequestBody PromptReqVO req) {
        Context ctx = chainOrchestrator.run(req.getPrompt());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("chainCode",    ctx.chainCode);
        resp.put("image",        ctx.selectedImage);
        resp.put("title",        ctx.title);
        resp.put("description",  ctx.description);
        resp.put("price",        ctx.price);
        resp.put("link",         ctx.link);
        return success(resp);
    }

    // ----------------------------------------------------------------
    // Request VOs
    // ----------------------------------------------------------------

    public static class RunReqVO {
        private String keyword;
        private Long   customerId;
        private String category;
        private String style;
        private String crowd;
        private String market;
        private String priceLevel;
        private String designSelectMode;

        public String getKeyword()           { return keyword; }
        public void   setKeyword(String v)           { this.keyword = v; }
        public Long   getCustomerId()        { return customerId; }
        public void   setCustomerId(Long v)          { this.customerId = v; }
        public String getCategory()          { return category; }
        public void   setCategory(String v)          { this.category = v; }
        public String getStyle()             { return style; }
        public void   setStyle(String v)             { this.style = v; }
        public String getCrowd()             { return crowd; }
        public void   setCrowd(String v)             { this.crowd = v; }
        public String getMarket()            { return market; }
        public void   setMarket(String v)            { this.market = v; }
        public String getPriceLevel()        { return priceLevel; }
        public void   setPriceLevel(String v)        { this.priceLevel = v; }
        public String getDesignSelectMode()  { return designSelectMode; }
        public void   setDesignSelectMode(String v)  { this.designSelectMode = v; }
    }

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

