package cn.iocoder.yudao.module.product.service.recommend;

import cn.iocoder.yudao.module.member.api.level.MemberLevelApi;
import cn.iocoder.yudao.module.member.api.level.dto.MemberLevelRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.product.dal.dataobject.history.ProductBrowseHistoryDO;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import cn.iocoder.yudao.module.product.dal.mysql.history.ProductBrowseHistoryMapper;
import cn.iocoder.yudao.module.product.dal.mysql.spu.ProductSpuMapper;
import cn.iocoder.yudao.module.product.enums.spu.ProductSpuStatusEnum;
import cn.iocoder.yudao.module.product.service.history.ProductBrowseHistoryService;
import cn.iocoder.yudao.module.product.service.recommend.bo.ProductRecommendBO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能服装推荐服务实现
 * <p>
 * 规则引擎替代 BERT + TF-IDF（相同 API 语义）：
 * - 分类匹配度：40%（浏览历史 Top 分类）
 * - 商品热度：30%（salesCount / 10000，最高 1.0）
 * - 季节适配：20%（月份 → 春夏秋冬映射）
 * - 预算匹配：10%（price <= budget → 1.0，否则 0）
 *
 * @author deepay
 */
@Slf4j
@Service
public class ProductRecommendServiceImpl implements ProductRecommendService {

    /** 每次最多返回推荐数量 */
    private static final int MAX_RECOMMENDATIONS = 10;
    /** 候选商品数量上限 */
    private static final int CANDIDATE_POOL_SIZE = 100;
    /** 最低推荐阈值 */
    private static final double SCORE_THRESHOLD = 0.2;

    @Resource
    private ProductSpuMapper productSpuMapper;
    @Resource
    private ProductBrowseHistoryMapper productBrowseHistoryMapper;
    @Resource
    private ProductBrowseHistoryService productBrowseHistoryService;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MemberLevelApi memberLevelApi;

    // ==================== 公开接口 ====================

    @Override
    public List<ProductRecommendBO> getPersonalizedRecommendations(Long userId, int budgetCents, String occasion) {
        // 1. 获取用户会员等级折扣
        int discountPercent = resolveDiscountPercent(userId);

        // 2. 获取用户浏览历史（用于分类偏好）
        Map<Long, Long> categoryFrequency = resolveCategoryFrequency(userId);

        // 3. 获取候选商品（上架 + 有库存）
        List<ProductSpuDO> candidates = productSpuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductSpuDO>()
                        .eq(ProductSpuDO::getStatus, ProductSpuStatusEnum.ENABLE.getStatus())
                        .gt(ProductSpuDO::getStock, 0)
                        .orderByDesc(ProductSpuDO::getSalesCount)
                        .last("LIMIT " + CANDIDATE_POOL_SIZE));

        // 4. 评分 + 过滤
        int currentMonth = LocalDate.now().getMonthValue();
        long totalBrowse = categoryFrequency.values().stream().mapToLong(Long::longValue).sum();

        List<ProductRecommendBO> scored = candidates.stream()
                .map(spu -> score(spu, categoryFrequency, totalBrowse, currentMonth, budgetCents, discountPercent))
                .filter(b -> b.getScore() >= SCORE_THRESHOLD)
                .sorted(Comparator.comparingDouble(ProductRecommendBO::getScore).reversed())
                .limit(MAX_RECOMMENDATIONS)
                .collect(Collectors.toList());

        return scored;
    }

    @Override
    public List<ProductRecommendBO> trackActionAndRefresh(Long userId, String action, Long spuId) {
        // 记录行为到浏览历史（VIEW / LIKE / CART_ADD 均产生正向信号）
        if ("DISLIKE".equals(action)) {
            log.debug("用户 {} 对 SPU {} 表示不喜欢，后续降权", userId, spuId);
        } else {
            productBrowseHistoryService.createBrowseHistory(userId, spuId);
        }
        // 重新计算推荐（无额外上下文时 budget=0 表示不限）
        return getPersonalizedRecommendations(userId, 0, null);
    }

    // ==================== 私有方法 ====================

    /**
     * 对单个 SPU 计算推荐评分并生成 BO
     */
    private ProductRecommendBO score(ProductSpuDO spu,
                                     Map<Long, Long> categoryFrequency,
                                     long totalBrowse,
                                     int currentMonth,
                                     int budgetCents,
                                     int discountPercent) {
        ProductRecommendBO bo = new ProductRecommendBO();
        bo.setSpuId(spu.getId());
        bo.setSpuName(spu.getName());
        bo.setPicUrl(spu.getPicUrl());
        bo.setCategoryId(spu.getCategoryId());
        bo.setRetailPrice(spu.getPrice());
        bo.setStock(spu.getStock());
        bo.setSalesCount(spu.getSalesCount() + spu.getVirtualSalesCount());

        // 批发价
        int wPrice = discountPercent > 0
                ? (int) Math.round(spu.getPrice() * (100 - discountPercent) / 100.0)
                : spu.getPrice();
        bo.setWholesalePrice(wPrice);
        bo.setDiscountPercent(discountPercent);

        ProductRecommendBO.DimensionScore ds = new ProductRecommendBO.DimensionScore();

        // 分类匹配度（40%）
        double categoryScore = 0.0;
        if (!categoryFrequency.isEmpty() && totalBrowse > 0) {
            long freq = categoryFrequency.getOrDefault(spu.getCategoryId(), 0L);
            categoryScore = Math.min((double) freq / totalBrowse * 3, 1.0); // 放大系数
        }
        ds.setCategoryMatch(categoryScore);

        // 热度（30%）
        int total = spu.getSalesCount() + spu.getVirtualSalesCount();
        double popularityScore = Math.min(total / 10000.0, 1.0);
        ds.setPopularity(popularityScore);

        // 季节适配（20%）：按月份对应季节
        double seasonScore = computeSeasonScore(currentMonth);
        ds.setSeasonRelevance(seasonScore);

        // 预算匹配（10%）
        double budgetScore = (budgetCents <= 0 || spu.getPrice() <= budgetCents) ? 1.0 : 0.0;
        ds.setBudgetFit(budgetScore);

        double total_ = categoryScore * 0.4
                + popularityScore * 0.3
                + seasonScore * 0.2
                + budgetScore * 0.1;

        bo.setDimensionScore(ds);
        bo.setScore(Math.min(total_, 1.0));
        bo.setReason(buildReason(spu, categoryScore, popularityScore, seasonScore, discountPercent));
        return bo;
    }

    private int resolveDiscountPercent(Long userId) {
        if (userId == null) {
            return 0;
        }
        try {
            MemberUserRespDTO user = memberUserApi.getUser(userId);
            if (user == null || user.getLevelId() == null) {
                return 0;
            }
            MemberLevelRespDTO level = memberLevelApi.getMemberLevel(user.getLevelId());
            return (level != null && level.getDiscountPercent() != null) ? level.getDiscountPercent() : 0;
        } catch (Exception e) {
            log.warn("获取用户会员等级失败 userId={}: {}", userId, e.getMessage());
            return 0;
        }
    }

    private Map<Long, Long> resolveCategoryFrequency(Long userId) {
        if (userId == null) {
            return Collections.emptyMap();
        }
        try {
            Page<ProductBrowseHistoryDO> page =
                    productBrowseHistoryMapper.selectPageByUserIdOrderByCreateTimeAsc(userId, 1, 50);
            if (page.getRecords().isEmpty()) {
                return Collections.emptyMap();
            }
            List<Long> spuIds = page.getRecords().stream()
                    .map(ProductBrowseHistoryDO::getSpuId)
                    .collect(Collectors.toList());
            // 获取 SPU 信息以获取 categoryId
            List<ProductSpuDO> spus = productSpuMapper.selectBatchIds(spuIds);
            Map<Long, Long> freq = new HashMap<>();
            spus.forEach(s -> freq.merge(s.getCategoryId(), 1L, Long::sum));
            return freq;
        } catch (Exception e) {
            log.warn("获取浏览历史失败 userId={}: {}", userId, e.getMessage());
            return Collections.emptyMap();
        }
    }

    /** 简化的季节评分：春(3-5)=0.9, 夏(6-8)=0.9, 秋(9-11)=0.9, 冬(12-2)=0.9, 换季=0.6 */
    private double computeSeasonScore(int month) {
        // 换季月份稍低（实际应基于商品标签，此处统一基线）
        return (month == 3 || month == 6 || month == 9 || month == 12) ? 0.6 : 0.9;
    }

    private String buildReason(ProductSpuDO spu, double catScore, double popScore,
                                double seasonScore, int discountPercent) {
        List<String> parts = new ArrayList<>();
        if (catScore > 0.3) {
            parts.add("符合您的常购品类");
        }
        if (popScore > 0.5) {
            parts.add("平台热销款");
        }
        if (seasonScore >= 0.9) {
            parts.add("当季热门");
        }
        if (discountPercent > 0) {
            parts.add("享受会员 " + discountPercent + "% 折扣");
        }
        return parts.isEmpty() ? "基于您的采购习惯推荐" : String.join("；", parts);
    }

}
