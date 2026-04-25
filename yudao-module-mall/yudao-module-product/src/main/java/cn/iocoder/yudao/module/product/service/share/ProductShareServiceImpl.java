package cn.iocoder.yudao.module.product.service.share;

import cn.iocoder.yudao.module.pay.service.blockchain.MerkleTreeUtils;
import cn.iocoder.yudao.module.product.dal.dataobject.share.ProductShareDO;
import cn.iocoder.yudao.module.product.dal.mysql.share.ProductShareMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 商品安全分享服务实现
 * <p>
 * 对应 Python SecureSharingService + IntelligentSharingSystem。
 * 使用数据库（product_share）持久化令牌，无需 Redis 依赖。
 *
 * @author deepay
 */
@Slf4j
@Service
public class ProductShareServiceImpl implements ProductShareService {

    private static final int DEFAULT_EXPIRE_DAYS = 7;
    private static final int DEFAULT_MAX_VIEWS   = 10;

    @Resource
    private ProductShareMapper productShareMapper;

    @Value("${b2b.share.base-url:https://b2b.deepay.io/share}")
    private String shareBaseUrl;

    // ==================== 公开接口 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareResultDTO createShareableLink(Long userId, String resourceType, String resourceId,
                                               String platform, Map<String, Object> permissions) {
        String token = UUID.randomUUID().toString().replace("-", "");
        int maxViews = permissions != null
                ? ((Number) permissions.getOrDefault("maxViews", DEFAULT_MAX_VIEWS)).intValue()
                : DEFAULT_MAX_VIEWS;
        boolean watermark = permissions == null
                || Boolean.TRUE.equals(permissions.get("watermark"));

        String rawPassword = null;
        String passwordHash = null;
        if (permissions != null && Boolean.TRUE.equals(permissions.get("passwordProtected"))) {
            rawPassword = String.valueOf(100000 + new Random().nextInt(900000));
            passwordHash = MerkleTreeUtils.sha256(rawPassword);
        }

        int expireDays = permissions != null
                ? ((Number) permissions.getOrDefault("expiresIn", DEFAULT_EXPIRE_DAYS)).intValue()
                : DEFAULT_EXPIRE_DAYS;
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(expireDays);

        ProductShareDO share = ProductShareDO.builder()
                .token(token)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .userId(userId)
                .platform(platform != null ? platform : "general")
                .permissions(permissions)
                .viewCount(0)
                .maxViews(maxViews)
                .passwordHash(passwordHash)
                .watermark(watermark)
                .expiresAt(expiresAt)
                .status(1)
                .trackingData(new ArrayList<>())
                .build();
        productShareMapper.insert(share);

        ShareResultDTO result = new ShareResultDTO();
        result.token = token;
        result.shareUrl = shareBaseUrl + "/" + token;
        result.qrCodeParam = result.shareUrl; // 前端负责渲染二维码
        result.expiresAt = expiresAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        result.platformContent = buildPlatformContent(share, result.shareUrl);
        result.shareButtons = buildShareButtons(platform, result.shareUrl);
        result.bestTimeToPost = computeBestPostTime(userId, platform);
        if (rawPassword != null) {
            result.platformContent.put("accessPassword", rawPassword);
        }
        log.info("分享链接已创建 token={} resourceType={} resourceId={}", token, resourceType, resourceId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyAccess(String token, String ip, String password) {
        ProductShareDO share = productShareMapper.selectByToken(token);
        if (share == null) {
            return false;
        }
        // 过期检查
        if (LocalDateTime.now().isAfter(share.getExpiresAt())) {
            disableShare(token, share.getUserId());
            return false;
        }
        // 查看次数检查
        if (share.getViewCount() >= share.getMaxViews()) {
            return false;
        }
        // 密码检查
        if (share.getPasswordHash() != null) {
            if (password == null || !share.getPasswordHash().equals(MerkleTreeUtils.sha256(password))) {
                return false;
            }
        }
        // 记录访问
        recordAccess(share, ip);
        return true;
    }

    @Override
    public Map<String, Object> generateShareContent(String token, String platform) {
        ProductShareDO share = productShareMapper.selectByToken(token);
        if (share == null) {
            return Collections.emptyMap();
        }
        String shareUrl = shareBaseUrl + "/" + token;
        return buildPlatformContent(share, shareUrl);
    }

    @Override
    public Map<String, Object> getSharePerformance(String token) {
        ProductShareDO share = productShareMapper.selectByToken(token);
        if (share == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> perf = new LinkedHashMap<>();
        perf.put("token", token);
        perf.put("viewCount", share.getViewCount());
        perf.put("maxViews", share.getMaxViews());
        perf.put("utilizationRate", share.getMaxViews() > 0
                ? (double) share.getViewCount() / share.getMaxViews() : 0);
        perf.put("expiresAt", share.getExpiresAt());
        perf.put("accessLog", share.getTrackingData());
        // 简单基准对比
        perf.put("benchmarkAvgViews", 5);
        perf.put("aboveAverage", share.getViewCount() > 5);
        return perf;
    }

    @Override
    public void disableShare(String token, Long userId) {
        productShareMapper.update(null,
                new LambdaUpdateWrapper<ProductShareDO>()
                        .eq(ProductShareDO::getToken, token)
                        .eq(ProductShareDO::getUserId, userId)
                        .set(ProductShareDO::getStatus, 0));
    }

    // ==================== 私有方法 ====================

    private void recordAccess(ProductShareDO share, String ip) {
        List<ProductShareDO.AccessRecord> log_ = share.getTrackingData();
        if (log_ == null) {
            log_ = new ArrayList<>();
        }
        log_.add(new ProductShareDO.AccessRecord(
                ip,
                "API",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        productShareMapper.update(null,
                new LambdaUpdateWrapper<ProductShareDO>()
                        .eq(ProductShareDO::getId, share.getId())
                        .setSql("view_count = view_count + 1")
                        .set(ProductShareDO::getTrackingData, log_));
    }

    private Map<String, Object> buildPlatformContent(ProductShareDO share, String shareUrl) {
        Map<String, Object> content = new LinkedHashMap<>();
        String resourceDesc = "order".equals(share.getResourceType()) ? "采购订单"
                : "contract".equals(share.getResourceType()) ? "采购合同"
                : "blockchain_proof".equals(share.getResourceType()) ? "区块链存证"
                : "商品";
        String title = "【B2B平台】" + resourceDesc + " #" + share.getResourceId();

        if ("wechat".equals(share.getPlatform())) {
            content.put("previewType", "wechat_card");
            content.put("title", title);
            content.put("description", "点击查看真实可信的" + resourceDesc + "，区块链存证，不可篡改");
            content.put("url", shareUrl);
        } else if ("weibo".equals(share.getPlatform())) {
            content.put("previewType", "weibo_post");
            content.put("text", title + " " + shareUrl);
            content.put("hashtags", Arrays.asList("#B2B电商", "#智能供应链", "#区块链溯源"));
        } else {
            content.put("previewType", "general_link");
            content.put("title", title);
            content.put("url", shareUrl);
            content.put("description", "安全可信的B2B" + resourceDesc);
        }
        content.put("watermark", share.getWatermark());
        content.put("expiresAt", share.getExpiresAt());
        return content;
    }

    private Map<String, Object> buildShareButtons(String platform, String shareUrl) {
        Map<String, Object> buttons = new LinkedHashMap<>();
        buttons.put("copyLink", shareUrl);
        buttons.put("wechat", shareUrl);
        buttons.put("weibo", "https://service.weibo.com/share/share.php?url=" + shareUrl);
        buttons.put("email", "mailto:?subject=B2B分享&body=" + shareUrl);
        return buttons;
    }

    private String computeBestPostTime(Long userId, String platform) {
        // 简化：工作日上午 9-11 点是 B2B 场景最佳发布时间
        return "工作日 09:00-11:00";
    }

}
