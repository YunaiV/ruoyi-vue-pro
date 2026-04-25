package cn.iocoder.yudao.module.product.service.review;

import java.util.List;
import java.util.Map;

/**
 * 可信评价服务
 * <p>
 * 对应 Python TrustworthyReviewSystem：
 * - 反欺诈检测（内容长度、关键词密度、重复度）
 * - 信任度评分（7 维加权）
 * - 区块链存证（异步）
 * - 区块链存证通过 BlockchainAsyncService 事件触发
 *
 * @author deepay
 */
public interface TrustworthyReviewService {

    /**
     * 提交可信评价
     *
     * @param userId     用户编号
     * @param spuId      商品 SPU 编号
     * @param orderId    交易订单编号（可为 null）
     * @param orderItemId 订单项编号（可为 null）
     * @param scores     评分（1-5）
     * @param content    评价内容
     * @param picUrls    图片 URL 列表
     * @param anonymous  是否匿名
     * @return 提交结果 DTO
     */
    ReviewSubmitResultDTO submitReview(Long userId, Long spuId, Long orderId, Long orderItemId,
                                       int scores, String content,
                                       List<String> picUrls, boolean anonymous);

    class ReviewSubmitResultDTO {
        /** 处理状态：success / rejected */
        public String status;
        /** 拒绝原因（rejected 时） */
        public String reason;
        /** 生成的评论 ID */
        public Long reviewId;
        /** 可信度分数（0-1） */
        public double trustScore;
        /** 加权评分（考虑可信度后的最终星级） */
        public double weightedRating;
        /** 是否展示给其他用户 */
        public boolean visible;
        /** 区块链存证任务 ID */
        public String blockchainTaskId;
        /** 各维度可信度因子 */
        public Map<String, Double> trustFactors;
    }

}
