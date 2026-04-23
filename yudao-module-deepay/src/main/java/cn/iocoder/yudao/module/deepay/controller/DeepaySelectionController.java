package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.service.SelectionFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 选款引导层接口（Phase 6-7）。
 *
 * <pre>
 * POST /deepay/selection/start     — 开始或继续选款流程
 *   返回 pendingQuestion（还有问题）或 selectionImages（出图）
 *
 * POST /deepay/selection/answer    — 提交问答答案，继续流程
 *   等同 start，但调用方已把答案填入对应字段
 *
 * POST /deepay/selection/feedback  — 用户点选反馈（学习）
 *   驱动趋势库权重 + 客户画像进化，实现"越用越准"
 * </pre>
 *
 * <h3>典型调用序列</h3>
 * <pre>
 * 第1次：POST /start { customerId: 1 }
 *         → { pendingQuestion: "你主要做什么品类？", pendingField: "category" }
 * 第2次：POST /answer { customerId: 1, category: "外套" }
 *         → { pendingQuestion: "你的客户是谁？", pendingField: "crowd" }
 * ...
 * 第5次：POST /answer { ..., priceLevel: "MID" }
 *         → { selectionImages: [...], styleCombos: [...] }  ← 出图
 * 第6次：POST /feedback { customerId: 1, imageUrl: "...", isSelected: true }
 *         → {} 学习完成，下次更准
 * </pre>
 */
@Tag(name = "Deepay - 选款引导（Phase 6-7）")
@RestController
@RequestMapping("/deepay/selection")
@Validated
public class DeepaySelectionController {

    @Resource
    private SelectionFlowService selectionFlowService;

    // ====================================================================
    // POST /deepay/selection/start — 开始 / 继续选款流程
    // ====================================================================

    @PostMapping("/start")
    @Operation(summary = "开始选款流程（或继续填写答案）")
    public CommonResult<Map<String, Object>> start(@Valid @RequestBody SelectionReqVO req) {
        Context ctx = buildContext(req);
        ctx = selectionFlowService.startOrContinue(ctx);
        return success(buildResp(ctx));
    }

    // ====================================================================
    // POST /deepay/selection/answer — 提交答案继续
    // ====================================================================

    @PostMapping("/answer")
    @Operation(summary = "提交问答答案（等同 start，已填入答案）")
    public CommonResult<Map<String, Object>> answer(@Valid @RequestBody SelectionReqVO req) {
        Context ctx = buildContext(req);
        ctx = selectionFlowService.startOrContinue(ctx);
        return success(buildResp(ctx));
    }

    // ====================================================================
    // POST /deepay/selection/feedback — 用户点选反馈
    // ====================================================================

    @PostMapping("/feedback")
    @Operation(summary = "用户选图反馈（驱动学习：越用越准）")
    public CommonResult<Map<String, Object>> feedback(@Valid @RequestBody FeedbackReqVO req) {
        Context ctx = new Context();
        ctx.customerId = req.getCustomerId();
        ctx.category   = req.getCategory();
        ctx.style      = req.getStyle();
        ctx.crowd      = req.getCrowd();
        ctx.market     = req.getMarket();

        selectionFlowService.submitFeedback(ctx, req.getImageUrl(), req.isSelected());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("learned",    true);
        resp.put("customerId", req.getCustomerId());
        resp.put("isSelected", req.isSelected());
        return success(resp);
    }

    // ====================================================================
    // Request / Response VOs
    // ====================================================================

    /** 开始 / 继续选款请求（所有字段均可选，系统主动问缺失字段） */
    public static class SelectionReqVO {

        @NotNull(message = "customerId 不能为空")
        private Long customerId;

        private String category;   // 可选，有则跳过对应问题
        private String crowd;
        private String style;
        private String market;
        private String priceLevel;
        private String purpose;
        private String gender;
        private String targetAge;

        public Long   getCustomerId()  { return customerId; }
        public void   setCustomerId(Long v)   { this.customerId = v; }
        public String getCategory()    { return category; }
        public void   setCategory(String v)   { this.category = v; }
        public String getCrowd()       { return crowd; }
        public void   setCrowd(String v)      { this.crowd = v; }
        public String getStyle()       { return style; }
        public void   setStyle(String v)      { this.style = v; }
        public String getMarket()      { return market; }
        public void   setMarket(String v)     { this.market = v; }
        public String getPriceLevel()  { return priceLevel; }
        public void   setPriceLevel(String v) { this.priceLevel = v; }
        public String getPurpose()     { return purpose; }
        public void   setPurpose(String v)    { this.purpose = v; }
        public String getGender()      { return gender; }
        public void   setGender(String v)     { this.gender = v; }
        public String getTargetAge()   { return targetAge; }
        public void   setTargetAge(String v)  { this.targetAge = v; }
    }

    /** 选图反馈请求 */
    public static class FeedbackReqVO {

        @NotNull(message = "customerId 不能为空")
        private Long    customerId;
        private String  imageUrl;
        private String  category;
        private String  style;
        private String  crowd;
        private String  market;
        private boolean selected;

        public Long    getCustomerId()   { return customerId; }
        public void    setCustomerId(Long v)   { this.customerId = v; }
        public String  getImageUrl()     { return imageUrl; }
        public void    setImageUrl(String v)   { this.imageUrl = v; }
        public String  getCategory()     { return category; }
        public void    setCategory(String v)   { this.category = v; }
        public String  getStyle()        { return style; }
        public void    setStyle(String v)      { this.style = v; }
        public String  getCrowd()        { return crowd; }
        public void    setCrowd(String v)      { this.crowd = v; }
        public String  getMarket()       { return market; }
        public void    setMarket(String v)     { this.market = v; }
        public boolean isSelected()      { return selected; }
        public void    setSelected(boolean v)  { this.selected = v; }
    }

    // ====================================================================
    // 内部工具
    // ====================================================================

    private Context buildContext(SelectionReqVO req) {
        Context ctx = new Context();
        ctx.customerId = req.getCustomerId();
        ctx.category   = req.getCategory();
        ctx.crowd      = req.getCrowd();
        ctx.style      = req.getStyle();
        ctx.market     = req.getMarket();
        ctx.priceLevel = req.getPriceLevel();
        ctx.purpose    = req.getPurpose();
        ctx.gender     = req.getGender();
        ctx.targetAge  = req.getTargetAge();
        return ctx;
    }

    private Map<String, Object> buildResp(Context ctx) {
        Map<String, Object> resp = new LinkedHashMap<>();
        // 问题（若有 → 前端展示问题）
        resp.put("pendingQuestion", ctx.pendingQuestion);
        resp.put("pendingField",    ctx.pendingField);
        // 出图（问答完成后）
        resp.put("selectionImages", ctx.selectionImages);
        resp.put("selectionFeed",   ctx.selectionFeed);
        resp.put("styleCombos",     ctx.styleCombos);
        // 当前画像摘要
        resp.put("category",   ctx.category);
        resp.put("crowd",      ctx.crowd);
        resp.put("style",      ctx.style);
        resp.put("market",     ctx.market);
        resp.put("priceLevel", ctx.priceLevel);
        resp.put("purpose",    ctx.purpose);
        resp.put("confidence", ctx.confidenceScore);
        return resp;
    }

}
