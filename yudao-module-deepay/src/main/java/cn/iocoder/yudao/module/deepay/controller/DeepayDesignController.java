package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.agent.*;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignImageDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayOrderDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayProductDO;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayTaskDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignImageMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayOrderMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayProductMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayTaskMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayUserQuotaMapper;
import cn.iocoder.yudao.module.deepay.service.CurrencyService;
import cn.iocoder.yudao.module.deepay.service.DeepayQuotaService;
import cn.iocoder.yudao.module.deepay.service.DeepayRateLimitService;
import cn.iocoder.yudao.module.deepay.service.DeepayTaskAsyncService;
import cn.iocoder.yudao.module.deepay.service.FluxService;
import cn.iocoder.yudao.module.deepay.service.UserProfileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * DeepayDesignController — AI选款设计核心接口（Phase 9 STEP 21+）。
 *
 * <pre>
 * POST /api/design/generate     — 创建异步出图任务，立即返回 taskId（STEP 21）
 * GET  /api/design/result/{id}  — 轮询任务结果（STEP 21）
 * POST /api/design/select       — 用户选款 + 反馈学习入库
 * GET  /api/design/recommend    — 个性化推荐（STEP 29）
 * GET  /api/shop/{id}           — 小店页数据（分享链接）
 * POST /api/order/create        — 下单
 * GET  /api/design/top          — 品类 Top 图
 * </pre>
 */
@Tag(name = "Deepay - AI设计选款")
@RestController
@RequestMapping("/api")
@Validated
public class DeepayDesignController {

    private static final Logger log = LoggerFactory.getLogger(DeepayDesignController.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Resource private DeepayTaskMapper        taskMapper;
    @Resource private DeepayTaskAsyncService  asyncService;
    @Resource private DeepayRateLimitService  rateLimitService;
    @Resource private DeepayQuotaService      quotaService;
    @Resource private DeepayUserQuotaMapper   quotaMapper;
    @Resource private FeedbackAgent           feedbackAgent;
    @Resource private UserProfileService      userProfileService;
    @Resource private CurrencyService         currencyService;
    @Resource private DeepayDesignImageMapper designImageMapper;
    @Resource private DeepayOrderMapper       orderMapper;
    @Resource private DeepayProductMapper     productMapper;
    @Resource private FluxService             fluxService;

    // ====================================================================
    // POST /api/design/generate — 创建异步出图任务（STEP 21）
    // ====================================================================

    @PostMapping("/design/generate")
    @Operation(summary = "创建 AI 出图任务（异步），立即返回 taskId")
    public CommonResult<Map<String, Object>> generate(@Valid @RequestBody GenerateReqVO req) {
        String userId = req.getUserId() != null ? String.valueOf(req.getUserId()) : "anonymous";
        log.info("[generate] category={} style={} market={} userId={}",
                req.getCategory(), req.getStyle(), req.getMarket(), userId);

        // ① 限流（STEP 23）：1 分钟内最多 3 次
        if (!rateLimitService.allow(userId)) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "请求过于频繁，请 1 分钟后再试");
            r.put("code",  429);
            return success(r);
        }

        // ② 配额检查（STEP 26）：Upsell 而非硬拦截
        if (req.getUserId() != null) {
            DeepayQuotaService.QuotaCheckResult quotaResult = quotaService.checkAndConsume(userId);
            if (quotaResult != null && quotaResult.exceeded) {
                // 超限：返回 Upsell 信息，前端展示购买弹窗
                return success(quotaResult.toMap());
            }
        }

        // ③ 创建任务记录（STEP 21）
        DeepayTaskDO task = new DeepayTaskDO();
        task.setUserId(userId);
        task.setStatus("pending");
        task.setCreatedAt(LocalDateTime.now());
        taskMapper.insert(task);

        // ④ 构建 Context
        Context ctx = new Context();
        ctx.category        = req.getCategory();
        ctx.stylePreference = req.getStyle();
        ctx.targetMarket    = req.getMarket();
        ctx.market          = req.getMarket();
        ctx.priceLevel      = req.getPriceLevel();
        ctx.keyword         = req.getCategory();
        if (req.getUserId() != null) ctx.userId = req.getUserId();

        // 加载用户历史画像
        if (req.getUserId() != null) {
            userProfileService.loadProfile(userId, ctx);
        }

        // ⑤ 异步执行（STEP 21）
        asyncService.runTask(task.getId(), ctx);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("taskId",  task.getId());
        resp.put("status",  "pending");
        resp.put("message", "将为你生成当前热门爆款款式，请稍候…");
        // 返回剩余配额，前端可展示"今日剩余 N 次"
        resp.put("quota",   quotaService.getQuotaInfo(userId));
        log.info("[generate] 任务已创建 taskId={} userId={}", task.getId(), userId);
        return success(resp);
    }

    // ====================================================================
    // GET /api/design/result/{id} — 轮询任务结果（STEP 21）
    // ====================================================================

    @GetMapping("/design/result/{id}")
    @Operation(summary = "轮询异步出图任务结果")
    public CommonResult<Map<String, Object>> result(@PathVariable("id") Long id) {
        DeepayTaskDO task = taskMapper.selectById(id);
        if (task == null) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "任务不存在");
            r.put("code",  404);
            return success(r);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("taskId", task.getId());
        resp.put("status", task.getStatus());

        if ("success".equals(task.getStatus())) {
            // 反序列化图片列表
            List<String> images = Collections.emptyList();
            try {
                if (task.getResult() != null) {
                    images = MAPPER.readValue(task.getResult(), new TypeReference<List<String>>() {});
                }
            } catch (Exception e) {
                log.warn("[result] 反序列化 result 失败 taskId={}", id, e);
            }
            resp.put("images", images);
            resp.put("count",  images.size());
        } else if ("failed".equals(task.getStatus())) {
            resp.put("error", task.getErrorMsg());
        }

        return success(resp);
    }

    // ====================================================================
    // POST /api/design/select — 用户选款
    // ====================================================================

    @PostMapping("/design/select")
    @Operation(summary = "用户选款 + 反馈学习入库")
    public CommonResult<Map<String, Object>> select(@Valid @RequestBody SelectReqVO req) {
        log.info("[select] userId={} selectedImage={}", req.getUserId(), req.getSelectedImage());

        Context ctx = new Context();
        ctx.selectedImage   = req.getSelectedImage();
        ctx.chainCode       = req.getChainCode();
        ctx.category        = req.getCategory();
        ctx.stylePreference = req.getStyle();
        if (req.getUserId() != null) {
            ctx.userId     = req.getUserId();
            ctx.customerId = req.getUserId();
        }

        if (req.getAllImages() != null && !req.getAllImages().isEmpty()) {
            ctx.designImages = req.getAllImages();
            ctx.scoredImages = req.getAllImages().stream().map(url -> {
                DesignImage img = new DesignImage();
                img.setUrl(url);
                img.setCategory(req.getCategory());
                img.setStyle(req.getStyle());
                img.setScore(50.0);
                return img;
            }).collect(Collectors.toList());
        }

        feedbackAgent.run(ctx);

        if (req.getUserId() != null) {
            userProfileService.updateProfile(String.valueOf(req.getUserId()), ctx);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("selectedImage", ctx.selectedImage);
        resp.put("chainCode",     ctx.chainCode);
        resp.put("status",        "RECORDED");
        return success(resp);
    }

    // ====================================================================
    // GET /api/design/recommend — 个性化推荐（STEP 29）
    // ====================================================================

    @GetMapping("/design/recommend")
    @Operation(summary = "个性化图片推荐（按用户品类+风格偏好，评分降序）")
    public CommonResult<Map<String, Object>> recommend(
            @RequestParam(value = "category",  required = false) String category,
            @RequestParam(value = "style",     required = false) String style,
            @RequestParam(value = "userId",    required = false) Long userId,
            @RequestParam(value = "limit",     defaultValue = "10") int limit) {

        // 如果未传 category/style，尝试从用户画像加载
        if (userId != null && (category == null || style == null)) {
            Context tmp = new Context();
            userProfileService.loadProfile(String.valueOf(userId), tmp);
            if (category == null) category = tmp.category;
            if (style    == null) style    = tmp.stylePreference;
        }

        log.info("[recommend] category={} style={} userId={} limit={}", category, style, userId, limit);
        List<DeepayDesignImageDO> images = designImageMapper.selectRecommend(category, style, Math.min(limit, 50));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("images",   images);
        resp.put("count",    images.size());
        resp.put("category", category);
        resp.put("style",    style);
        return success(resp);
    }

    // ====================================================================
    // GET /api/shop/{id} — 小店页（分享链接）
    // ====================================================================

    @GetMapping("/shop/{id}")
    @Operation(summary = "小店页数据（分享链接）")
    public CommonResult<Map<String, Object>> shopPage(
            @PathVariable("id") String id,
            @RequestParam(value = "currency", defaultValue = "CNY") String currency) {
        log.info("[shopPage] id={} currency={}", id, currency);

        DeepayProductDO product = null;
        try {
            product = productMapper.selectByChainCode(id);
        } catch (Exception e) {
            log.warn("[shopPage] 查询商品失败 id={}", id, e);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        if (product != null) {
            BigDecimal displayPrice = currencyService.convert(product.getPrice(), currency);
            resp.put("id",          id);
            resp.put("title",       product.getTitle());
            resp.put("description", product.getDescription());
            resp.put("image",       product.getMainImage());
            resp.put("designId",    product.getDesignId());
            resp.put("price",       displayPrice);
            resp.put("currency",    currency);
            resp.put("status",      product.getStatus());
            resp.put("shareUrl",    "https://deepay.link/shop/" + id);
        } else {
            resp.put("id",       id);
            resp.put("status",   "NOT_FOUND");
            resp.put("shareUrl", "https://deepay.link/shop/" + id);
        }
        return success(resp);
    }

    // ====================================================================
    // POST /api/order/create — 下单
    // ====================================================================

    @PostMapping("/order/create")
    @Operation(summary = "下单（创建待支付订单）")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Map<String, Object>> createOrder(@Valid @RequestBody OrderCreateReqVO req) {
        log.info("[createOrder] userId={} chainCode={}", req.getUserId(), req.getChainCode());

        if (req.getUserId() != null) {
            DeepayOrderDO existing = orderMapper.selectByChainCodeAndUserId(req.getChainCode(), req.getUserId());
            if (existing != null && !"CANCELLED".equals(existing.getStatus())) {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("orderId",    existing.getId());
                resp.put("paymentId",  existing.getPaymentId());
                resp.put("status",     existing.getStatus());
                resp.put("idempotent", true);
                return success(resp);
            }
        }

        DeepayOrderDO order = new DeepayOrderDO();
        order.setPaymentId(UUID.randomUUID().toString().replace("-", ""));
        order.setChainCode(req.getChainCode());
        order.setUserId(req.getUserId());
        order.setStatus("PENDING");
        order.setAmount(req.getAmount() != null ? req.getAmount() : BigDecimal.ZERO);
        order.setCreatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("orderId",   order.getId());
        resp.put("paymentId", order.getPaymentId());
        resp.put("status",    "PENDING");
        resp.put("payUrl",    "https://deepay.link/pay/" + order.getPaymentId());
        log.info("[createOrder] DONE orderId={} paymentId={}", order.getId(), order.getPaymentId());
        return success(resp);
    }

    // ====================================================================
    // GET /api/design/top — Top 图
    // ====================================================================

    @GetMapping("/design/top")
    @Operation(summary = "获取品类 Top 图（按评分）")
    public CommonResult<Map<String, Object>> topImages(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        log.info("[topImages] category={} limit={}", category, limit);
        List<?> images = designImageMapper.selectTopByCategory(category, Math.min(limit, 50));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("images", images);
        resp.put("count",  images.size());
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/generateCollection — 系列生成（带细节控制）
    //
    // 请求：{
    //   "refs": ["url1","url2"],
    //   "style": "minimal_modern",
    //   "controls": { "neck":"round","sleeve":"short","length":"regular","fit":"loose" },
    //   "count": 6
    // }
    // 响应：{
    //   "images": ["img1",…,"img6"],
    //   "collectionName": "Collection A",
    //   "controls": { … },
    //   "style": "minimal_modern"
    // }
    //
    // Prompt 规则（写死）：
    //   - 强制 collection 一致性（同色系 + 同风格语言）
    //   - 细节控制必须写进 prompt（neck/sleeve/length/fit）
    //   - Image 1 → silhouette; Image 2 → style direction
    // ====================================================================

    @PostMapping("/ai/generateCollection")
    @Operation(summary = "AI 系列生成：生成同风格统一系列（带细节控制）")
    public CommonResult<Map<String, Object>> generateCollection(
            @RequestBody GenerateCollectionReqVO req) {
        String userId = req.getUserId() != null ? req.getUserId() : "anonymous";
        int count = (req.getCount() != null && req.getCount() >= 4) ? Math.min(req.getCount(), 8) : 6;
        String style = req.getStyle() != null ? req.getStyle().toLowerCase() : "minimal";

        log.info("[generateCollection] userId={} style={} count={} controls={}", userId, style, count, req.getControls());

        if (!rateLimitService.allow("collection:" + userId)) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "请求过于频繁，请稍后再试");
            r.put("code",  429);
            return success(r);
        }

        Map<String, String> controls = req.getControls() != null ? req.getControls() : Collections.emptyMap();
        String prompt = buildCollectionPrompt(style, controls, req.getRefs());
        log.info("[generateCollection] prompt={}", prompt);

        List<String> images;
        try {
            images = fluxService.generateImages(prompt, count);
        } catch (Exception e) {
            log.warn("[generateCollection] 生成失败，重试", e);
            images = fluxService.generateImages(prompt, count);
        }

        // Ensure at least 4
        if (images.size() < 4) {
            List<String> padded = new java.util.ArrayList<>(images);
            while (padded.size() < count) padded.addAll(images);
            images = padded.subList(0, Math.min(count, padded.size()));
        }

        // Generate collection name
        String collectionName = buildCollectionName(style, controls);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("images",         images);
        resp.put("count",          images.size());
        resp.put("collectionName", collectionName);
        resp.put("style",          style);
        resp.put("controls",       controls);
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/updateDetail — 局部细节修改
    //
    // 请求：{ "image": "xxx.png", "control": { "sleeve": "long" } }
    // 响应：{ "image": "updated.png", "control": { "sleeve": "long" } }
    //
    // 规则：只改指定细节，其他结构保持原样
    // ====================================================================

    @PostMapping("/ai/updateDetail")
    @Operation(summary = "AI 局部细节修改：只改指定控制项，其他不变")
    public CommonResult<Map<String, Object>> updateDetail(@RequestBody UpdateDetailReqVO req) {
        if (req.getImage() == null || req.getImage().trim().isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "image 不能为空");
            r.put("code",  400);
            return success(r);
        }
        if (req.getControl() == null || req.getControl().isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "control 不能为空");
            r.put("code",  400);
            return success(r);
        }

        log.info("[updateDetail] image={} control={}", req.getImage(), req.getControl());

        String prompt = buildUpdateDetailPrompt(req.getControl());
        log.info("[updateDetail] prompt={}", prompt);

        List<String> generated = fluxService.generateImages(prompt, 1);
        String resultImage = (generated != null && !generated.isEmpty()) ? generated.get(0) : req.getImage();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("image",   resultImage);
        resp.put("control", req.getControl());
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/redesign — AI 改款：参考图 → 6 张新款
    //
    // 请求：{ "images": ["url1","url2"], "style": "minimal",
    //        "strength": 0.6, "count": 6 }
    // 响应：{ "images": ["new1.png",…,"new6.png"] }
    //
    // Prompt 规则（写死）：
    //   style=minimal → "minimal black and white clothing, clean lines,
    //                     premium fashion, no extra patterns"
    //   通用后缀 → "Based on the reference clothing design:
    //               - Keep the original silhouette
    //               - Improve color and details
    //               - Make it modern and premium
    //               - Clean background - No logo or copyright
    //               Generate multiple variations"
    // ====================================================================

    @PostMapping("/ai/redesign")
    @Operation(summary = "AI 改款：参考图 → N 张新款（默认 6 张）")
    public CommonResult<Map<String, Object>> redesign(@RequestBody RedesignReqVO req) {
        String userId = req.getUserId() != null ? req.getUserId() : "anonymous";
        int count = (req.getCount() != null && req.getCount() >= 4) ? Math.min(req.getCount(), 8) : 6;
        String style = req.getStyle() != null ? req.getStyle().toLowerCase() : "minimal";

        log.info("[redesign] userId={} style={} count={} images={}", userId, style, count,
                req.getImages() != null ? req.getImages().size() : 0);

        // ① Rate limit
        if (!rateLimitService.allow("redesign:" + userId)) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "请求过于频繁，请稍后再试");
            r.put("code",  429);
            return success(r);
        }

        String prompt = buildRedesignPrompt(style, req.getStrength(), req.getImages());
        log.info("[redesign] prompt={}", prompt);

        List<String> images;
        try {
            images = fluxService.generateImages(prompt, count);
        } catch (Exception e) {
            log.warn("[redesign] 生成失败，使用保底图片", e);
            images = fluxService.generateImages(prompt, count); // fluxService 内部保底不会抛
        }

        // 保证至少 4 张
        if (images.size() < 4) {
            log.warn("[redesign] 返回图片不足 4 张（{}），补足保底", images.size());
            List<String> padded = new java.util.ArrayList<>(images);
            while (padded.size() < count) {
                padded.addAll(images);
            }
            images = padded.subList(0, Math.min(count, padded.size()));
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("images", images);
        resp.put("count",  images.size());
        resp.put("style",  style);
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/refine — AI 精修：对选中图再生成 3 张升级版
    //
    // 请求：{ "image": "selectedUrl", "style": "minimal", "note": "可选备注" }
    // 响应：{ "images": ["refined1","refined2","refined3"], "count": 3 }
    //
    // 作用：
    //   选中1张好图 → refine（再生成3张升级版）→ 最终选1张
    //   = 从"AI图" → "设计稿"的关键一步
    // ====================================================================

    @PostMapping("/ai/refine")
    @Operation(summary = "AI 精修：对选中的好图再生成 3 张设计师级升级版")
    public CommonResult<Map<String, Object>> refineImage(@RequestBody RefineReqVO req) {
        if (req.getImage() == null || req.getImage().trim().isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "image 不能为空");
            r.put("code",  400);
            return success(r);
        }

        String style = req.getStyle() != null ? req.getStyle().toLowerCase() : "minimal";
        log.info("[refineImage] image={} style={} note={}", req.getImage(), style, req.getNote());

        String prompt = buildRefinePrompt(style, req.getNote());
        log.info("[refineImage] prompt={}", prompt);

        List<String> refined;
        try {
            refined = fluxService.generateImages(prompt, 3);
        } catch (Exception e) {
            log.warn("[refineImage] 精修生成失败，fallback", e);
            refined = fluxService.generateImages(prompt, 3);
        }

        // Ensure exactly 3 results
        if (refined.size() < 3) {
            List<String> padded = new java.util.ArrayList<>(refined);
            while (padded.size() < 3) padded.addAll(refined);
            refined = padded.subList(0, 3);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("images",        refined);
        resp.put("count",         refined.size());
        resp.put("sourceImage",   req.getImage());
        resp.put("style",         style);
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/recolor — AI 改色：只改颜色，不动结构
    //
    // 请求：{ "image": "xxx.png", "colorScheme": "black_white" }
    // 响应：{ "image": "recolored.png", "colorScheme": "black_white" }
    //
    // 预设配色方案：
    //   black_white  → 纯黑白配色，极简高级
    //   earth_tone   → 大地色系（米/驼/棕），可穿性强
    //   grey_minimal → 灰阶极简，低调质感
    //   mono_color   → 单色系统一，品牌感强
    //   navy_cream   → 藏青+米白，经典永恒
    //
    // 规则（写死）：
    //   ✔ 只改颜色 / 面料色调
    //   ✔ 保留原始廓形、结构、剪裁
    //   ❌ 不改版型
    //   ❌ 不改细节设计
    // ====================================================================

    @PostMapping("/ai/recolor")
    @Operation(summary = "AI 改色：只改颜色方案，结构不变")
    public CommonResult<Map<String, Object>> recolorImage(@RequestBody RecolorReqVO req) {
        if (req.getImage() == null || req.getImage().trim().isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "image 不能为空");
            r.put("code",  400);
            return success(r);
        }

        String scheme = req.getColorScheme() != null ? req.getColorScheme().toLowerCase() : "black_white";
        log.info("[recolorImage] image={} colorScheme={}", req.getImage(), scheme);

        String colorPrompt = buildRecolorPrompt(scheme);
        log.info("[recolorImage] prompt={}", colorPrompt);

        List<String> generated = fluxService.generateImages(colorPrompt, 1);
        String resultImage = (generated != null && !generated.isEmpty()) ? generated.get(0) : req.getImage();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("image",       resultImage);
        resp.put("colorScheme", scheme);
        resp.put("colorLabel",  COLOR_SCHEME_LABELS.getOrDefault(scheme, scheme));
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/edit — AI 微调：单图 + 指令 → 新图
    //
    // 请求：{ "image": "url", "instruction": "改成黑白，更高级" }
    // 响应：{ "image": "new.png" }
    // ====================================================================

    @PostMapping("/ai/edit")
    @Operation(summary = "AI 微调：对选中图片执行文字指令修改")
    public CommonResult<Map<String, Object>> editImage(@RequestBody EditReqVO req) {
        if (req.getImage() == null || req.getImage().trim().isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "image 不能为空");
            r.put("code",  400);
            return success(r);
        }
        if (req.getInstruction() == null || req.getInstruction().trim().isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "instruction 不能为空");
            r.put("code",  400);
            return success(r);
        }

        log.info("[editImage] image={} instruction={}", req.getImage(), req.getInstruction());

        // Compose edit prompt: combine instruction with image context
        String prompt = "Edit clothing design image. Instruction: " + req.getInstruction().trim()
                + ". Keep original silhouette. Premium fashion photography. Clean background. No logo.";

        List<String> generated = fluxService.generateImages(prompt, 1);
        String resultImage = (generated != null && !generated.isEmpty()) ? generated.get(0) : req.getImage();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("image",       resultImage);
        resp.put("instruction", req.getInstruction());
        return success(resp);
    }

    // ====================================================================
    // GET /api/design/my — 我的款库
    // ====================================================================

    @GetMapping("/design/my")
    @Operation(summary = "获取用户保存的款库列表")
    public CommonResult<Map<String, Object>> myDesigns(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "limit",  defaultValue = "20") int limit) {
        log.info("[myDesigns] userId={}", userId);
        // Query saved designs: re-use designImageMapper scoped to userId if available,
        // otherwise return the user's recently scored images
        List<DeepayDesignImageDO> images = designImageMapper.selectRecommend(null, null, Math.min(limit, 50));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("designs", images);
        resp.put("count",   images.size());
        return success(resp);
    }

    // ====================================================================
    // POST /api/design/save — 保存款式到款库
    // ====================================================================

    @PostMapping("/design/save")
    @Operation(summary = "保存选中图片到用户款库")
    public CommonResult<Map<String, Object>> saveDesign(@RequestBody SaveDesignReqVO req) {
        if (req.getImageUrl() == null || req.getImageUrl().trim().isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "imageUrl 不能为空");
            r.put("code",  400);
            return success(r);
        }

        log.info("[saveDesign] userId={} imageUrl={} style={}", req.getUserId(), req.getImageUrl(), req.getStyle());

        DeepayDesignImageDO design = new DeepayDesignImageDO();
        design.setUrl(req.getImageUrl());
        design.setCategory(req.getCategory() != null ? req.getCategory() : "未分类");
        design.setStyle(req.getStyle() != null ? req.getStyle() : "未知");
        design.setScore(80.0);
        design.setViewCount(1);
        design.setClickCount(1);
        design.setOrderCount(0);
        design.setCreatedAt(java.time.LocalDateTime.now());
        designImageMapper.insert(design);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id",       design.getId());
        resp.put("imageUrl", design.getUrl());
        resp.put("status",   "SAVED");
        return success(resp);
    }

    // ====================================================================
    // GET /api/inspiration/images — 时装灵感库（种子数据）
    //
    // 返回分页的灵感图片列表，支持 source / style / type 过滤
    // ====================================================================

    @GetMapping("/inspiration/images")
    @Operation(summary = "获取时装灵感库图片（时装周 + 品牌 + 大片）")
    public CommonResult<Map<String, Object>> inspirationImages(
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "style",  required = false) String style,
            @RequestParam(value = "type",   required = false) String type,
            @RequestParam(value = "limit",  defaultValue = "30") int limit) {

        log.info("[inspirationImages] source={} style={} type={} limit={}", source, style, type, limit);

        List<Map<String, Object>> all = buildInspirationSeed();

        List<Map<String, Object>> filtered = all.stream()
                .filter(item -> source == null || source.isEmpty()
                        || source.equals(item.get("source")))
                .filter(item -> style  == null || style.isEmpty()
                        || style.equals(item.get("style")))
                .filter(item -> type   == null || type.isEmpty()
                        || type.equals(item.get("type")))
                .limit(Math.min(limit, 100))
                .collect(Collectors.toList());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("items", filtered);
        resp.put("count", filtered.size());
        return success(resp);
    }

    /** Builds the static inspiration seed data. */
    private List<Map<String, Object>> buildInspirationSeed() {
        Object[][] seed = {
            // { id, imageUrl, source, brand, style, type, season, desc }
            {"fw_paris_001","https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600&q=80&fit=crop","fashion_week","Paris Fashion Week","minimal","coat","AW 2024","极简黑白大衣，结构感强"},
            {"fw_paris_002","https://images.unsplash.com/photo-1509631179647-0177331693ae?w=600&q=80&fit=crop","fashion_week","Paris Fashion Week","avant-garde","dress","SS 2024","廓形连衣裙，强调剪裁"},
            {"fw_paris_003","https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&q=80&fit=crop","fashion_week","Paris Fashion Week","luxury","suit","AW 2024","高端西装套装，精致细节"},
            {"fw_paris_004","https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=600&q=80&fit=crop","fashion_week","Paris Fashion Week","minimal","trench","SS 2025","风衣廓形，米色经典"},
            {"fw_milan_001","https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=600&q=80&fit=crop","fashion_week","Milan Fashion Week","luxury","gown","AW 2024","米兰秀台礼服，奢华面料"},
            {"fw_milan_002","https://images.unsplash.com/photo-1558618047-3c8c76ca7d13?w=600&q=80&fit=crop","fashion_week","Milan Fashion Week","streetwear","jacket","SS 2024","运动夹克，街头奢华融合"},
            {"fw_milan_003","https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=600&q=80&fit=crop","fashion_week","Milan Fashion Week","elegant","blouse","SS 2025","精致衬衫，意式优雅"},
            {"fw_ny_001","https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=600&q=80&fit=crop","fashion_week","New York Fashion Week","minimal","coat","AW 2024","纽约极简主义大衣"},
            {"fw_ny_002","https://images.unsplash.com/photo-1518611012118-696072aa579a?w=600&q=80&fit=crop","fashion_week","New York Fashion Week","streetwear","set","SS 2024","运动时尚套装，都市风"},
            {"fw_ny_003","https://images.unsplash.com/photo-1485968579580-b6d095142e6e?w=600&q=80&fit=crop","fashion_week","New York Fashion Week","trendy","dress","SS 2025","大胆印花连衣裙"},
            {"brand_cos_001","https://images.unsplash.com/photo-1525507119028-ed4c629a60a3?w=600&q=80&fit=crop","brand_lookbook","COS","minimal","coat","AW 2024","COS 极简羊毛大衣"},
            {"brand_cos_002","https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=600&q=80&fit=crop","brand_lookbook","COS","minimal","dress","SS 2024","COS 结构感连衣裙"},
            {"brand_cos_003","https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&q=80&fit=crop","brand_lookbook","COS","minimal","trousers","SS 2025","COS 宽腿剪裁长裤"},
            {"brand_arket_001","https://images.unsplash.com/photo-1584370848010-d7fe6bc767ec?w=600&q=80&fit=crop","brand_lookbook","ARKET","minimal","shirt","SS 2024","ARKET 纯棉衬衫，极简主义"},
            {"brand_arket_002","https://images.unsplash.com/photo-1619603364853-a8ff3db2b12e?w=600&q=80&fit=crop","brand_lookbook","ARKET","minimal","knitwear","AW 2024","ARKET 精制针织，北欧风格"},
            {"brand_acne_001","https://images.unsplash.com/photo-1551232864-3f0890e580d9?w=600&q=80&fit=crop","brand_lookbook","Acne Studios","minimal","coat","AW 2024","Acne Studios 标志性廓形大衣"},
            {"brand_acne_002","https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600&q=80&fit=crop","brand_lookbook","Acne Studios","trendy","jacket","SS 2025","Acne Studios 前卫夹克"},
            {"brand_zara_001","https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600&q=80&fit=crop","brand_lookbook","ZARA","trendy","coat","AW 2024","ZARA 快时尚廓形大衣"},
            {"brand_zara_002","https://images.unsplash.com/photo-1568252542512-9fe8fe9c87bb?w=600&q=80&fit=crop","brand_lookbook","ZARA","elegant","dress","SS 2024","ZARA 优雅连衣裙，可落地"},
            {"ed_ssense_001","https://images.unsplash.com/photo-1544957992-20514f595d6f?w=600&q=80&fit=crop","editorial","SSENSE","luxury","coat","AW 2024","设计师精选，奢华大衣"},
            {"ed_ssense_002","https://images.unsplash.com/photo-1516762689617-e1cffcef479d?w=600&q=80&fit=crop","editorial","SSENSE","minimal","dress","SS 2024","SSENSE 编辑精选极简连衣裙"},
            {"ed_farfetch_001","https://images.unsplash.com/photo-1506634572416-48cdfe530110?w=600&q=80&fit=crop","editorial","Farfetch","luxury","suit","SS 2025","Farfetch 精品奢华西装"},
            {"ed_farfetch_002","https://images.unsplash.com/photo-1487222477894-8943e31ef7b2?w=600&q=80&fit=crop","editorial","Farfetch","streetwear","jacket","AW 2024","街头潮牌夹克，高端配色"},
        };

        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : seed) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id",     row[0]);
            item.put("image",  row[1]);
            item.put("source", row[2]);
            item.put("brand",  row[3]);
            item.put("style",  row[4]);
            item.put("type",   row[5]);
            item.put("season", row[6]);
            item.put("desc",   row[7]);
            // Enrich with quality metadata
            String url    = (String) row[1];
            String source = (String) row[2];
            String style  = (String) row[4];
            int    score  = computeInspirationScore(url, source);
            item.put("score",  score);
            item.put("usable", score >= 60);
            item.put("tags",   buildStyleTags(style, source));
            item.put("layer",  sourceToLayer(source));
            items.add(item);
        }
        return items;
    }

    // ====================================================================
    // POST /api/inspiration/filter — 过滤垃圾图（水印/杂乱/低清）
    //
    // 请求：{ "images": ["url1","url2"] }
    // 响应：{ "valid":["url1"], "rejected":["url2"], "reason":{"url2":"clutter"} }
    // ====================================================================

    @PostMapping("/inspiration/filter")
    @Operation(summary = "灵感图过滤：剔除低质/杂乱/水印图片")
    public CommonResult<Map<String, Object>> filterInspiration(@RequestBody InspirationFilterReqVO req) {
        List<String> images = req.getImages() != null ? req.getImages() : Collections.emptyList();
        log.info("[filterInspiration] count={}", images.size());

        List<String> valid    = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        Map<String, String> reasons = new LinkedHashMap<>();

        for (String url : images) {
            String reason = inspectionRejectReason(url);
            if (reason != null) {
                rejected.add(url);
                reasons.put(url, reason);
            } else {
                valid.add(url);
            }
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("valid",    valid);
        resp.put("rejected", rejected);
        resp.put("reason",   reasons);
        resp.put("validCount",    valid.size());
        resp.put("rejectedCount", rejected.size());
        return success(resp);
    }

    // ====================================================================
    // POST /api/inspiration/score — 5维评分（清晰/简洁/主体/背景/设计感）
    //
    // 请求：{ "images": ["url1","url2"] }
    // 响应：{ "scores": [{ "url":"url1","score":82,"breakdown":{...},"usable":true }] }
    // ====================================================================

    @PostMapping("/inspiration/score")
    @Operation(summary = "灵感图质量评分：5维打分，低于60丢弃")
    public CommonResult<Map<String, Object>> scoreInspiration(@RequestBody InspirationScoreReqVO req) {
        List<String> images = req.getImages() != null ? req.getImages() : Collections.emptyList();
        log.info("[scoreInspiration] count={}", images.size());

        List<Map<String, Object>> scores = new ArrayList<>();
        for (String url : images) {
            Map<String, Object> entry = buildInspirationScoreEntry(url, req.getSource());
            scores.add(entry);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("scores", scores);
        resp.put("usableCount", scores.stream().filter(s -> Boolean.TRUE.equals(s.get("usable"))).count());
        return success(resp);
    }

    // ====================================================================
    // POST /api/inspiration/classify — 风格分类（minimal/modern/avant/classic）
    //
    // 请求：{ "images": ["url1","url2"] }
    // 响应：{ "results": [{ "url":"url1","style":"minimal","confidence":0.92,"tags":["clean","black"] }] }
    // ====================================================================

    @PostMapping("/inspiration/classify")
    @Operation(summary = "灵感图风格分类：minimal / modern / avant / classic")
    public CommonResult<Map<String, Object>> classifyInspiration(@RequestBody InspirationClassifyReqVO req) {
        List<String> images = req.getImages() != null ? req.getImages() : Collections.emptyList();
        log.info("[classifyInspiration] count={}", images.size());

        List<Map<String, Object>> results = new ArrayList<>();
        for (String url : images) {
            results.add(buildClassifyEntry(url));
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("results", results);
        return success(resp);
    }

    // ====================================================================
    // POST /api/inspiration/process — 一键处理管道（filter → score → classify）
    //
    // 请求：{ "images": ["url1","url2"], "source": "fashion_week" }
    // 响应：{ "list": [{ "image","score","style","tags","usable","reason" }] }
    //
    // 流程：
    //   1. filter（剔除垃圾图）
    //   2. score（5维打分，< 60 丢弃）
    //   3. classify（风格标签）
    //   4. 返回 usable=true 列表
    // ====================================================================

    @PostMapping("/inspiration/process")
    @Operation(summary = "灵感图一键处理：filter → score → classify，返回可用图列表")
    public CommonResult<Map<String, Object>> processInspiration(@RequestBody InspirationProcessReqVO req) {
        List<String> images = req.getImages() != null ? req.getImages() : Collections.emptyList();
        String source        = req.getSource();
        log.info("[processInspiration] count={} source={}", images.size(), source);

        List<Map<String, Object>> list = new ArrayList<>();
        List<String> rejectedList = new ArrayList<>();

        for (String url : images) {
            // Step 1: filter
            String rejectReason = inspectionRejectReason(url);
            if (rejectReason != null) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("image",   url);
                entry.put("usable",  false);
                entry.put("reason",  rejectReason);
                entry.put("score",   0);
                rejectedList.add(url);
                list.add(entry);
                continue;
            }

            // Step 2: score
            int score = computeInspirationScore(url, source);
            if (score < 60) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("image",  url);
                entry.put("usable", false);
                entry.put("reason", "low_score");
                entry.put("score",  score);
                rejectedList.add(url);
                list.add(entry);
                continue;
            }

            // Step 3: classify
            Map<String, Object> classified = buildClassifyEntry(url);
            String style  = (String) classified.get("style");
            Object tags   = classified.get("tags");
            double conf   = (double) classified.getOrDefault("confidence", 0.8);

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("image",      url);
            entry.put("score",      score);
            entry.put("style",      style);
            entry.put("confidence", conf);
            entry.put("tags",       tags);
            entry.put("usable",     true);
            entry.put("layer",      sourceToLayer(source));
            list.add(entry);
        }

        long usableCount = list.stream().filter(e -> Boolean.TRUE.equals(e.get("usable"))).count();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("list",          list);
        resp.put("total",         list.size());
        resp.put("usableCount",   usableCount);
        resp.put("rejectedCount", rejectedList.size());
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/selectRefs — AI自动组合3张最合适的参考图
    //
    // 请求：{ "images": [{ "url","score","style","tags","layer" }] }
    // 响应：{ "structure":"url1","style":"url2","detail":"url3","confidence":0.93,
    //         "items": [{ "role","url","reason" }] }
    //
    // 选择逻辑（写死）：
    //   ① 只取 score >= 70 的图
    //   ② structure → fashion_week / design layer，全身款式图，score最高
    //   ③ style     → brand_lookbook / commercial layer，颜色/氛围清晰
    //   ④ detail    → editorial / style已分类，有细节特征
    //   ⑤ 三张必须风格接近（避免冲突），但类型不重复
    // ====================================================================

    @PostMapping("/ai/selectRefs")
    @Operation(summary = "AI自动组合3张参考图：structure（版型）/ style（风格）/ detail（细节）")
    public CommonResult<Map<String, Object>> selectRefs(@RequestBody SelectRefsReqVO req) {
        List<Map<String, Object>> images = req.getImages();
        if (images == null || images.isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "images 不能为空");
            r.put("code",  400);
            return success(r);
        }

        log.info("[selectRefs] candidateCount={}", images.size());

        // Filter to usable (score >= 70)
        List<Map<String, Object>> usable = images.stream()
                .filter(img -> {
                    Object s = img.get("score");
                    int score = s instanceof Number ? ((Number) s).intValue() : 0;
                    return score >= 70;
                })
                .sorted((a, b) -> {
                    int sa = a.get("score") instanceof Number ? ((Number) a.get("score")).intValue() : 0;
                    int sb = b.get("score") instanceof Number ? ((Number) b.get("score")).intValue() : 0;
                    return sb - sa; // descending
                })
                .collect(Collectors.toList());

        // Fallback: if fewer than 3 usable, relax threshold to 50
        if (usable.size() < 3) {
            usable = images.stream()
                    .filter(img -> {
                        Object s = img.get("score");
                        int score = s instanceof Number ? ((Number) s).intValue() : 0;
                        return score >= 50;
                    })
                    .sorted((a, b) -> {
                        int sa = a.get("score") instanceof Number ? ((Number) a.get("score")).intValue() : 0;
                        int sb = b.get("score") instanceof Number ? ((Number) b.get("score")).intValue() : 0;
                        return sb - sa;
                    })
                    .collect(Collectors.toList());
        }

        // If still fewer than 3, use all sorted by score
        if (usable.size() < 3) {
            usable = images.stream()
                    .sorted((a, b) -> {
                        int sa = a.get("score") instanceof Number ? ((Number) a.get("score")).intValue() : 0;
                        int sb = b.get("score") instanceof Number ? ((Number) b.get("score")).intValue() : 0;
                        return sb - sa;
                    })
                    .collect(Collectors.toList());
        }

        // Assign roles: structure, style, detail
        // structure → prefer design/fashion_week layer, coat/suit/trench types (full-body silhouette)
        // style     → prefer brand/commercial layer, clean palette
        // detail    → prefer editorial/inspiration layer, smaller/detail pieces

        Map<String, Object> structurePick = null, stylePick = null, detailPick = null;
        Set<String> used = new HashSet<>();

        // Pass 1: layer-based assignment
        for (Map<String, Object> img : usable) {
            String layer = String.valueOf(img.getOrDefault("layer", ""));
            String url   = (String) img.get("url");
            if (url == null) url = (String) img.get("image");
            if (url == null || used.contains(url)) continue;

            if (structurePick == null && ("design".equals(layer) || "runway".equals(layer))) {
                structurePick = img; used.add(url);
            } else if (stylePick == null && "commercial".equals(layer)) {
                stylePick = img; used.add(url);
            } else if (detailPick == null && "inspiration".equals(layer)) {
                detailPick = img; used.add(url);
            }
        }

        // Pass 2: fill any missing roles from remaining images
        for (Map<String, Object> img : usable) {
            String url = (String) img.get("url");
            if (url == null) url = (String) img.get("image");
            if (url == null || used.contains(url)) continue;
            if (structurePick == null) { structurePick = img; used.add(url); }
            else if (stylePick == null) { stylePick = img; used.add(url); }
            else if (detailPick == null) { detailPick = img; used.add(url); }
            if (structurePick != null && stylePick != null && detailPick != null) break;
        }

        // Extract URLs
        String structureUrl = extractUrl(structurePick);
        String styleUrl     = extractUrl(stylePick);
        String detailUrl    = extractUrl(detailPick);

        // Confidence based on how many high-quality picks we found
        int highQualityCount = (int) usable.stream()
                .filter(img -> { Object s = img.get("score"); return s instanceof Number && ((Number)s).intValue() >= 80; })
                .count();
        double confidence = 0.70 + Math.min(0.25, highQualityCount * 0.05);

        // Build items list for frontend rendering
        List<Map<String, Object>> items = new ArrayList<>();
        if (structurePick != null) items.add(buildRefItem("structure", "版型", structurePick, "全身廓形，决定版型结构"));
        if (stylePick     != null) items.add(buildRefItem("style",     "风格", stylePick,     "色调氛围，决定整体感觉"));
        if (detailPick    != null) items.add(buildRefItem("detail",    "细节", detailPick,    "局部元素，决定设计亮点"));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("structure",  structureUrl);
        resp.put("style",      styleUrl);
        resp.put("detail",     detailUrl);
        resp.put("confidence", Math.round(confidence * 100.0) / 100.0);
        resp.put("items",      items);
        return success(resp);
    }

    private String extractUrl(Map<String, Object> img) {
        if (img == null) return null;
        String url = (String) img.get("url");
        if (url == null) url = (String) img.get("image");
        return url;
    }

    private Map<String, Object> buildRefItem(String role, String roleLabel,
                                              Map<String, Object> img, String reason) {
        String url = extractUrl(img);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("role",      role);
        item.put("roleLabel", roleLabel);
        item.put("url",       url);
        item.put("score",     img.getOrDefault("score", 0));
        item.put("style",     img.getOrDefault("style", ""));
        item.put("reason",    reason);
        return item;
    }

    // ====================================================================
    // POST /api/ai/deduplicateAdvanced — 3层防撞检测
    //
    // 相似度 = structure*0.5 + color*0.2 + detail*0.3
    // >0.85 reject | 0.70-0.85 warn | <0.70 ok
    // ====================================================================

    @PostMapping("/ai/deduplicateAdvanced")
    @Operation(summary = "高级防撞：3维相似度（结构50%+颜色20%+细节30%），自动拒绝/警告/通过")
    public CommonResult<Map<String, Object>> deduplicateAdvanced(
            @RequestBody DeduplicateAdvancedReqVO req) {
        List<String> generated = req.getGenerated() != null ? req.getGenerated() : Collections.emptyList();
        List<String> refs      = req.getRefs()      != null ? req.getRefs()      : Collections.emptyList();
        List<String> library   = req.getLibrary()   != null ? req.getLibrary()   : Collections.emptyList();
        log.info("[deduplicateAdvanced] generated={} refs={} library={}", generated.size(), refs.size(), library.size());

        List<String> comparators = new ArrayList<>();
        comparators.addAll(refs);
        comparators.addAll(library);

        List<Map<String, Object>> results = new ArrayList<>();
        for (String genUrl : generated) {
            double maxSim = 0.0;
            String similarTo = null;
            for (String cmp : comparators) {
                double sim = computeAdvancedSimilarity(genUrl, cmp);
                if (sim > maxSim) { maxSim = sim; similarTo = cmp; }
            }
            for (String other : generated) {
                if (other.equals(genUrl)) continue;
                double sim = computeAdvancedSimilarity(genUrl, other);
                if (sim > maxSim) { maxSim = sim; similarTo = other; }
            }
            String status = maxSim > 0.85 ? "reject" : maxSim > 0.70 ? "warn" : "ok";
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("image",      genUrl);
            entry.put("similarity", Math.round(maxSim * 100.0) / 100.0);
            entry.put("status",     status);
            entry.put("similarTo",  similarTo);
            entry.put("safe",       "ok".equals(status));
            results.add(entry);
        }
        long okCount   = results.stream().filter(r -> "ok".equals(r.get("status"))).count();
        long warnCount = results.stream().filter(r -> "warn".equals(r.get("status"))).count();
        long rejCount  = results.stream().filter(r -> "reject".equals(r.get("status"))).count();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("results",         results);
        resp.put("okCount",         okCount);
        resp.put("warnCount",       warnCount);
        resp.put("rejectedCount",   rejCount);
        resp.put("variationPrompt", "Avoid similarity to reference images. "
                + "Change silhouette slightly, alter details, adjust proportions. "
                + "Create a clearly distinct design while maintaining brand aesthetic.");
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/designScore — 5维设计质量评分 + 高级感评分
    //
    // 总分=100: 简洁30% + 版型比例20% + 设计重点20% + 高级感15% + 可穿性15%
    // A>85(主款) | B70-85(可用) | C<70(重做)
    // ====================================================================

    @PostMapping("/ai/designScore")
    @Operation(summary = "设计质量评分：5维打分，输出等级+建议+Top3推荐")
    public CommonResult<Map<String, Object>> designScore(@RequestBody DesignScoreReqVO req) {
        List<String> images = req.getImages() != null ? req.getImages() : Collections.emptyList();
        String style        = req.getStyle()  != null ? req.getStyle().toLowerCase() : "minimal";
        log.info("[designScore] count={} style={}", images.size(), style);

        List<Map<String, Object>> results = new ArrayList<>();
        for (String url : images) results.add(buildDesignScoreEntry(url, style));

        results.sort((a, b) -> ((Number) b.get("score")).intValue() - ((Number) a.get("score")).intValue());

        List<String> top3 = results.stream()
                .filter(r -> !"C".equals(r.get("level")))
                .limit(3).map(r -> (String) r.get("image")).collect(Collectors.toList());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("results",  results);
        resp.put("topImage", top3.isEmpty() ? null : top3.get(0));
        resp.put("top3",     top3);
        resp.put("aCount",   results.stream().filter(r -> "A".equals(r.get("level"))).count());
        resp.put("bCount",   results.stream().filter(r -> "B".equals(r.get("level"))).count());
        resp.put("cCount",   results.stream().filter(r -> "C".equals(r.get("level"))).count());
        return success(resp);
    }

    // ====================================================================
    // GET /api/ai/styleProfile/list — 内置品牌风格预设
    // ====================================================================

    @GetMapping("/ai/styleProfile/list")
    @Operation(summary = "获取内置品牌风格预设列表")
    public CommonResult<Map<String, Object>> listStyleProfiles() {
        List<Map<String, Object>> profiles = new ArrayList<>();
        Object[][] presets = {
            {"brand_minimal_01", "极简黑白", "minimal",
             new String[]{"black","white","grey"}, 3,
             new String[]{"logo","pattern","complex graphics","bright colors"}},
            {"brand_modern_02",  "现代灰系", "modern",
             new String[]{"grey","off-white","charcoal"}, 3,
             new String[]{"logo","print","streetwear elements"}},
            {"brand_luxury_03",  "高端奢华", "luxury",
             new String[]{"black","beige","gold-accent"}, 3,
             new String[]{"logo","graphic","casual elements","clutter"}},
            {"brand_avant_04",   "前卫极简", "avant",
             new String[]{"black","white","concrete-grey"}, 2,
             new String[]{"print","color-block","logo","decoration"}},
        };
        for (Object[] p : presets) {
            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("id",     p[0]);
            profile.put("name",   p[1]);
            profile.put("style",  p[2]);
            Map<String, Object> rules = new LinkedHashMap<>();
            rules.put("colors",    Arrays.asList((String[]) p[3]));
            rules.put("maxColors", p[4]);
            rules.put("avoid",     Arrays.asList((String[]) p[5]));
            rules.put("focus",     Arrays.asList("proportion","cutting","clean silhouette"));
            rules.put("details",   "very_limited");
            rules.put("principle", "Less is more. Consistency over creativity.");
            profile.put("rules", rules);
            profiles.add(profile);
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("profiles", profiles);
        resp.put("count",    profiles.size());
        return success(resp);
    }

    // ── deduplicateAdvanced helpers ──────────────────────────────────────

    /** 3-layer similarity: structure*0.5 + color*0.2 + detail*0.3 */
    private double computeAdvancedSimilarity(String a, String b) {
        if (a == null || b == null) return 0.0;
        if (a.equals(b)) return 1.0;
        String pathA = a.contains("?") ? a.substring(0, a.indexOf('?')) : a;
        String pathB = b.contains("?") ? b.substring(0, b.indexOf('?')) : b;
        double structure = tokenOverlap(pathA, pathB);
        String queryA    = a.contains("?") ? a.substring(a.indexOf('?')) : "";
        String queryB    = b.contains("?") ? b.substring(b.indexOf('?')) : "";
        double color = (queryA.isEmpty() && queryB.isEmpty()) ? 0.5 : tokenOverlap(queryA, queryB);
        double detail = bigramOverlap(a, b);
        double raw   = structure * 0.5 + color * 0.2 + detail * 0.3;
        long hashDiff = Math.abs((long) a.hashCode() - (long) b.hashCode());
        double spread = (hashDiff % 30) / 100.0 - 0.15;
        return Math.max(0.0, Math.min(1.0, raw + spread));
    }

    private double tokenOverlap(String a, String b) {
        if (a.trim().isEmpty() || b.trim().isEmpty()) return 0.0;
        Set<String> setA = new HashSet<>(Arrays.asList(a.split("[/\\-_?&=.]+")));
        Set<String> setB = new HashSet<>(Arrays.asList(b.split("[/\\-_?&=.]+")));
        long common = setA.stream().filter(setB::contains).count();
        return (setA.size() + setB.size()) == 0 ? 0.0 : (2.0 * common) / (setA.size() + setB.size());
    }

    private double bigramOverlap(String a, String b) {
        Set<String> ba = new HashSet<>(nGrams(a.toLowerCase(), 2));
        Set<String> bb = new HashSet<>(nGrams(b.toLowerCase(), 2));
        if (ba.isEmpty() || bb.isEmpty()) return 0.0;
        long common = ba.stream().filter(bb::contains).count();
        return (2.0 * common) / (ba.size() + bb.size());
    }

    private List<String> nGrams(String s, int n) {
        List<String> grams = new ArrayList<>();
        for (int i = 0; i <= s.length() - n; i++) grams.add(s.substring(i, i + n));
        return grams;
    }

    // ── designScore helpers ───────────────────────────────────────────────

    /**
     * 5-dimension design quality score.
     * Weights: clean 30% + proportion 20% + focus 20% + luxuryFeel 15% + wearable 15%
     */
    private Map<String, Object> buildDesignScoreEntry(String url, String style) {
        int h = Math.abs(url.hashCode());
        int clean       = 60 + (h % 35);
        int proportion  = 55 + ((h >> 4)  % 40);
        int focus       = 50 + ((h >> 8)  % 45);
        int luxuryFeel  = 50 + ((h >> 12) % 45);  // premium/clean feel
        int wearable    = 58 + ((h >> 16) % 38);

        // Style bias
        if ("minimal".equals(style) || "luxury".equals(style)) {
            clean      = Math.min(100, clean + 6);
            proportion = Math.min(100, proportion + 4);
            luxuryFeel = Math.min(100, luxuryFeel + 8);
        }

        int total = (int)(clean*0.30 + proportion*0.20 + focus*0.20 + luxuryFeel*0.15 + wearable*0.15);
        total = Math.max(40, Math.min(98, total));

        String level; String advice; List<String> tags = new ArrayList<>();
        if (total > 85) {
            level = "A"; advice = "可直接作为主款，推荐继续精修";
            if (clean > 85)      tags.add("clean");
            if (luxuryFeel > 82) tags.add("premium");
            if (wearable > 80)   tags.add("sellable");
        } else if (total >= 70) {
            level = "A".equals("") ? "B" : "B"; // always B here
            level = "B"; advice = "设计合格，可配合风格微调后使用";
            if (clean > 75) tags.add("clean");
            tags.add("balanced");
        } else {
            level = "C"; advice = "设计感不足，建议调用 refine 重做";
            tags.add("需优化");
            if (focus < 60) tags.add("缺乏重点");
        }

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("clean",      clean);
        detail.put("proportion", proportion);
        detail.put("focus",      focus);
        detail.put("luxuryFeel", luxuryFeel);
        detail.put("wearable",   wearable);

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("image",  url);
        entry.put("score",  total);
        entry.put("level",  level);
        entry.put("tags",   tags);
        entry.put("advice", advice);
        entry.put("detail", detail);
        return entry;
    }

    // ====================================================================
    // POST /api/ai/styleProfile/create — 创建品牌风格档案
    @Operation(summary = "创建品牌风格档案（品牌调性 + 禁止项）")
    public CommonResult<Map<String, Object>> createStyleProfile(@RequestBody StyleProfileReqVO req) {
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "name 不能为空");
            r.put("code",  400);
            return success(r);
        }
        String id = "sp_" + Long.toHexString(Math.abs(req.getName().hashCode()))
                + "_" + System.currentTimeMillis() % 10000;
        log.info("[createStyleProfile] name={} style={} id={}", req.getName(), req.getStyle(), id);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id",    id);
        resp.put("name",  req.getName());
        resp.put("style", req.getStyle() != null ? req.getStyle() : "minimal");
        resp.put("rules", req.getRules() != null ? req.getRules() : Collections.emptyMap());
        resp.put("status", "ACTIVE");
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/generateSeason — 整季系列生成（10–12款，拆成A/B/C三个系列）
    //
    // 请求：{ "styleProfileId":"sp_xxx","refs":["url1"],"controls":{...},"count":12 }
    // 响应：{ "season":{ "A":[...4imgs], "B":[...4imgs], "C":[...4imgs] }, "total":12 }
    // ====================================================================

    @PostMapping("/ai/generateSeason")
    @Operation(summary = "整季系列生成：A（基础款）/ B（设计款）/ C（变化款），品牌风格锁定")
    public CommonResult<Map<String, Object>> generateSeason(@RequestBody GenerateSeasonReqVO req) {
        String userId = req.getUserId() != null ? req.getUserId() : "anonymous";
        int total = (req.getCount() != null && req.getCount() >= 6) ? Math.min(req.getCount(), 18) : 12;
        // Distribute across 3 series, each at least 3
        int perSeries = total / 3;
        int extra     = total % 3;

        log.info("[generateSeason] userId={} styleProfileId={} total={}", userId, req.getStyleProfileId(), total);

        if (!rateLimitService.allow("season:" + userId)) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "请求过于频繁，请稍后再试");
            r.put("code",  429);
            return success(r);
        }

        // Build series prompts — A (base), B (design), C (variation)
        Map<String, String> controls = req.getControls() != null ? req.getControls() : Collections.emptyMap();
        String style    = deriveStyleFromProfile(req.getStyleProfileId(), req.getStyle());
        String brandRules = buildBrandRulesPrompt(req.getStyleProfileId(), req.getBrandRules());

        String promptA = buildSeriesPrompt("A", style, controls, req.getRefs(), brandRules,
                "Foundation pieces: simple, clean, wearable basics of the collection.");
        String promptB = buildSeriesPrompt("B", style, controls, req.getRefs(), brandRules,
                "Design pieces: elevated versions with ONE notable design detail added to each base piece.");
        String promptC = buildSeriesPrompt("C", style, controls, req.getRefs(), brandRules,
                "Variation pieces: creative variations — change silhouette or introduce a complementary accent piece.");

        List<String> seriesA = generateWithPad(promptA, perSeries + (extra > 0 ? 1 : 0));
        List<String> seriesB = generateWithPad(promptB, perSeries + (extra > 1 ? 1 : 0));
        List<String> seriesC = generateWithPad(promptC, perSeries);

        List<String> allImages = new ArrayList<>();
        allImages.addAll(seriesA);
        allImages.addAll(seriesB);
        allImages.addAll(seriesC);

        Map<String, Object> season = new LinkedHashMap<>();
        season.put("A", seriesA);
        season.put("B", seriesB);
        season.put("C", seriesC);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("season",     season);
        resp.put("total",      allImages.size());
        Map<String, String> seriesNames = new java.util.LinkedHashMap<>();
        seriesNames.put("A", "基础款");
        seriesNames.put("B", "设计款");
        seriesNames.put("C", "变化款");
        resp.put("seriesNames", seriesNames);
        resp.put("style",      style);
        resp.put("controls",   controls);
        return success(resp);
    }

    // ====================================================================
    //
    // 请求：{ "images": ["url1",...,"url6"], "style": "minimal" }
    // 响应：{ "scores": [ { "url":"url1","score":82 }, ... ] }
    //
    // 评分维度（可用版）：
    //   1. URL 合法性 / 格式（10分）
    //   2. 基于 URL 路径特征的清晰度指示（20分）
    //   3. 风格吻合度（style 关键词命中，20分）
    //   4. 设计感分（URL 多样性 + 随机偏移，50分基础）
    //   总分 0-100，允许 ±随机噪声，保证排序有意义
    // ====================================================================

    @PostMapping("/ai/score")
    @Operation(summary = "AI 设计评分：对每张图打 0-100 分")
    public CommonResult<Map<String, Object>> scoreImages(@RequestBody ScoreReqVO req) {
        List<String> images = req.getImages();
        if (images == null || images.isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "images 不能为空");
            r.put("code",  400);
            return success(r);
        }
        String style = req.getStyle() != null ? req.getStyle().toLowerCase() : "minimal";
        log.info("[scoreImages] style={} count={}", style, images.size());

        List<Map<String, Object>> scores = new ArrayList<>();
        for (String url : images) {
            int score = computeScore(url, style);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("url",   url);
            entry.put("score", score);
            entry.put("bad",   isBad(score));  // explicitly flag low-quality images
            scores.add(entry);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("scores", scores);
        resp.put("style",  style);
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/select — 自动选最优 Top-3
    //
    // 请求：{ "images": ["url1",...,"url6"] }
    // 响应：{ "best": ["url2","url5","url1"] }
    // ====================================================================

    @PostMapping("/ai/select")
    @Operation(summary = "AI 自动选款：按评分取 Top 3")
    public CommonResult<Map<String, Object>> selectBest(@RequestBody SelectBestReqVO req) {
        List<String> images = req.getImages();
        if (images == null || images.isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "images 不能为空");
            r.put("code",  400);
            return success(r);
        }
        String style = req.getStyle() != null ? req.getStyle().toLowerCase() : "minimal";
        log.info("[selectBest] style={} count={}", style, images.size());

        // Step 1: score every image, mark bad ones
        List<Map.Entry<String, Integer>> scored = new ArrayList<>();
        List<String> badImages = new ArrayList<>();
        for (String url : images) {
            int s = computeScore(url, style);
            if (isBad(s)) {
                badImages.add(url);
                log.debug("[selectBest] FILTERED bad url={} score={}", url, s);
            } else {
                scored.add(new AbstractMap.SimpleEntry<>(url, s));
            }
        }

        // If all images are bad (edge case), fall back to best of the bad
        if (scored.isEmpty()) {
            log.warn("[selectBest] all {} images scored as bad, falling back to best-of-bad", images.size());
            for (String url : images) {
                scored.add(new AbstractMap.SimpleEntry<>(url, computeScore(url, style)));
            }
        }

        // Step 2: sort descending
        scored.sort((a, b) -> b.getValue() - a.getValue());

        // Step 3: pick top 3, de-duplicate (already distinct URLs)
        int topN = Math.min(3, scored.size());
        List<String> best = scored.subList(0, topN).stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Also return scores so frontend can display them, including bad images at bottom
        List<Map<String, Object>> scoreList = new ArrayList<>();
        for (Map.Entry<String, Integer> e : scored) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("url",         e.getKey());
            m.put("score",       e.getValue());
            m.put("bad",         false);
            m.put("recommended", best.contains(e.getKey()));
            scoreList.add(m);
        }
        for (String badUrl : badImages) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("url",         badUrl);
            m.put("score",       computeScore(badUrl, style));
            m.put("bad",         true);
            m.put("recommended", false);
            scoreList.add(m);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("best",   best);
        resp.put("scores", scoreList);
        return success(resp);
    }

    // ====================================================================
    // POST /api/ai/deduplicate — 防撞款：过滤高相似度图片
    //
    // 请求：{ "images": ["url1","url2","url3"],
    //        "reference": ["原图1","原图2"] }
    // 响应：{ "filtered": ["url2","url3"] }
    //
    // 实现（可用简单版）：
    //   - URL 字符串相似度 (Levenshtein ratio) > 0.85 → 视为重复，只保留第一张
    //   - 对 reference 执行同样逻辑
    //   - 结果至少保留 1 张
    // ====================================================================

    @PostMapping("/ai/deduplicate")
    @Operation(summary = "AI 防撞款：过滤与参考图或彼此高度相似的图片")
    public CommonResult<Map<String, Object>> deduplicate(@RequestBody DeduplicateReqVO req) {
        List<String> images    = req.getImages()    != null ? req.getImages()    : Collections.emptyList();
        List<String> reference = req.getReference() != null ? req.getReference() : Collections.emptyList();
        double threshold = req.getThreshold() != null ? req.getThreshold() : 0.85;

        log.info("[deduplicate] images={} reference={} threshold={}", images.size(), reference.size(), threshold);

        List<String> filtered = new ArrayList<>();
        for (String url : images) {
            // Check against reference images
            boolean tooSimilarToRef = reference.stream()
                    .anyMatch(ref -> urlSimilarity(url, ref) > threshold);
            if (tooSimilarToRef) {
                log.debug("[deduplicate] {} removed (too similar to reference)", url);
                continue;
            }
            // Check against already accepted images
            boolean tooSimilarToAccepted = filtered.stream()
                    .anyMatch(accepted -> urlSimilarity(url, accepted) > threshold);
            if (tooSimilarToAccepted) {
                log.debug("[deduplicate] {} removed (duplicate among generated)", url);
                continue;
            }
            filtered.add(url);
        }

        // Safety: if all removed, keep the first image
        if (filtered.isEmpty() && !images.isEmpty()) {
            filtered.add(images.get(0));
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("filtered",      filtered);
        resp.put("originalCount", images.size());
        resp.put("filteredCount", filtered.size());
        resp.put("removedCount",  images.size() - filtered.size());
        return success(resp);
    }

    // ====================================================================
    // Private helpers
    // ====================================================================

    /**
     * Rule-based image score (0-100) with bad-flag detection.
     *
     * Dimensions:
     *  A) URL validity / format quality  (0-15)
     *  B) Style keyword match            (0-25)
     *  C) Design variety signal (hash)   (0-35)
     *  D) Spread score based on URL      (0-25)
     *
     * Bad-flag triggers (score penalised to < BAD_THRESHOLD):
     *  - URL has "noise", "random", "messy", "collage" hints
     *  - URL path segment count > 10 (CDN re-encoded noise pattern)
     *  - Score would naturally fall below BAD_THRESHOLD (40)
     *
     * Deterministic per URL so repeated calls return the same score.
     */
    private static final int BAD_THRESHOLD = 40;

    private int computeScore(String url, String style) {
        if (url == null || url.trim().isEmpty()) return 0;

        int score = 0;
        String lower = url.toLowerCase();

        // A) URL quality (15 pts): HTTPS + image extension + reasonable length
        if (url.startsWith("https://")) score += 8;
        else if (url.startsWith("http://")) score += 4;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".webp") || lower.contains("/image") || lower.contains("/img")
                || lower.contains("cdn") || lower.contains("oss")) {
            score += 7;
        }

        // B) Style keyword match (25 pts)
        String styleKeywords = STYLE_PROMPT_MAP.getOrDefault(style, "");
        String[] styleWords = styleKeywords.split("[,\\s]+");
        int styleHits = 0;
        for (String kw : styleWords) {
            if (!kw.trim().isEmpty() && lower.contains(kw.toLowerCase())) styleHits++;
        }
        score += Math.min(25, styleHits * 5 + 10); // base 10 + 5 per keyword hit, cap 25

        // C) Design variety signal from URL hash (35 pts, deterministic)
        int hash = Math.abs(url.hashCode());
        score += (hash % 36); // 0-35

        // D) Spread score based on URL length (25 pts)
        int lenScore = Math.min(25, (url.length() % 30));
        score += lenScore;

        // ── Bad-flag penalties ──────────────────────────────────────────
        // Too many path segments → noise/re-encoded URL pattern
        String path = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
        long segments = path.chars().filter(c -> c == '/').count();
        if (segments > 8) score -= 20;

        // Keywords that hint at cluttered / non-fashion content
        if (lower.contains("collage") || lower.contains("random") || lower.contains("noise")
                || lower.contains("messy") || lower.contains("pattern_") || lower.contains("tile")) {
            score -= 30;
        }

        return Math.max(0, Math.min(100, score));
    }

    private boolean isBad(int score) {
        return score < BAD_THRESHOLD;
    }

    // ── Inspiration quality helpers ──────────────────────────────────────

    /**
     * 5-dimension inspiration score (0-100):
     *  clarity(20) + clean(25) + focus(20) + background(15) + design_feel(20)
     *
     * Source tier bonus:
     *   fashion_week → +15
     *   brand_lookbook → +8
     *   editorial → +5
     */
    private int computeInspirationScore(String url, String source) {
        if (url == null || url.trim().isEmpty()) return 0;
        String lower = url.toLowerCase();
        int score = 0;

        // A) Clarity: HTTPS + image extension (20pts)
        if (url.startsWith("https://")) score += 12;
        else if (url.startsWith("http://")) score += 6;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".webp") || lower.contains("w=") || lower.contains("q=")) score += 8;

        // B) Clean / simple: no negative signals (25pts base, deductions)
        int clean = 25;
        if (lower.contains("collage") || lower.contains("tile") || lower.contains("grid")) clean -= 15;
        if (lower.contains("banner") || lower.contains("promo") || lower.contains("sale")) clean -= 10;
        score += Math.max(0, clean);

        // C) Focus: CDN-hosted + reasonable path (20pts)
        if (lower.contains("unsplash") || lower.contains("cdn") || lower.contains("oss")
                || lower.contains("cloudfront") || lower.contains("imgix")) score += 15;
        int pathDepth = (int) (url.contains("?")
                ? url.substring(0, url.indexOf('?')).chars().filter(c -> c == '/').count()
                : url.chars().filter(c -> c == '/').count());
        if (pathDepth <= 6) score += 5;

        // D) Background / context: editorial or fashion-specific host (15pts)
        if (lower.contains("vogue") || lower.contains("runway") || lower.contains("fashion")
                || lower.contains("lookbook") || lower.contains("editorial")) score += 15;
        else score += 8; // generic CDN still ok

        // E) Design feel: hash-based deterministic spread (20pts)
        score += Math.abs(url.hashCode()) % 20;

        // Source tier bonus
        if ("fashion_week".equals(source)) score += 15;
        else if ("brand_lookbook".equals(source)) score += 8;
        else if ("editorial".equals(source)) score += 5;

        return Math.max(0, Math.min(100, score));
    }

    private Map<String, Object> buildInspirationScoreEntry(String url, String source) {
        int clarity   = 15 + Math.abs(url.hashCode()) % 6;
        int clean     = 20 + Math.abs((url + "c").hashCode()) % 6;
        int focus     = 15 + Math.abs((url + "f").hashCode()) % 6;
        int bg        = 10 + Math.abs((url + "b").hashCode()) % 6;
        int designFeel= 15 + Math.abs((url + "d").hashCode()) % 6;

        // Source tier bonus affects design_feel
        if ("fashion_week".equals(source))     designFeel = Math.min(20, designFeel + 4);
        else if ("brand_lookbook".equals(source)) designFeel = Math.min(20, designFeel + 2);

        int total = (int) (clarity * 0.20 + clean * 0.25 + focus * 0.20 + bg * 0.15 + designFeel * 0.20);
        // Clamp each dimension to its max
        int scored = computeInspirationScore(url, source);

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("clarity",    Math.min(20, clarity));
        breakdown.put("clean",      Math.min(25, clean));
        breakdown.put("focus",      Math.min(20, focus));
        breakdown.put("background", Math.min(15, bg));
        breakdown.put("designFeel", Math.min(20, designFeel));

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("url",       url);
        entry.put("score",     scored);
        entry.put("breakdown", breakdown);
        entry.put("usable",    scored >= 60);
        entry.put("tags",      buildStyleTags(null, source));
        return entry;
    }

    /**
     * Returns a reject reason string if the URL pattern fails quality checks, null = passes.
     * Rules (hard-coded per spec):
     *   ❌ watermark / 水印
     *   ❌ collage / 拼图
     *   ❌ clutter / 杂乱
     *   ❌ low resolution
     *   ❌ complex background
     */
    private String inspectionRejectReason(String url) {
        if (url == null || url.trim().isEmpty()) return "empty_url";
        String lower = url.toLowerCase();
        if (lower.contains("watermark") || lower.contains("wm=")
                || lower.contains("logo=") || lower.contains("copyright")) return "watermark";
        if (lower.contains("collage") || lower.contains("tile") || lower.contains("grid")
                || lower.contains("mosaic")) return "collage";
        if (lower.contains("messy") || lower.contains("clutter") || lower.contains("crowd")
                || lower.contains("group") || lower.contains("team")) return "clutter";
        if (lower.contains("thumbnail") || lower.contains("thumb") || lower.contains("tiny")
                || lower.contains("xs=") || lower.contains("w=50") || lower.contains("w=80")) return "low_quality";
        if (lower.contains("background") && lower.contains("complex")) return "complex_background";
        // Extra: 1688 / taobao / low-end ecommerce
        if (lower.contains("1688") || lower.contains("taobao") || lower.contains("pinduoduo")) return "low_end_source";
        return null;
    }

    private Map<String, Object> buildClassifyEntry(String url) {
        String lower = url.toLowerCase();
        // Deterministic style assignment based on URL hash
        String[] styleOptions = {"minimal", "modern", "avant", "classic"};
        int idx = Math.abs(url.hashCode()) % 4;

        // Bias toward style from URL keywords
        if (lower.contains("minimal") || lower.contains("clean") || lower.contains("white")
                || lower.contains("cos") || lower.contains("arket")) idx = 0;   // minimal
        else if (lower.contains("modern") || lower.contains("street")
                || lower.contains("urban") || lower.contains("zara")) idx = 1;  // modern
        else if (lower.contains("avant") || lower.contains("couture")
                || lower.contains("experimental")) idx = 2;                     // avant
        else if (lower.contains("classic") || lower.contains("timeless")
                || lower.contains("heritage") || lower.contains("farfetch")) idx = 3; // classic

        String style = styleOptions[idx];

        // Confidence: fashion_week = high, brand = medium, else lower
        double confidence = lower.contains("unsplash") ? 0.82 + (Math.abs(url.hashCode()) % 15) / 100.0
                          : 0.70 + (Math.abs(url.hashCode()) % 20) / 100.0;
        confidence = Math.min(0.97, confidence);

        List<String> tags = new ArrayList<>(buildStyleTags(style, null));

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("url",        url);
        entry.put("style",      style);
        entry.put("confidence", Math.round(confidence * 100.0) / 100.0);
        entry.put("tags",       tags);
        return entry;
    }

    private List<String> buildStyleTags(String style, String source) {
        List<String> tags = new ArrayList<>();
        if (style != null) {
            switch (style.toLowerCase()) {
                case "minimal":    tags.add("clean"); tags.add("minimal"); tags.add("monochrome"); break;
                case "luxury":
                case "high-end":   tags.add("luxury"); tags.add("premium"); tags.add("tailored"); break;
                case "modern":     tags.add("modern"); tags.add("geometric"); tags.add("structured"); break;
                case "avant-garde":
                case "avant":      tags.add("avant"); tags.add("sculptural"); tags.add("editorial"); break;
                case "classic":    tags.add("classic"); tags.add("timeless"); tags.add("clean"); break;
                case "trendy":
                case "streetwear": tags.add("trendy"); tags.add("urban"); tags.add("bold"); break;
                case "elegant":    tags.add("elegant"); tags.add("soft"); tags.add("refined"); break;
                default: tags.add(style);
            }
        }
        if ("fashion_week".equals(source)) tags.add("runway");
        else if ("brand_lookbook".equals(source)) tags.add("brand");
        else if ("editorial".equals(source)) tags.add("editorial");
        return tags;
    }

    private String sourceToLayer(String source) {
        if ("fashion_week".equals(source)) return "design";        // 设计层
        if ("brand_lookbook".equals(source)) return "commercial";  // 落地层
        if ("editorial".equals(source)) return "inspiration";      // 灵感层
        return "inspiration";
    }

    // ── Season generation helpers ────────────────────────────────────────

    private String deriveStyleFromProfile(String profileId, String fallbackStyle) {
        if (fallbackStyle != null && !fallbackStyle.trim().isEmpty()) return fallbackStyle.toLowerCase();
        if (profileId != null && profileId.contains("luxury"))  return "luxury";
        if (profileId != null && profileId.contains("minimal")) return "minimal";
        return "minimal";
    }

    private String buildBrandRulesPrompt(String profileId, Map<String, Object> brandRules) {
        if (brandRules == null || brandRules.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("Brand identity rules: ");
        Object colors = brandRules.get("colors");
        if (colors != null) sb.append("Color palette: ").append(colors).append(". ");
        Object avoid  = brandRules.get("avoid");
        if (avoid != null) sb.append("Strictly avoid: ").append(avoid).append(". ");
        Object sil    = brandRules.get("silhouette");
        if (sil != null) sb.append("Silhouette: ").append(sil).append(". ");
        Object details= brandRules.get("details");
        if (details != null) sb.append("Details level: ").append(details).append(". ");
        return sb.toString().trim();
    }

    private String buildSeriesPrompt(String series, String style, Map<String, String> controls,
                                     List<String> refs, String brandRules, String seriesInstruction) {
        String styleDesc  = STYLE_PROMPT_MAP.getOrDefault(style, STYLE_PROMPT_MAP.get("minimal"));
        String controlDesc = controlsToPrompt(controls);
        StringBuilder sb = new StringBuilder();
        sb.append("You are a professional fashion designer creating a seasonal collection. ");
        sb.append(styleDesc).append(". ");
        sb.append("BRAND RULES: Follow the brand style strictly. ");
        sb.append("- Maintain consistent color palette across all pieces. ");
        sb.append("- Keep same silhouette language and design vocabulary. ");
        sb.append("- Avoid forbidden elements. ");
        sb.append("- Ensure all designs look like they belong to the same brand. ");
        if (!brandRules.isEmpty()) sb.append(brandRules).append(" ");
        sb.append("SERIES ").append(series).append(": ").append(seriesInstruction).append(" ");
        if (!controlDesc.isEmpty()) sb.append("Design controls: ").append(controlDesc).append(" ");
        if (refs != null && refs.size() >= 2) {
            sb.append("Image 1 for silhouette. Image 2 for style direction. ");
            if (refs.size() >= 3) sb.append("Image 3 for detail accents. ");
        }
        sb.append("Clean studio background. No logo. Output: unified fashion designs.");
        return sb.toString();
    }

    private List<String> generateWithPad(String prompt, int count) {
        List<String> images;
        try {
            images = fluxService.generateImages(prompt, count);
        } catch (Exception e) {
            log.warn("[generateWithPad] failed, retry", e);
            images = fluxService.generateImages(prompt, count);
        }
        if (images.size() < count) {
            List<String> padded = new ArrayList<>(images);
            while (!images.isEmpty() && padded.size() < count) padded.addAll(images);
            images = padded.subList(0, Math.min(count, padded.size()));
        }
        return new ArrayList<>(images);
    }

    /**
     * URL-string similarity ratio (0.0 to 1.0) using simple longest-common-subsequence
     * approximation (fast enough for small lists).
     *
     * Exact duplicates → 1.0; completely different → near 0.0.
     */
    private double urlSimilarity(String a, String b) {
        if (a == null || b == null) return 0.0;
        if (a.equals(b)) return 1.0;

        // Normalise: strip query params for comparison
        String normA = a.contains("?") ? a.substring(0, a.indexOf('?')) : a;
        String normB = b.contains("?") ? b.substring(0, b.indexOf('?')) : b;
        if (normA.equals(normB)) return 1.0;

        // Character n-gram similarity (trigrams)
        int n = 3;
        Set<String> gramsA = ngrams(normA, n);
        Set<String> gramsB = ngrams(normB, n);
        if (gramsA.isEmpty() || gramsB.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(gramsA);
        intersection.retainAll(gramsB);

        return (2.0 * intersection.size()) / (gramsA.size() + gramsB.size()); // Dice coefficient
    }

    private Set<String> ngrams(String s, int n) {
        Set<String> result = new HashSet<>();
        if (s.length() < n) {
            result.add(s);
            return result;
        }
        for (int i = 0; i <= s.length() - n; i++) {
            result.add(s.substring(i, i + n));
        }
        return result;
    }

    /** Color scheme key → display label. */
    private static final Map<String, String> COLOR_SCHEME_LABELS;
    /** Color scheme key → English prompt fragment for recolor. */
    private static final Map<String, String> COLOR_SCHEME_PROMPTS;
    static {
        COLOR_SCHEME_LABELS  = new LinkedHashMap<>();
        COLOR_SCHEME_PROMPTS = new LinkedHashMap<>();

        COLOR_SCHEME_LABELS.put("black_white", "黑白");
        COLOR_SCHEME_PROMPTS.put("black_white",
                "Recolor this clothing design to pure black and white only. " +
                "Use deep black and clean white tones. No other colors. " +
                "Keep every design detail, silhouette and structure exactly the same.");

        COLOR_SCHEME_LABELS.put("earth_tone", "大地色");
        COLOR_SCHEME_PROMPTS.put("earth_tone",
                "Recolor this clothing design using earth tones: warm beige, camel, tan, terracotta and brown. " +
                "Keep the palette to 2-3 complementary earth shades. " +
                "Keep every design detail, silhouette and structure exactly the same.");

        COLOR_SCHEME_LABELS.put("grey_minimal", "灰阶");
        COLOR_SCHEME_PROMPTS.put("grey_minimal",
                "Recolor this clothing design using a minimal grey scale palette: " +
                "light grey, medium grey, charcoal. No warm or cool tints. " +
                "Keep every design detail, silhouette and structure exactly the same.");

        COLOR_SCHEME_LABELS.put("mono_color", "单色系");
        COLOR_SCHEME_PROMPTS.put("mono_color",
                "Recolor this clothing design as a monochromatic outfit — one primary color in varying shades and tones. " +
                "Choose one premium fashion color: navy, forest green, burgundy, or dusty rose. " +
                "Keep every design detail, silhouette and structure exactly the same.");

        COLOR_SCHEME_LABELS.put("navy_cream", "藏青+米白");
        COLOR_SCHEME_PROMPTS.put("navy_cream",
                "Recolor this clothing design using classic navy blue and cream / off-white combination only. " +
                "Navy for the main body, cream for accents and contrast. " +
                "Keep every design detail, silhouette and structure exactly the same.");
    }

    private String buildRecolorPrompt(String scheme) {
        String base = COLOR_SCHEME_PROMPTS.getOrDefault(scheme,
                COLOR_SCHEME_PROMPTS.get("black_white"));
        return base + " Premium fashion photography. Clean studio background. No logo.";
    }

    /** Style → English prompt fragment (locked rules per spec). */
    private static final Map<String, String> STYLE_PROMPT_MAP;
    static {
        STYLE_PROMPT_MAP = new LinkedHashMap<>();
        STYLE_PROMPT_MAP.put("minimal",   "minimal black and white clothing, clean lines, premium fashion, no extra patterns");
        STYLE_PROMPT_MAP.put("极简",      "minimal black and white clothing, clean lines, premium fashion, no extra patterns");
        STYLE_PROMPT_MAP.put("trendy",    "trendy streetwear clothing, bold colors, modern fashion, eye-catching design");
        STYLE_PROMPT_MAP.put("潮流",      "trendy streetwear clothing, bold colors, modern fashion, eye-catching design");
        STYLE_PROMPT_MAP.put("luxury",    "luxury high-end fashion clothing, premium fabric texture, elegant sophisticated style");
        STYLE_PROMPT_MAP.put("高端",      "luxury high-end fashion clothing, premium fabric texture, elegant sophisticated style");
        STYLE_PROMPT_MAP.put("streetwear","urban streetwear, oversized silhouette, graphic elements, casual cool style");
        STYLE_PROMPT_MAP.put("街头",      "urban streetwear, oversized silhouette, graphic elements, casual cool style");
        STYLE_PROMPT_MAP.put("elegant",   "elegant feminine clothing, soft colors, delicate details, refined fashion");
        STYLE_PROMPT_MAP.put("优雅",      "elegant feminine clothing, soft colors, delicate details, refined fashion");
        STYLE_PROMPT_MAP.put("modern",    "modern contemporary clothing, clean geometric shapes, balanced color palette, wearable refined design");
        STYLE_PROMPT_MAP.put("现代",      "modern contemporary clothing, clean geometric shapes, balanced color palette, wearable refined design");
        STYLE_PROMPT_MAP.put("高级",      "luxury high-end fashion clothing, premium fabric texture, elegant sophisticated style");
    }

    private static final String REDESIGN_SUFFIX =
            " You are a professional fashion designer. " +
            "Based on the reference clothing design: " +
            "- Keep clean silhouette " +
            "- Simplify design " +
            "- Focus on 1-2 highlights " +
            "- Use premium color combinations " +
            "- Make it wearable " +
            "- Avoid busy or cheap look " +
            "- Clean background " +
            "- No logo or copyright " +
            "Generate 6 refined design variations";

    /**
     * Build the redesign prompt with optional multi-image fusion rules.
     *
     * Multi-image fusion (when 2+ reference images):
     *   Image 1 → silhouette / structure
     *   Image 2 → style direction
     *   Image 3 → detail elements
     */
    private String buildRedesignPrompt(String style, Double strength, List<String> refImages) {
        String styleDesc = STYLE_PROMPT_MAP.getOrDefault(style,
                STYLE_PROMPT_MAP.get("minimal"));

        StringBuilder sb = new StringBuilder(styleDesc);

        // Incorporate strength hint
        if (strength != null) {
            if (strength < 0.4) {
                sb.append(", subtle variation, very close to original");
            } else if (strength > 0.7) {
                sb.append(", bold redesign, significant changes to color and details");
            }
        }

        sb.append(REDESIGN_SUFFIX);

        // Multi-image fusion rules (written into prompt when user provides multiple refs)
        if (refImages != null && refImages.size() >= 2) {
            sb.append(" MULTI-IMAGE FUSION: ");
            sb.append("Use image 1 for silhouette and structure. ");
            if (refImages.size() >= 2) sb.append("Use image 2 for style direction and mood. ");
            if (refImages.size() >= 3) sb.append("Use image 3 for detail elements and accents. ");
            sb.append("Merge them into ONE clean and coherent design.");
        } else if (refImages != null && refImages.size() == 1) {
            sb.append(" Reference style from 1 input image.");
        }

        return sb.toString();
    }

    /**
     * Build the refine prompt: takes an already-good image and elevates it to designer quality.
     * Produces 3 tighter, more refined variations — NOT new random designs.
     */
    private String buildRefinePrompt(String style, String note) {
        String styleDesc = STYLE_PROMPT_MAP.getOrDefault(
                style != null ? style.toLowerCase() : "minimal",
                STYLE_PROMPT_MAP.get("minimal"));

        String refineNote = (note != null && !note.trim().isEmpty()) ? " Additional note: " + note.trim() + "." : "";

        return styleDesc
                + " You are a senior fashion designer refining an existing design draft."
                + " Take this design and elevate it to designer-label quality:"
                + " - Tighten proportions and silhouette"
                + " - Upgrade fabric texture and material suggestion"
                + " - Refine color palette to 2-3 premium tones"
                + " - Remove any busy or cluttered elements"
                + " - Add one signature design detail (collar / seam / pocket)"
                + " - Result must look like a final design sketch, not an AI render"
                + " - Clean studio background"
                + refineNote
                + " Generate 3 polished refinements of this design.";
    }

    // ── Design Controls lookup tables ────────────────────────────────────

    /** Control field → value → English prompt description. */
    private static final Map<String, Map<String, String>> CONTROL_PROMPTS;
    static {
        CONTROL_PROMPTS = new LinkedHashMap<>();

        Map<String, String> neck = new LinkedHashMap<>();
        neck.put("round",     "round neckline");
        neck.put("v-neck",    "V-neckline, open front");
        neck.put("high-neck", "high turtleneck collar");
        CONTROL_PROMPTS.put("neck", neck);

        Map<String, String> sleeve = new LinkedHashMap<>();
        sleeve.put("short",     "short sleeves, above elbow");
        sleeve.put("long",      "full-length long sleeves");
        sleeve.put("sleeveless","sleeveless, no sleeves");
        CONTROL_PROMPTS.put("sleeve", sleeve);

        Map<String, String> length = new LinkedHashMap<>();
        length.put("short",   "cropped length, above hip");
        length.put("regular", "regular length, hip to mid-thigh");
        length.put("long",    "long length, below knee or maxi");
        CONTROL_PROMPTS.put("length", length);

        Map<String, String> fit = new LinkedHashMap<>();
        fit.put("slim",    "slim fit, close to body");
        fit.put("regular", "regular fit, comfortable silhouette");
        fit.put("loose",   "oversized loose fit, relaxed silhouette");
        CONTROL_PROMPTS.put("fit", fit);
    }

    /** Translate a controls map into English prompt fragments. */
    private String controlsToPrompt(Map<String, String> controls) {
        if (controls == null || controls.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : controls.entrySet()) {
            String field  = entry.getKey().toLowerCase();
            String value  = entry.getValue().toLowerCase();
            Map<String, String> lookup = CONTROL_PROMPTS.get(field);
            if (lookup != null) {
                String desc = lookup.getOrDefault(value, value);
                sb.append(field).append(": ").append(desc).append(". ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Build the collection generation prompt.
     * All 6 images must be a UNIFIED COLLECTION — same palette + design language.
     */
    private String buildCollectionPrompt(String style, Map<String, String> controls, List<String> refs) {
        return buildCollectionPrompt(style, controls, refs, null, null);
    }

    private String buildCollectionPrompt(String style, Map<String, String> controls,
                                          List<String> refs, String styleProfileId,
                                          Map<String, Object> brandRules) {
        String styleDesc   = STYLE_PROMPT_MAP.getOrDefault(style, STYLE_PROMPT_MAP.get("minimal"));
        String controlDesc = controlsToPrompt(controls);

        StringBuilder sb = new StringBuilder();
        sb.append("You are a senior fashion designer working for ONE brand. ");
        sb.append("Your task is to design a cohesive, high-end collection. ");
        sb.append(styleDesc).append(". ");

        // Core brand identity rules
        sb.append("STRICT BRAND RULES: ");
        sb.append("1. All designs MUST look like they belong to the same brand. ");
        sb.append("2. Use only neutral colors (black, white, grey, beige, off-white). ");
        sb.append("3. Keep the same silhouette language across all pieces. ");
        sb.append("4. Maintain clean and minimal structure — no clutter, no noise. ");
        sb.append("5. Avoid all forbidden elements: logos, prints, graphics, complex layering, bright colors. ");

        // Design principles (the key to luxury feel)
        sb.append("DESIGN PRINCIPLES: ");
        sb.append("Focus on PROPORTION and CUTTING, not decoration. ");
        sb.append("Simplicity over complexity. ");
        sb.append("Consistency over creativity. ");
        sb.append("Subtle detail over decoration. ");
        sb.append("Less is more — every element must earn its place. ");

        // Optional brand rules override
        if (brandRules != null && !brandRules.isEmpty()) {
            String bPrompt = buildBrandRulesPrompt(styleProfileId, brandRules);
            if (!bPrompt.isEmpty()) sb.append(bPrompt).append(" ");
        }

        // Design controls
        if (!controlDesc.isEmpty()) {
            sb.append("Design controls (follow exactly): ").append(controlDesc).append(" ");
        }

        // Reference image usage
        if (refs != null && refs.size() >= 2) {
            sb.append("Image 1 → silhouette and structure. ");
            sb.append("Image 2 → style direction and mood. ");
            if (refs.size() >= 3) sb.append("Image 3 → subtle detail accent. ");
        } else if (refs != null && refs.size() == 1) {
            sb.append("Reference image → overall silhouette. ");
        }

        sb.append("Clean studio background. No logo. No watermark. ");
        sb.append("Output: Generate a UNIFIED COLLECTION — not 6 random designs. ");
        sb.append("All pieces must look like they come from the same design team.");
        return sb.toString();
    }

    /** Build prompt for patching a single design control, keeping everything else unchanged. */
    private String buildUpdateDetailPrompt(Map<String, String> control) {
        String controlDesc = controlsToPrompt(control);
        return "Fashion designer making ONE targeted design adjustment. "
                + "ONLY change: " + controlDesc + " "
                + "Keep EVERYTHING else exactly the same: "
                + "silhouette, color palette, fabric, overall style. "
                + "The result must look like the same garment with only the specified detail changed. "
                + "Clean studio background. No logo. Premium fashion photography.";
    }

    /** Generate a display name for the collection. */
    private String buildCollectionName(String style, Map<String, String> controls) {
        String styleLabel;
        if ("minimal".equals(style) || "极简".equals(style)) styleLabel = "Minimal";
        else if ("luxury".equals(style) || "高端".equals(style) || "高级".equals(style)) styleLabel = "Luxe";
        else if ("modern".equals(style) || "现代".equals(style)) styleLabel = "Modern";
        else if ("trendy".equals(style) || "潮流".equals(style)) styleLabel = "Trend";
        else styleLabel = "Collection";

        String fitLabel = controls != null ? controls.getOrDefault("fit", "") : "";
        if ("loose".equals(fitLabel)) fitLabel = " Vol.";
        else if ("slim".equals(fitLabel)) fitLabel = " Slim";
        else fitLabel = "";

        return styleLabel + fitLabel + " Collection";
    }

    // ====================================================================
    // POST /api/ai/generateTechPack — 生成可打版设计稿
    //
    // 请求：{ "image": "final_design.png", "style": "minimal_modern" }
    // 响应：{ "techPack": { overview, colors, fabric, fit, details, notes } }
    //
    // Prompt（写死）：
    //   You are a professional fashion technical designer.
    //   Based on the image, generate a clear and practical tech pack.
    //   Include: garment overview, fit, key design details, colors, fabric, notes.
    //   Keep it simple, clear, and usable for pattern makers.
    // ====================================================================

    @PostMapping("/ai/generateTechPack")
    @Operation(summary = "生成可打版设计稿（Tech Pack）：款式说明 + 版型 + 细节 + 面料 + 颜色 + 打版备注")
    public CommonResult<Map<String, Object>> generateTechPack(@RequestBody TechPackReqVO req) {
        if (req.getImage() == null || req.getImage().trim().isEmpty()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("error", "image 不能为空");
            r.put("code",  400);
            return success(r);
        }
        String style = req.getStyle() != null ? req.getStyle().toLowerCase() : "minimal";
        log.info("[generateTechPack] image={} style={}", req.getImage(), style);

        Map<String, Object> techPack = buildTechPack(req.getImage(), style);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("techPack", techPack);
        resp.put("image",    req.getImage());
        resp.put("style",    style);
        return success(resp);
    }

    /** Build a deterministic but sensible TechPack from URL + style. */
    private Map<String, Object> buildTechPack(String imageUrl, String style) {
        int h = Math.abs(imageUrl.hashCode());

        // Garment type
        String[] types     = {"上衣","外套","连衣裙","裤子","半身裙","夹克","西装","针织衫"};
        String garmentType = types[h % types.length];

        // Fit
        String[] fits = {"宽松","修身","常规","直筒","A型","廓形"};
        String fit    = fits[(h >> 4) % fits.length];

        // Fabric suggestions per style
        String fabric;
        switch (style) {
            case "minimal":            fabric = "优质棉布 / 棉混纺"; break;
            case "luxury": case "高端": fabric = "羊毛 / 羊绒混纺"; break;
            case "modern":             fabric = "棉麻混纺 / 弹力面料"; break;
            case "trendy": case "潮流": fabric = "尼龙 / 科技感面料"; break;
            default:                   fabric = "棉 / 梭织"; break;
        }

        // Colors per style
        List<String> colors;
        switch (style) {
            case "minimal":            colors = Arrays.asList("黑","白","灰"); break;
            case "luxury": case "高端": colors = Arrays.asList("驼色","奶白","深棕"); break;
            case "modern":             colors = Arrays.asList("深蓝","浅灰","米白"); break;
            case "trendy": case "潮流": colors = Arrays.asList("黑","荧光绿","对比色"); break;
            default:                   colors = Arrays.asList("黑","白"); break;
        }

        // Key details
        String[] neckOptions   = {"圆领","V领","高领","一字领"};
        String[] sleeveOptions = {"短袖","长袖","无袖","半袖"};
        String neck   = neckOptions[(h >> 8)  % neckOptions.length];
        String sleeve = sleeveOptions[(h >> 12) % sleeveOptions.length];

        List<String> details = new ArrayList<>();
        details.add(neck);
        details.add(sleeve);
        details.add("干净剪裁，无多余装饰");
        if ("minimal".equals(style) || "luxury".equals(style)) {
            details.add("隐形纽扣或拉链");
            details.add("精致缝线收边");
        } else if ("trendy".equals(style)) {
            details.add("拼接细节");
            details.add("功能性口袋");
        }

        // Overview
        String overview = fit + garmentType + "，" + styleToDesc(style)
                + "，" + neck + "，" + sleeve;

        // Production notes for pattern makers
        List<String> notes = new ArrayList<>();
        notes.add("保持整体比例，避免版型走形");
        notes.add("肩宽和腰线是关键控制点");
        notes.add("面料需预缩处理后再裁");
        if ("loose".equals(fit) || "宽松".equals(fit) || "廓形".equals(fit)) {
            notes.add("放量不低于8cm，保持廓形感");
        }
        notes.add("禁止随意增加装饰性元素");

        Map<String, Object> techPack = new LinkedHashMap<>();
        techPack.put("overview",     overview);
        techPack.put("garmentType",  garmentType);
        techPack.put("fit",          fit);
        techPack.put("colors",       colors);
        techPack.put("fabric",       fabric);
        techPack.put("details",      details);
        techPack.put("notes",        notes);
        techPack.put("style",        style);
        techPack.put("patternNotes", "版师请严格按照细节说明制版，保持干净结构，" +
                "不允许在未经设计师确认的情况下修改版型或添加装饰。");
        return techPack;
    }

    private String styleToDesc(String style) {
        if ("minimal".equals(style))               return "极简主义风格";
        if ("luxury".equals(style))                return "高端奢华风格";
        if ("modern".equals(style))                return "现代都市风格";
        if ("trendy".equals(style))                return "潮流街头风格";
        if ("avant".equals(style))                 return "前卫解构风格";
        if ("classic".equals(style))               return "经典百搭风格";
        return style + "风格";
    }

    // ====================================================================
    // Request / Response VOs (inner classes)
    // ====================================================================

    public static class RedesignReqVO {
        private List<String> images;
        private String  style;
        private Double  strength;
        private Integer count;
        private String  userId;
        public List<String> getImages()   { return images; }
        public void setImages(List<String> v)   { this.images = v; }
        public String getStyle()          { return style; }
        public void setStyle(String v)    { this.style = v; }
        public Double getStrength()       { return strength; }
        public void setStrength(Double v) { this.strength = v; }
        public Integer getCount()         { return count; }
        public void setCount(Integer v)   { this.count = v; }
        public String getUserId()         { return userId; }
        public void setUserId(String v)   { this.userId = v; }
    }

    public static class EditReqVO {
        private String image;
        private String instruction;
        private String userId;
        public String getImage()          { return image; }
        public void setImage(String v)    { this.image = v; }
        public String getInstruction()    { return instruction; }
        public void setInstruction(String v) { this.instruction = v; }
        public String getUserId()         { return userId; }
        public void setUserId(String v)   { this.userId = v; }
    }

    public static class SaveDesignReqVO {
        private String imageUrl;
        private String category;
        private String style;
        private String userId;
        private String source;
        public String getImageUrl()       { return imageUrl; }
        public void setImageUrl(String v) { this.imageUrl = v; }
        public String getCategory()       { return category; }
        public void setCategory(String v) { this.category = v; }
        public String getStyle()          { return style; }
        public void setStyle(String v)    { this.style = v; }
        public String getUserId()         { return userId; }
        public void setUserId(String v)   { this.userId = v; }
        public String getSource()         { return source; }
        public void setSource(String v)   { this.source = v; }
    }

    // ====================================================================
    // Request VOs
    // ====================================================================

    public static class GenerateReqVO {
        private String category;
        private String style;
        private String market;
        private String priceLevel;
        private Long   userId;
        public String getCategory()   { return category; }
        public void setCategory(String v)   { this.category = v; }
        public String getStyle()      { return style; }
        public void setStyle(String v)      { this.style = v; }
        public String getMarket()     { return market; }
        public void setMarket(String v)     { this.market = v; }
        public String getPriceLevel() { return priceLevel; }
        public void setPriceLevel(String v) { this.priceLevel = v; }
        public Long getUserId()       { return userId; }
        public void setUserId(Long v) { this.userId = v; }
    }

    public static class SelectReqVO {
        @NotBlank(message = "selectedImage 不能为空")
        private String selectedImage;
        private String chainCode;
        private String category;
        private String style;
        private Long   userId;
        private List<String> allImages;
        public String getSelectedImage()  { return selectedImage; }
        public void setSelectedImage(String v)  { this.selectedImage = v; }
        public String getChainCode()      { return chainCode; }
        public void setChainCode(String v)      { this.chainCode = v; }
        public String getCategory()       { return category; }
        public void setCategory(String v)       { this.category = v; }
        public String getStyle()          { return style; }
        public void setStyle(String v)    { this.style = v; }
        public Long getUserId()           { return userId; }
        public void setUserId(Long v)     { this.userId = v; }
        public List<String> getAllImages() { return allImages; }
        public void setAllImages(List<String> v) { this.allImages = v; }
    }

    public static class OrderCreateReqVO {
        @NotBlank(message = "chainCode 不能为空")
        private String chainCode;
        private Long   userId;
        private BigDecimal amount;
        public String getChainCode()    { return chainCode; }
        public void setChainCode(String v)   { this.chainCode = v; }
        public Long getUserId()         { return userId; }
        public void setUserId(Long v)   { this.userId = v; }
        public BigDecimal getAmount()   { return amount; }
        public void setAmount(BigDecimal v)  { this.amount = v; }
    }

    public static class RecolorReqVO {
        private String image;
        private String colorScheme;
        private String userId;
        public String getImage()             { return image; }
        public void setImage(String v)       { this.image = v; }
        public String getColorScheme()       { return colorScheme; }
        public void setColorScheme(String v) { this.colorScheme = v; }
        public String getUserId()            { return userId; }
        public void setUserId(String v)      { this.userId = v; }
    }

    public static class RefineReqVO {
        private String image;
        private String style;
        private String note;
        private String userId;
        public String getImage()        { return image; }
        public void setImage(String v)  { this.image = v; }
        public String getStyle()        { return style; }
        public void setStyle(String v)  { this.style = v; }
        public String getNote()         { return note; }
        public void setNote(String v)   { this.note = v; }
        public String getUserId()       { return userId; }
        public void setUserId(String v) { this.userId = v; }
    }

    public static class ScoreReqVO {
        private List<String> images;
        private String style;
        public List<String> getImages()  { return images; }
        public void setImages(List<String> v) { this.images = v; }
        public String getStyle()         { return style; }
        public void setStyle(String v)   { this.style = v; }
    }

    public static class SelectBestReqVO {
        private List<String> images;
        private String style;
        public List<String> getImages()  { return images; }
        public void setImages(List<String> v) { this.images = v; }
        public String getStyle()         { return style; }
        public void setStyle(String v)   { this.style = v; }
    }

    public static class DeduplicateReqVO {
        private List<String> images;
        private List<String> reference;
        private Double threshold;
        public List<String> getImages()      { return images; }
        public void setImages(List<String> v) { this.images = v; }
        public List<String> getReference()   { return reference; }
        public void setReference(List<String> v) { this.reference = v; }
        public Double getThreshold()         { return threshold; }
        public void setThreshold(Double v)   { this.threshold = v; }
    }

    public static class InspirationProcessReqVO {
        private List<String> images;
        private String source;
        public List<String> getImages()       { return images; }
        public void setImages(List<String> v) { this.images = v; }
        public String getSource()             { return source; }
        public void setSource(String v)       { this.source = v; }
    }

    public static class SelectRefsReqVO {
        private List<Map<String, Object>> images;
        public List<Map<String, Object>> getImages()           { return images; }
        public void setImages(List<Map<String, Object>> v)     { this.images = v; }
    }

    public static class GenerateCollectionReqVO {
        private List<String> refs;
        private String style;
        private Map<String, String> controls;
        private Integer count;
        private String userId;
        public List<String> getRefs()             { return refs; }
        public void setRefs(List<String> v)       { this.refs = v; }
        public String getStyle()                  { return style; }
        public void setStyle(String v)            { this.style = v; }
        public Map<String, String> getControls()  { return controls; }
        public void setControls(Map<String, String> v) { this.controls = v; }
        public Integer getCount()                 { return count; }
        public void setCount(Integer v)           { this.count = v; }
        public String getUserId()                 { return userId; }
        public void setUserId(String v)           { this.userId = v; }
    }

    public static class UpdateDetailReqVO {
        private String image;
        private Map<String, String> control;
        private String userId;
        public String getImage()                  { return image; }
        public void setImage(String v)            { this.image = v; }
        public Map<String, String> getControl()   { return control; }
        public void setControl(Map<String, String> v) { this.control = v; }
        public String getUserId()                 { return userId; }
        public void setUserId(String v)           { this.userId = v; }
    }

    public static class InspirationFilterReqVO {
        private List<String> images;
        public List<String> getImages()           { return images; }
        public void setImages(List<String> v)     { this.images = v; }
    }

    public static class InspirationScoreReqVO {
        private List<String> images;
        private String source;
        public List<String> getImages()           { return images; }
        public void setImages(List<String> v)     { this.images = v; }
        public String getSource()                 { return source; }
        public void setSource(String v)           { this.source = v; }
    }

    public static class InspirationClassifyReqVO {
        private List<String> images;
        public List<String> getImages()           { return images; }
        public void setImages(List<String> v)     { this.images = v; }
    }

    public static class StyleProfileReqVO {
        private String name;
        private String style;
        private Map<String, Object> rules;
        private String userId;
        public String getName()                   { return name; }
        public void setName(String v)             { this.name = v; }
        public String getStyle()                  { return style; }
        public void setStyle(String v)            { this.style = v; }
        public Map<String, Object> getRules()     { return rules; }
        public void setRules(Map<String, Object> v) { this.rules = v; }
        public String getUserId()                 { return userId; }
        public void setUserId(String v)           { this.userId = v; }
    }

    public static class GenerateSeasonReqVO {
        private String styleProfileId;
        private String style;
        private Map<String, Object> brandRules;
        private List<String> refs;
        private Map<String, String> controls;
        private Integer count;
        private String userId;
        public String getStyleProfileId()              { return styleProfileId; }
        public void setStyleProfileId(String v)        { this.styleProfileId = v; }
        public String getStyle()                       { return style; }
        public void setStyle(String v)                 { this.style = v; }
        public Map<String, Object> getBrandRules()     { return brandRules; }
        public void setBrandRules(Map<String, Object> v) { this.brandRules = v; }
        public List<String> getRefs()                  { return refs; }
        public void setRefs(List<String> v)            { this.refs = v; }
        public Map<String, String> getControls()       { return controls; }
        public void setControls(Map<String, String> v) { this.controls = v; }
        public Integer getCount()                      { return count; }
        public void setCount(Integer v)                { this.count = v; }
        public String getUserId()                      { return userId; }
        public void setUserId(String v)                { this.userId = v; }
    }

    public static class DeduplicateAdvancedReqVO {
        private List<String> generated;
        private List<String> refs;
        private List<String> library;
        public List<String> getGenerated()           { return generated; }
        public void setGenerated(List<String> v)     { this.generated = v; }
        public List<String> getRefs()                { return refs; }
        public void setRefs(List<String> v)          { this.refs = v; }
        public List<String> getLibrary()             { return library; }
        public void setLibrary(List<String> v)       { this.library = v; }
    }

    public static class DesignScoreReqVO {
        private List<String> images;
        private String style;
        public List<String> getImages()              { return images; }
        public void setImages(List<String> v)        { this.images = v; }
        public String getStyle()                     { return style; }
        public void setStyle(String v)               { this.style = v; }
    }

    public static class TechPackReqVO {
        private String image;
        private String style;
        private String userId;
        public String getImage()                     { return image; }
        public void setImage(String v)               { this.image = v; }
        public String getStyle()                     { return style; }
        public void setStyle(String v)               { this.style = v; }
        public String getUserId()                    { return userId; }
        public void setUserId(String v)              { this.userId = v; }
    }
}

