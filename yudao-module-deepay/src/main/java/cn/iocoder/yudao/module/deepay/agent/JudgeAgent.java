package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayDesignVersionDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayDesignVersionMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayMetricsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JudgeAgent — 给每张设计图打分，结果落库 deepay_design_version（可追溯）。
 *
 * <p>打分规则（数据驱动）：
 * <pre>
 *   score = clamp(70 + (int)(历史同品类均销量 × 3), 70, 100)
 * </pre>
 * 无历史数据时 score = 70（中性基准分），有历史时分数随销量上升；均销量 ≥ 10 时达到满分 100。
 * 每次 REDESIGN 版本号自动 +1，保证历史版本完整保留。</p>
 */
@Component
public class JudgeAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(JudgeAgent.class);

    /** 历史数据缺失时使用的基准分 */
    private static final int BASE_SCORE = 70;
    /** 每单位均销量折算的分数 */
    private static final double SCORE_PER_UNIT = 3.0;

    @Resource
    private DeepayDesignVersionMapper deepayDesignVersionMapper;

    @Resource
    private DeepayMetricsMapper deepayMetricsMapper;

    @Override
    public Context run(Context ctx) {
        Map<String, Integer> scores = new HashMap<>();

        // 查询历史同品类均销量，作为打分加权因子
        int score = computeScore(ctx.keyword);

        if (ctx.designImages != null) {
            // 版本号 = 当前 chainCode 已有记录数 + 1（支持 REDESIGN 多轮追溯）
            int baseVersion = ctx.chainCode != null
                    ? deepayDesignVersionMapper.countByChainCode(ctx.chainCode) + 1
                    : 1;

            int idx = 0;
            for (String img : ctx.designImages) {
                scores.put(img, score);

                DeepayDesignVersionDO ver = new DeepayDesignVersionDO();
                ver.setChainCode(ctx.chainCode);
                ver.setImageUrl(img);
                ver.setVersion(baseVersion + idx);
                ver.setSelected(false);
                ver.setCreatedAt(LocalDateTime.now());
                deepayDesignVersionMapper.insert(ver);
                idx++;
            }
        }

        ctx.imageScores = scores;
        log.info("JudgeAgent: 打分完成，score={} 共 {} 张，chainCode={}", score, scores.size(), ctx.chainCode);
        return ctx;
    }

    /**
     * 根据同品类历史均销量计算动态分数。
     *
     * @param keyword 商品关键词（品类）
     * @return 70~100 之间的整数评分
     */
    private int computeScore(String keyword) {
        if (keyword != null) {
            try {
                Double avgSold = deepayMetricsMapper.selectAvgSoldCountByCategory(keyword);
                if (avgSold != null && avgSold > 0) {
                    int bonus = (int) Math.min(30, avgSold * SCORE_PER_UNIT);
                    int computed = BASE_SCORE + bonus;
                    log.info("JudgeAgent: 品类[{}] 历史均销量={}, 计算分={}", keyword, avgSold, computed);
                    return computed;
                }
            } catch (Exception e) {
                log.warn("JudgeAgent: 查询历史均销量失败，使用基准分 {}", BASE_SCORE, e);
            }
        }
        log.info("JudgeAgent: 品类[{}] 无历史数据，使用基准分 {}", keyword, BASE_SCORE);
        return BASE_SCORE;
    }

}


