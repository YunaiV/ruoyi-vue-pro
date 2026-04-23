package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.agent.Context;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayUserProfileDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayUserProfileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * UserProfileService — 用户画像读写（Phase 9 STEP 15）。
 *
 * <p>每次用户完成选款后调用 {@link #updateProfile}，
 * 将品类/风格偏好持久化到 {@code deepay_user_profile}，
 * 实现长期记忆。</p>
 */
@Component
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    @Resource
    private DeepayUserProfileMapper mapper;

    /**
     * 更新用户画像（upsert）。
     *
     * @param userId 用户 ID
     * @param ctx    当前 Context（读 category / stylePreference / market / priceLevel）
     */
    public void updateProfile(String userId, Context ctx) {
        if (!StringUtils.hasText(userId)) return;
        try {
            DeepayUserProfileDO profile = new DeepayUserProfileDO();
            profile.setUserId(userId);
            if (StringUtils.hasText(ctx.category))         profile.setCategory(ctx.category);
            if (StringUtils.hasText(ctx.stylePreference))  profile.setStylePreference(ctx.stylePreference);
            if (StringUtils.hasText(ctx.style))            profile.setStylePreference(ctx.style);
            if (StringUtils.hasText(ctx.market))           profile.setMarket(ctx.market);
            if (StringUtils.hasText(ctx.priceLevel))       profile.setPriceRange(ctx.priceLevel);
            profile.setUpdatedAt(LocalDateTime.now());
            mapper.upsert(profile);
            log.info("[UserProfileService] 画像已更新 userId={} category={} style={}", userId, ctx.category, ctx.stylePreference);
        } catch (Exception e) {
            log.warn("[UserProfileService] 更新画像失败 userId={}", userId, e);
        }
    }

    /**
     * 加载用户画像到 Context。
     *
     * @param userId 用户 ID
     * @param ctx    目标 Context（写入画像字段）
     */
    public void loadProfile(String userId, Context ctx) {
        if (!StringUtils.hasText(userId)) return;
        try {
            DeepayUserProfileDO profile = mapper.selectByUserId(userId);
            if (profile == null) return;
            if (StringUtils.hasText(profile.getCategory()) && !StringUtils.hasText(ctx.category)) {
                ctx.category = profile.getCategory();
            }
            if (StringUtils.hasText(profile.getStylePreference()) && !StringUtils.hasText(ctx.stylePreference)) {
                ctx.stylePreference = profile.getStylePreference();
                ctx.style = profile.getStylePreference();
            }
            if (StringUtils.hasText(profile.getMarket()) && !StringUtils.hasText(ctx.market)) {
                ctx.market = profile.getMarket();
            }
            if (StringUtils.hasText(profile.getPriceRange()) && !StringUtils.hasText(ctx.priceLevel)) {
                ctx.priceLevel = profile.getPriceRange();
            }
            log.info("[UserProfileService] 画像已加载 userId={} category={} style={}", userId, ctx.category, ctx.stylePreference);
        } catch (Exception e) {
            log.warn("[UserProfileService] 加载画像失败 userId={}", userId, e);
        }
    }
}
