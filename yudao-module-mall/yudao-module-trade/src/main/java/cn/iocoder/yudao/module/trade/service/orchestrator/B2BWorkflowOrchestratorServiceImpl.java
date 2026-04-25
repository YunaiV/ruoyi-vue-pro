package cn.iocoder.yudao.module.trade.service.orchestrator;

import cn.iocoder.yudao.module.pay.service.blockchain.BlockchainAsyncService;
import cn.iocoder.yudao.module.pay.service.blockchain.MerkleTreeUtils;
import cn.iocoder.yudao.module.pay.service.blockchain.dto.OrderProofDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.service.recommend.ProductRecommendService;
import cn.iocoder.yudao.module.product.service.recommend.bo.ProductRecommendBO;
import cn.iocoder.yudao.module.product.service.share.ProductShareService;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessReqVO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessRespVO;
import cn.iocoder.yudao.module.trade.service.orchestrator.bo.WorkflowResultBO;
import cn.iocoder.yudao.module.trade.service.orchestrator.bo.WorkflowStepBO;
import cn.iocoder.yudao.module.trade.service.wholesale.IntelligentOrderProcessorService;
import cn.iocoder.yudao.module.trade.service.wholesale.bo.WholesaleOrderSummaryBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * B2B 工作流编排服务实现
 * <p>
 * 编排步骤（对应 Python B2BWorkflowOrchestrator.execute_wholesale_workflow()）：
 * <ol>
 *   <li>商品验证 — 通过官方 {@link ProductSkuApi} / {@link ProductSpuApi} 校验库存与状态</li>
 *   <li>智能定价 — {@link IntelligentOrderProcessorService} 计算批发价 + 信用评估</li>
 *   <li>区块链存证 — 通过官方 {@link BlockchainAsyncService}（JD Chain SDK 可选降级）</li>
 *   <li>安全分享链接 — {@link ProductShareService} 生成 order/contract/blockchain_proof 三类链接</li>
 *   <li>智能推荐 — {@link ProductRecommendService} 返回个性化后续推荐</li>
 * </ol>
 *
 * @author deepay
 */
@Slf4j
@Service
public class B2BWorkflowOrchestratorServiceImpl implements B2BWorkflowOrchestratorService {

    // ===== 官方 API / SDK 注入 =====

    /** 官方商品 SKU API（ProductSkuApiImpl，同进程直调） */
    @Resource
    private ProductSkuApi productSkuApi;

    /** 官方商品 SPU API（ProductSpuApiImpl，同进程直调） */
    @Resource
    private ProductSpuApi productSpuApi;

    /** 区块链异步存证服务（JD Chain SDK 可选；否则降级为本地哈希） */
    @Resource
    private BlockchainAsyncService blockchainAsyncService;

    // ===== 内部服务注入 =====

    @Resource
    private IntelligentOrderProcessorService intelligentOrderProcessorService;

    @Resource
    private ProductShareService productShareService;

    @Resource
    private ProductRecommendService productRecommendService;

    // ==================== 主入口 ====================

    @Override
    public WorkflowResultBO executeWholesaleWorkflow(Long userId,
                                                      AppWholesaleOrderProcessReqVO reqVO) {
        WorkflowResultBO result = new WorkflowResultBO();
        List<WorkflowStepBO> steps = new ArrayList<>();
        List<Map<String, Object>> timeline = new ArrayList<>();

        // ── Step 1：官方 ProductSkuApi 验证库存 ──────────────────────────────
        WorkflowStepBO step1 = executeStep(1, "商品验证", () -> {
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(AppWholesaleOrderProcessReqVO.Item::getSkuId)
                    .collect(Collectors.toList());
            List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(skuIds);
            // 校验每个 SKU 库存
            Map<Long, ProductSkuRespDTO> skuMap = skus.stream()
                    .collect(Collectors.toMap(ProductSkuRespDTO::getId, s -> s));
            List<String> errors = new ArrayList<>();
            for (AppWholesaleOrderProcessReqVO.Item item : reqVO.getItems()) {
                ProductSkuRespDTO sku = skuMap.get(item.getSkuId());
                if (sku == null) {
                    errors.add("SKU " + item.getSkuId() + " 不存在");
                } else if (sku.getStock() == null || sku.getStock() < item.getCount()) {
                    errors.add("SKU " + item.getSkuId() + " 库存不足（需要 "
                            + item.getCount() + "，剩余 " + (sku.getStock() == null ? 0 : sku.getStock()) + "）");
                }
            }
            if (!errors.isEmpty()) {
                throw new RuntimeException(String.join("; ", errors));
            }
            return Map.of("validated", true, "skuCount", skuIds.size());
        });
        steps.add(step1);
        addTimeline(timeline, step1);

        if ("failed".equals(step1.getStatus())) {
            result.setSteps(steps);
            result.setTimeline(timeline);
            result.setOverallStatus("failed");
            return result;
        }

        // ── Step 2：IntelligentOrderProcessorService 智能定价 + 信用评估 ─────
        AppWholesaleOrderProcessRespVO[] processorResult = new AppWholesaleOrderProcessRespVO[1];
        WorkflowStepBO step2 = executeStep(2, "智能定价+信用评估", () -> {
            processorResult[0] = intelligentOrderProcessorService.processWholesaleOrder(userId, reqVO);
            WholesaleOrderSummaryBO summary = processorResult[0].getSummary();
            return Map.of(
                    "originalPrice", summary != null ? summary.getTotalPrice() : 0,
                    "finalPrice", summary != null ? summary.getFinalPrice() : 0,
                    "discountAmount", summary != null ? summary.getDiscountAmount() : 0,
                    "creditLevel", processorResult[0].getCredit() != null
                            ? processorResult[0].getCredit().getLevel() : "D");
        });
        steps.add(step2);
        addTimeline(timeline, step2);

        // ── Step 3：官方 BlockchainAsyncService (JD Chain SDK) 存证 ──────────
        String[] taskId = {null};
        WorkflowStepBO step3 = executeStep(3, "区块链存证", () -> {
            WholesaleOrderSummaryBO summary = processorResult[0] != null ? processorResult[0].getSummary() : null;
            String contractHash = processorResult[0] != null && processorResult[0].getContract() != null
                    ? processorResult[0].getContract().getHash() : MerkleTreeUtils.sha256("no_contract");

            // 构建 Merkle Root（多维度数据指纹）
            Map<String, Object> attestationData = new LinkedHashMap<>();
            attestationData.put("userId", String.valueOf(userId));
            attestationData.put("totalAmount", summary != null ? String.valueOf(summary.getFinalPrice()) : "0");
            attestationData.put("contractHash", contractHash);
            attestationData.put("timestamp", LocalDateTime.now().toString());
            String merkleRoot = MerkleTreeUtils.computeMerkleRoot(attestationData);

            // 通过官方 BlockchainAsyncService 提交（JD Chain SDK 或降级为本地哈希）
            String id = "WF_" + userId + "_" + System.currentTimeMillis();
            OrderProofDTO dto = new OrderProofDTO();
            dto.setOrderId("WORKFLOW_" + id);
            dto.setAmount(summary != null ? BigDecimal.valueOf(summary.getFinalPrice(), 2) : BigDecimal.ZERO);
            dto.setCurrency("CNY");
            dto.setUserId(String.valueOf(userId));
            dto.setPaymentTime(LocalDateTime.now().toString());
            dto.setTransactionId(id);
            dto.setProductInfo("merkle_root:" + merkleRoot);
            blockchainAsyncService.asyncStoreOrderProof(dto);
            taskId[0] = id;

            return Map.of("taskId", id, "merkleRoot", merkleRoot);
        });
        steps.add(step3);
        addTimeline(timeline, step3);
        result.setBlockchainTaskId(taskId[0]);

        // ── Step 4：ProductShareService 生成三类安全分享链接 ───────────────────
        WorkflowStepBO step4 = executeStep(4, "安全分享链接生成", () -> {
            String contractNo = processorResult[0] != null && processorResult[0].getContract() != null
                    ? processorResult[0].getContract().getContractNo()
                    : "WORKFLOW_" + userId;

            // order 分享（只读 + 水印）
            ProductShareService.ShareResultDTO orderShare = productShareService.createShareableLink(
                    userId, "order", contractNo, "general",
                    buildSharePermissions(true, false, false, 5, true));

            // 合同分享（可查看 + 签字 + 密码保护）
            ProductShareService.ShareResultDTO contractShare = productShareService.createShareableLink(
                    userId, "contract", contractNo, "general",
                    buildSharePermissions(true, false, true, 3, true));

            // 区块链存证分享（公开可验证，30 天有效）
            ProductShareService.ShareResultDTO blockchainShare = productShareService.createShareableLink(
                    userId, "blockchain_proof", taskId[0] != null ? taskId[0] : contractNo,
                    "general",
                    Map.of("view", true, "verify", true, "maxViews", 100,
                            "watermark", false, "expiresIn", 30));

            Map<String, Object> links = new LinkedHashMap<>();
            links.put("orderVerification",  Map.of("shareUrl", orderShare.shareUrl,
                    "qrCodeParam", orderShare.qrCodeParam,
                    "expiresAt", orderShare.expiresAt));
            links.put("contractPreview",     Map.of("shareUrl", contractShare.shareUrl,
                    "qrCodeParam", contractShare.qrCodeParam,
                    "expiresAt", contractShare.expiresAt,
                    "password", contractShare.platformContent != null
                            ? contractShare.platformContent.getOrDefault("accessPassword", "") : ""));
            links.put("blockchainProof",     Map.of("shareUrl", blockchainShare.shareUrl,
                    "qrCodeParam", blockchainShare.qrCodeParam,
                    "expiresAt", blockchainShare.expiresAt));
            return links;
        });
        steps.add(step4);
        addTimeline(timeline, step4);
        if ("completed".equals(step4.getStatus())) {
            result.setVerificationLinks((Map<String, Object>) step4.getDetails());
        }

        // ── Step 5：ProductRecommendService 智能后续推荐 ─────────────────────
        WorkflowStepBO step5 = executeStep(5, "智能后续推荐", () -> {
            int budget = processorResult[0] != null && processorResult[0].getSummary() != null
                    ? (int) (processorResult[0].getSummary().getFinalPrice() * 0.3) : 0;
            List<ProductRecommendBO> recs = productRecommendService
                    .getPersonalizedRecommendations(userId, budget, null);
            return recs;
        });
        steps.add(step5);
        addTimeline(timeline, step5);
        if ("completed".equals(step5.getStatus())) {
            result.setRecommendations(step5.getDetails());
        }

        result.setSteps(steps);
        result.setTimeline(timeline);
        result.setOverallStatus(
                steps.stream().anyMatch(s -> "failed".equals(s.getStatus())) ? "partial" : "success");
        return result;
    }

    // ==================== 私有工具 ====================

    /** 带异常捕获的步骤执行器，保证后续步骤继续执行 */
    private WorkflowStepBO executeStep(int step, String name, java.util.concurrent.Callable<Object> action) {
        WorkflowStepBO s = new WorkflowStepBO();
        s.setStep(step);
        s.setName(name);
        s.setCompletedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        try {
            s.setDetails(action.call());
            s.setStatus("completed");
        } catch (Exception e) {
            log.warn("工作流步骤 [{}] 执行失败: {}", name, e.getMessage());
            s.setStatus("failed");
            s.setDetails(Map.of("error", e.getMessage() != null ? e.getMessage() : "未知错误"));
        }
        return s;
    }

    private void addTimeline(List<Map<String, Object>> timeline, WorkflowStepBO step) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("step", step.getStep());
        entry.put("name", step.getName());
        entry.put("status", step.getStatus());
        entry.put("completedAt", step.getCompletedAt());
        timeline.add(entry);
    }

    private Map<String, Object> buildSharePermissions(boolean view, boolean download,
                                                       boolean passwordProtected,
                                                       int maxViews, boolean watermark) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("view", view);
        p.put("download", download);
        p.put("passwordProtected", passwordProtected);
        p.put("maxViews", maxViews);
        p.put("watermark", watermark);
        p.put("expiresIn", 7);
        return p;
    }

}
