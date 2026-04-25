package cn.iocoder.yudao.module.product.service.review;

import cn.iocoder.yudao.module.pay.service.blockchain.BlockchainAsyncService;
import cn.iocoder.yudao.module.pay.service.blockchain.MerkleTreeUtils;
import cn.iocoder.yudao.module.pay.service.blockchain.dto.OrderProofDTO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import cn.iocoder.yudao.module.product.dal.mysql.comment.ProductCommentMapper;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 可信评价服务实现
 * <p>
 * 反欺诈阈值：fraudScore > 0.7 → 拒绝<br>
 * 信任度加权（7 因子）：
 * - user_reputation (0.30)：用户历史评价次数代理
 * - review_length   (0.10)：min(length/500, 1.0)
 * - specificity     (0.20)：服装关键词密度
 * - photo_evidence  (0.15)：有图片 1.0，无 0.3
 * - video_evidence  (0.10)：（暂不支持，固定 0）
 * - purchase_verified (0.10)：有 orderId → 1.0
 * - blockchain_attested (0.05)：固定 1.0
 *
 * @author deepay
 */
@Slf4j
@Service
public class TrustworthyReviewServiceImpl implements TrustworthyReviewService {

    /** 反欺诈阈值 */
    private static final double FRAUD_THRESHOLD = 0.7;
    /** 最小可见信任分 */
    private static final double VISIBLE_THRESHOLD = 0.5;

    /** 服装专业词汇（用于 specificity 评分） */
    private static final List<String> FASHION_KEYWORDS = Arrays.asList(
            "面料", "质地", "版型", "做工", "针脚", "颜色", "尺码", "腰围",
            "肩宽", "袖长", "裤长", "弹力", "透气", "亲肤", "缩水",
            "洗涤", "熨烫", "手感", "厚度", "光泽"
    );

    @Resource
    private ProductCommentMapper productCommentMapper;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private BlockchainAsyncService blockchainAsyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewSubmitResultDTO submitReview(Long userId, Long spuId, Long orderId, Long orderItemId,
                                              int scores, String content,
                                              List<String> picUrls, boolean anonymous) {
        ReviewSubmitResultDTO result = new ReviewSubmitResultDTO();

        // 1. 反欺诈检查
        double fraudScore = computeFraudScore(content, scores);
        if (fraudScore > FRAUD_THRESHOLD) {
            result.status = "rejected";
            result.reason = "疑似虚假评价（欺诈分：" + String.format("%.2f", fraudScore) + "）";
            return result;
        }

        // 2. 获取用户信息
        MemberUserRespDTO user = memberUserApi.getUser(userId);
        String nickname = anonymous ? ProductCommentDO.NICKNAME_ANONYMOUS
                : (user != null ? user.getNickname() : "用户" + userId);
        String avatar = (user != null) ? user.getAvatar() : null;

        // 3. 计算信任度
        Map<String, Double> factors = computeTrustFactors(userId, orderId, content, picUrls);
        double trustScore = factors.entrySet().stream()
                .mapToDouble(e -> e.getValue() * weightOf(e.getKey()))
                .sum();
        result.trustFactors = factors;
        result.trustScore = trustScore;

        // 4. 加权评分
        result.weightedRating = scores * (0.5 + trustScore * 0.5); // 分数 × 可信度加成

        // 5. 是否展示
        result.visible = trustScore >= VISIBLE_THRESHOLD;

        // 6. 持久化评论
        ProductCommentDO comment = new ProductCommentDO();
        comment.setUserId(userId);
        comment.setUserNickname(nickname);
        comment.setUserAvatar(avatar);
        comment.setAnonymous(anonymous);
        comment.setOrderId(orderId);
        comment.setOrderItemId(orderItemId);
        comment.setSpuId(spuId);
        comment.setScores(scores);
        comment.setContent(content);
        comment.setPicUrls(picUrls != null ? picUrls : Collections.emptyList());
        comment.setVisible(result.visible);
        comment.setReplyStatus(false);
        productCommentMapper.insert(comment);
        result.reviewId = comment.getId();

        // 7. 区块链存证（异步）
        result.blockchainTaskId = submitBlockchainAttestation(comment, trustScore);

        result.status = "success";
        return result;
    }

    // ==================== 私有方法 ====================

    private double computeFraudScore(String content, int scores) {
        if (content == null || content.trim().length() < 3) {
            return 0.9; // 极短评价 → 高欺诈分
        }
        double score = 0.0;
        // 内容过短
        if (content.length() < 10) score += 0.4;
        // 无实质内容（只有标点/空格）
        long wordCount = Arrays.stream(content.split("[\\s，。！？,.!?]+"))
                .filter(w -> w.length() > 0).count();
        if (wordCount < 2) score += 0.3;
        // 重复字符（如"好好好好好"）
        if (content.chars().distinct().count() < 3) score += 0.4;
        return Math.min(score, 1.0);
    }

    private Map<String, Double> computeTrustFactors(Long userId, Long orderId,
                                                     String content, List<String> picUrls) {
        Map<String, Double> f = new LinkedHashMap<>();

        // user_reputation：简化用历史订单评价次数代理（此处固定 0.7 基线）
        f.put("user_reputation", 0.7);

        // review_length
        f.put("review_length", content == null ? 0.0 : Math.min(content.length() / 500.0, 1.0));

        // specificity：服装专业词汇密度
        f.put("specificity", content == null ? 0.0 : computeSpecificity(content));

        // photo_evidence
        f.put("photo_evidence", (picUrls != null && !picUrls.isEmpty()) ? 1.0 : 0.3);

        // video_evidence（暂不支持）
        f.put("video_evidence", 0.0);

        // purchase_verified：有 orderId 表示已购买核实
        f.put("purchase_verified", (orderId != null && orderId > 0) ? 1.0 : 0.0);

        // blockchain_attested（提交后即视为 1.0）
        f.put("blockchain_attested", 1.0);

        return f;
    }

    private double computeSpecificity(String content) {
        long matches = FASHION_KEYWORDS.stream()
                .filter(content::contains)
                .count();
        return Math.min(matches / 5.0, 1.0);
    }

    private double weightOf(String factor) {
        switch (factor) {
            case "user_reputation":   return 0.30;
            case "review_length":     return 0.10;
            case "specificity":       return 0.20;
            case "photo_evidence":    return 0.15;
            case "video_evidence":    return 0.10;
            case "purchase_verified": return 0.10;
            case "blockchain_attested": return 0.05;
            default: return 0.0;
        }
    }

    private String submitBlockchainAttestation(ProductCommentDO comment, double trustScore) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("reviewId", String.valueOf(comment.getId()));
            data.put("userId", String.valueOf(comment.getUserId()));
            data.put("spuId", String.valueOf(comment.getSpuId()));
            data.put("scores", String.valueOf(comment.getScores()));
            data.put("contentHash", MerkleTreeUtils.sha256(
                    comment.getContent() != null ? comment.getContent() : ""));
            data.put("trustScore", String.valueOf(trustScore));
            data.put("timestamp", java.time.LocalDateTime.now().toString());

            String merkleRoot = MerkleTreeUtils.computeMerkleRoot(data);
            String taskId = "REVIEW_" + comment.getId() + "_" + System.currentTimeMillis();

            OrderProofDTO dto = new OrderProofDTO();
            dto.setOrderId("REVIEW_" + comment.getId());
            dto.setCurrency("CNY");
            dto.setAmount(BigDecimal.ZERO);
            dto.setUserId(String.valueOf(comment.getUserId()));
            dto.setProductInfo("SPU:" + comment.getSpuId());
            dto.setPaymentTime(java.time.LocalDateTime.now().toString());
            dto.setTransactionId(taskId);
            blockchainAsyncService.asyncStoreOrderProof(dto);
            return taskId;
        } catch (Exception ex) {
            log.warn("评价区块链存证提交失败 commentId={}: {}", comment.getId(), ex.getMessage());
            return null;
        }
    }

}
