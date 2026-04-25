package cn.iocoder.yudao.module.trade.service.wholesale;

import cn.iocoder.yudao.module.member.api.level.MemberLevelApi;
import cn.iocoder.yudao.module.member.api.level.dto.MemberLevelRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.pay.service.blockchain.BlockchainAsyncService;
import cn.iocoder.yudao.module.pay.service.blockchain.MerkleTreeUtils;
import cn.iocoder.yudao.module.pay.service.blockchain.dto.OrderProofDTO;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.spu.ProductSpuApi;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessReqVO;
import cn.iocoder.yudao.module.trade.controller.app.wholesale.vo.AppWholesaleOrderProcessRespVO;
import cn.iocoder.yudao.module.trade.service.wholesale.bo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能批发订单处理服务实现
 *
 * @author deepay
 */
@Slf4j
@Service
public class IntelligentOrderProcessorServiceImpl implements IntelligentOrderProcessorService {

    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private BlockchainAsyncService blockchainAsyncService;

    @Override
    public AppWholesaleOrderProcessRespVO processWholesaleOrder(Long userId,
                                                                AppWholesaleOrderProcessReqVO reqVO) {
        AppWholesaleOrderProcessRespVO resp = new AppWholesaleOrderProcessRespVO();

        // 1. 智能库存检查
        List<Long> skuIds = reqVO.getItems().stream()
                .map(AppWholesaleOrderProcessReqVO.Item::getSkuId)
                .collect(Collectors.toList());
        WholesaleStockCheckBO stockCheck = checkStockIntelligently(reqVO.getItems(), skuIds);
        if (!stockCheck.isAvailable()) {
            resp.setStatus("need_alternatives");
            resp.setAlternatives(stockCheck.getAlternatives());
            return resp;
        }

        // 2. 获取会员等级
        int discountPercent = resolveDiscountPercent(userId);
        int totalCount = reqVO.getItems().stream()
                .mapToInt(AppWholesaleOrderProcessReqVO.Item::getCount).sum();

        // 3. 智能定价
        WholesaleOrderSummaryBO summary = calculateOrderWithDiscounts(
                reqVO.getItems(), discountPercent, totalCount);

        // 4. 信用评估
        WholesalerCreditBO credit = assessCredit(discountPercent, summary.getFinalPrice());

        // 5. 生成智能合同
        SmartContractBO contract = generateSmartContract(String.valueOf(userId), summary, credit);

        // 6. 区块链存证（异步）
        String blockchainTaskId = submitBlockchainAttestation(userId, summary, contract);

        // 7. 下一步建议
        List<WholesaleNextStepBO> nextSteps = suggestNextSteps(credit);

        resp.setStatus("success");
        resp.setSummary(summary);
        resp.setContract(contract);
        resp.setCredit(credit);
        resp.setNextSteps(nextSteps);
        resp.setBlockchainTaskId(blockchainTaskId);
        return resp;
    }

    // ==================== 私有方法 ====================

    private WholesaleStockCheckBO checkStockIntelligently(
            List<AppWholesaleOrderProcessReqVO.Item> items, List<Long> skuIds) {
        WholesaleStockCheckBO result = new WholesaleStockCheckBO();
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(skuIds);
        Map<Long, ProductSkuRespDTO> skuMap = skus.stream()
                .collect(Collectors.toMap(ProductSkuRespDTO::getId, s -> s));

        List<Long> unavailable = new ArrayList<>();
        for (AppWholesaleOrderProcessReqVO.Item item : items) {
            ProductSkuRespDTO sku = skuMap.get(item.getSkuId());
            if (sku == null || sku.getStock() == null || sku.getStock() < item.getCount()) {
                unavailable.add(item.getSkuId());
            }
        }

        result.setAvailable(unavailable.isEmpty());
        result.setUnavailableSkuIds(unavailable);

        if (!unavailable.isEmpty()) {
            result.setAlternatives(buildAlternatives(unavailable, skuMap));
        }
        return result;
    }

    private List<WholesaleStockCheckBO.AlternativeItem> buildAlternatives(
            List<Long> unavailableSkuIds, Map<Long, ProductSkuRespDTO> skuMap) {
        List<WholesaleStockCheckBO.AlternativeItem> alternatives = new ArrayList<>();
        for (Long skuId : unavailableSkuIds) {
            ProductSkuRespDTO original = skuMap.get(skuId);
            if (original == null) continue;
            // 查找同 SPU 下有库存的其他 SKU
            List<ProductSkuRespDTO> siblings = productSkuApi.getSkuListBySpuId(
                    Collections.singletonList(original.getSpuId()));
            Optional<ProductSkuRespDTO> alt = siblings.stream()
                    .filter(s -> !s.getId().equals(skuId) && s.getStock() != null && s.getStock() > 0)
                    .findFirst();
            if (alt.isPresent()) {
                ProductSkuRespDTO a = alt.get();
                WholesaleStockCheckBO.AlternativeItem ai = new WholesaleStockCheckBO.AlternativeItem();
                ai.setOriginalSkuId(skuId);
                ai.setAlternativeSpuId(a.getSpuId());
                ai.setAlternativeSkuId(a.getId());
                ai.setPicUrl(a.getPicUrl());
                ai.setPrice(a.getPrice());
                ai.setStock(a.getStock());
                ai.setReason("原规格缺货，推荐同款其他规格");
                alternatives.add(ai);
            }
        }
        return alternatives;
    }

    private WholesaleOrderSummaryBO calculateOrderWithDiscounts(
            List<AppWholesaleOrderProcessReqVO.Item> items,
            int discountPercent,
            int totalCount) {

        // 批量额外折扣（对应 Python calculate_wholesale_price 中的 avg_qty 逻辑）
        int bulkBonus = 0;
        if (totalCount > 5000) {
            bulkBonus = 10;
        } else if (totalCount > 1000) {
            bulkBonus = 5;
        }

        List<Long> skuIds = items.stream()
                .map(AppWholesaleOrderProcessReqVO.Item::getSkuId).collect(Collectors.toList());
        Map<Long, ProductSkuRespDTO> skuMap = productSkuApi.getSkuList(skuIds).stream()
                .collect(Collectors.toMap(ProductSkuRespDTO::getId, s -> s));

        // 获取 SPU 名称
        Set<Long> spuIds = skuMap.values().stream()
                .map(ProductSkuRespDTO::getSpuId).collect(Collectors.toSet());
        Map<Long, ProductSpuRespDTO> spuMap = productSpuApi.getSpuList(spuIds).stream()
                .collect(Collectors.toMap(ProductSpuRespDTO::getId, s -> s));

        List<WholesaleOrderSummaryBO.OrderItemSummary> summaryItems = new ArrayList<>();
        int totalOriginal = 0;
        int totalFinal = 0;

        for (AppWholesaleOrderProcessReqVO.Item item : items) {
            ProductSkuRespDTO sku = skuMap.get(item.getSkuId());
            if (sku == null) continue;
            ProductSpuRespDTO spu = spuMap.get(sku.getSpuId());

            int basePrice = sku.getPrice();
            double discountFactor = (100 - discountPercent) / 100.0;
            double bulkFactor = (100 - bulkBonus) / 100.0;
            int wholesalePrice = (int) Math.round(basePrice * discountFactor * bulkFactor);

            WholesaleOrderSummaryBO.OrderItemSummary s = new WholesaleOrderSummaryBO.OrderItemSummary();
            s.setSkuId(sku.getId());
            s.setSpuId(sku.getSpuId());
            s.setSpuName(spu != null ? spu.getName() : "");
            s.setPicUrl(sku.getPicUrl() != null ? sku.getPicUrl() : (spu != null ? spu.getPicUrl() : null));
            s.setCount(item.getCount());
            s.setOriginalPrice(basePrice);
            s.setWholesalePrice(wholesalePrice);
            s.setSubtotal(wholesalePrice * item.getCount());

            totalOriginal += basePrice * item.getCount();
            totalFinal += s.getSubtotal();
            summaryItems.add(s);
        }

        WholesaleOrderSummaryBO summary = new WholesaleOrderSummaryBO();
        summary.setTotalPrice(totalOriginal);
        summary.setFinalPrice(totalFinal);
        summary.setDiscountAmount(totalOriginal - totalFinal);
        summary.setWholesaleDiscountPercent(discountPercent);
        summary.setBulkBonusPercent(bulkBonus);
        summary.setItems(summaryItems);
        return summary;
    }

    private WholesalerCreditBO assessCredit(int discountPercent, int finalPrice) {
        WholesalerCreditBO credit = new WholesalerCreditBO();
        if (discountPercent >= 30) {
            credit.setLevel("A");
            credit.setLevelName("钻石级批发商");
            credit.setAllowCredit(true);
            credit.setAvailableCredit(finalPrice * 2);
            credit.setPaymentTerms("net_30");
            credit.setFastTrack(true);
        } else if (discountPercent >= 20) {
            credit.setLevel("B");
            credit.setLevelName("金牌批发商");
            credit.setAllowCredit(true);
            credit.setAvailableCredit((int) (finalPrice * 1.5));
            credit.setPaymentTerms("net_60");
            credit.setFastTrack(true);
        } else if (discountPercent >= 10) {
            credit.setLevel("C");
            credit.setLevelName("银牌批发商");
            credit.setAllowCredit(true);
            credit.setAvailableCredit(finalPrice);
            credit.setPaymentTerms("net_30");
            credit.setFastTrack(false);
        } else {
            credit.setLevel("D");
            credit.setLevelName("普通批发商");
            credit.setAllowCredit(false);
            credit.setAvailableCredit(0);
            credit.setPaymentTerms("prepay");
            credit.setFastTrack(false);
        }
        return credit;
    }

    private SmartContractBO generateSmartContract(String userId,
                                                   WholesaleOrderSummaryBO summary,
                                                   WholesalerCreditBO credit) {
        Map<String, Object> contractData = new LinkedHashMap<>();
        contractData.put("userId", userId);
        contractData.put("totalAmount", String.valueOf(summary.getFinalPrice()));
        contractData.put("paymentTerms", credit.getPaymentTerms());
        contractData.put("generatedAt", LocalDateTime.now().toString());
        String hash = MerkleTreeUtils.computeMerkleRoot(contractData);

        SmartContractBO contract = new SmartContractBO();
        contract.setContractNo("WH_" + userId + "_" + System.currentTimeMillis());
        contract.setHash(hash);
        contract.setWholesalerId(userId);
        contract.setTotalAmount(summary.getFinalPrice());
        contract.setPaymentTerms(credit.getPaymentTerms());
        contract.setGeneratedAt(LocalDateTime.now());
        contract.setExpiresAt(LocalDateTime.now().plusDays(30));
        contract.setStatus("PENDING");
        return contract;
    }

    private String submitBlockchainAttestation(Long userId,
                                                WholesaleOrderSummaryBO summary,
                                                SmartContractBO contract) {
        try {
            String taskId = "WH_" + userId + "_" + System.currentTimeMillis();
            OrderProofDTO dto = new OrderProofDTO();
            dto.setOrderId(contract.getContractNo());
            dto.setAmount(BigDecimal.valueOf(summary.getFinalPrice(), 2));
            dto.setCurrency("CNY");
            dto.setUserId(String.valueOf(userId));
            dto.setPaymentTime(LocalDateTime.now().toString());
            dto.setTransactionId(taskId);
            dto.setProductInfo("contract_hash:" + contract.getHash());
            blockchainAsyncService.asyncStoreOrderProof(dto);
            return taskId;
        } catch (Exception e) {
            log.warn("批发订单区块链存证提交失败 userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private List<WholesaleNextStepBO> suggestNextSteps(WholesalerCreditBO credit) {
        List<WholesaleNextStepBO> steps = new ArrayList<>();
        if (credit.isAllowCredit()) {
            WholesaleNextStepBO step1 = new WholesaleNextStepBO();
            step1.setAction("apply_credit");
            step1.setTitle("申请账期支付");
            step1.setDescription("您有 " + (credit.getAvailableCredit() / 100) + " 元可用信用额度（" + credit.getPaymentTerms() + "）");
            step1.setPriority(1);
            steps.add(step1);
        }
        if (credit.isFastTrack()) {
            WholesaleNextStepBO step2 = new WholesaleNextStepBO();
            step2.setAction("fast_track");
            step2.setTitle("快速通道审核");
            step2.setDescription("您的订单符合优先处理条件，预计 2 小时内完成审核");
            step2.setPriority(2);
            steps.add(step2);
        }
        WholesaleNextStepBO step3 = new WholesaleNextStepBO();
        step3.setAction("upload_documents");
        step3.setTitle("上传采购文件");
        step3.setDescription("上传采购单或授权书以加速审核");
        step3.setPriority(3);
        steps.add(step3);
        return steps;
    }

    private int resolveDiscountPercent(Long userId) {
        try {
            MemberUserRespDTO user = memberUserApi.getUser(userId);
            if (user == null || user.getLevelId() == null) return 0;
            MemberLevelRespDTO level = memberLevelApi.getMemberLevel(user.getLevelId());
            return level != null && level.getDiscountPercent() != null ? level.getDiscountPercent() : 0;
        } catch (Exception e) {
            log.warn("获取用户等级失败 userId={}: {}", userId, e.getMessage());
            return 0;
        }
    }

}
