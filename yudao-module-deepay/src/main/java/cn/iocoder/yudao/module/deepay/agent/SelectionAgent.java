package cn.iocoder.yudao.module.deepay.agent;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserSelectionDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayUserSelectionMapper;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayUserProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SelectionAgent — 用户选图记忆（Phase 6 "越用越准"核心）。
 *
 * <p>触发时机：AIDecisionAgent 确定 selectedImage 之后，Orchestrator 立即调用。</p>
 *
 * <p>做两件事：
 * <ol>
 *   <li>向 {@code deepay_user_selection} 写入一条选择记录（userId / chainCode /
 *       selectedImage / category / style / score）</li>
 *   <li>更新 {@code deepay_user_profile.confidence + 0.05}，style_preference
 *       改为本次 style（让下次跳过问卷更快）</li>
 * </ol>
 * </p>
 *
 * <p>下次 TrendAgent 运行时，同品类爆款 + 用户历史偏好风格 的商品会排在更前面
 * （通过 styleWeights 传导，每次选择后由 MemoryAgent 加载时体现）。</p>
 *
 * <p>验收：
 * <ul>
 *   <li>✔ 用户选过之后，下次更准</li>
 *   <li>✔ 第二次基本不用问（confidence 累积）</li>
 * </ul>
 * </p>
 */
@Component
public class SelectionAgent implements Agent {

    private static final Logger log = LoggerFactory.getLogger(SelectionAgent.class);

    /** 每次选图后 confidence 增量 */
    private static final BigDecimal CONFIDENCE_DELTA = new BigDecimal("0.05");

    @Resource private DeepayUserSelectionMapper selectionMapper;
    @Resource private DeepayUserProfileMapper   profileMapper;

    @Override
    public Context run(Context ctx) {
        // 只有 selectedImage 存在时才有意义
        if (!StringUtils.hasText(ctx.selectedImage)) {
            log.debug("[SelectionAgent] 无 selectedImage，跳过选择记录");
            return ctx;
        }

        String userId = resolveUserId(ctx);
        if (userId == null) {
            log.debug("[SelectionAgent] 无 userId，跳过选择记录");
            return ctx;
        }

        // 1. 写入选择记录
        recordSelection(userId, ctx);

        // 2. 更新用户画像（confidence++ + style 更新）
        updateProfile(userId, ctx);

        log.info("[SelectionAgent] 选择已记录 userId={} category={} style={} image={}",
                userId, ctx.category, ctx.style, ctx.selectedImage);
        return ctx;
    }

    // ====================================================================
    // 内部实现
    // ====================================================================

    private void recordSelection(String userId, Context ctx) {
        DeepayUserSelectionDO record = new DeepayUserSelectionDO();
        record.setUserId(userId);
        record.setChainCode(ctx.chainCode);
        record.setSelectedImage(ctx.selectedImage);
        record.setCategory(ctx.category);
        record.setStyle(ctx.style);
        record.setMarket(ctx.market);

        // 从 imageScores 取当前选中图的评分
        if (ctx.imageScores != null && StringUtils.hasText(ctx.selectedImage)) {
            Integer score = ctx.imageScores.get(ctx.selectedImage);
            record.setScore(score);
        }

        record.setCreatedAt(LocalDateTime.now());
        try {
            selectionMapper.insert(record);
        } catch (Exception e) {
            log.warn("[SelectionAgent] 写入 deepay_user_selection 失败，不影响主流程", e);
        }
    }

    private void updateProfile(String userId, Context ctx) {
        try {
            // confidence += 0.05
            profileMapper.incrementConfidence(userId, CONFIDENCE_DELTA);

            // style_preference 更新为本次所选风格（若非空）
            if (StringUtils.hasText(ctx.style)) {
                profileMapper.update(null,
                        new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<
                                cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserProfileDO>()
                                .eq(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserProfileDO::getUserId,
                                        userId)
                                .set(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserProfileDO::getStylePreference,
                                        ctx.style)
                                .set(cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserProfileDO::getUpdatedAt,
                                        LocalDateTime.now()));
            }
        } catch (Exception e) {
            log.warn("[SelectionAgent] 更新 deepay_user_profile 失败，不影响主流程", e);
        }
    }

    private String resolveUserId(Context ctx) {
        if (ctx.userId != null)     return String.valueOf(ctx.userId);
        if (ctx.customerId != null) return String.valueOf(ctx.customerId);
        return null;
    }

}
