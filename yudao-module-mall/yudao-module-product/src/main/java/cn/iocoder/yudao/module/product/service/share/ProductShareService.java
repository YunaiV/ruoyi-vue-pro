package cn.iocoder.yudao.module.product.service.share;

import cn.iocoder.yudao.module.product.dal.dataobject.share.ProductShareDO;

import java.util.Map;

/**
 * 商品安全分享服务
 * <p>
 * 对应 Python SecureSharingService + IntelligentSharingSystem：
 * - createShareableLink：生成带权限控制的分享链接 + 二维码配置
 * - verifyAccess：校验 Token 合法性、过期、查看次数、密码
 * - generateShareContent：生成平台专属分享内容（微信卡片 / 微博推文）
 * - getSharePerformance：返回分享访问统计
 *
 * @author deepay
 */
public interface ProductShareService {

    /**
     * 创建分享链接
     *
     * @param userId       分享者用户编号
     * @param resourceType 资源类型：product / order / contract / blockchain_proof
     * @param resourceId   资源编号
     * @param platform     目标平台：wechat / weibo / general
     * @param permissions  权限配置 Map
     * @return 分享结果 DTO
     */
    ShareResultDTO createShareableLink(Long userId, String resourceType, String resourceId,
                                       String platform, Map<String, Object> permissions);

    /**
     * 校验访问权限
     *
     * @param token    分享令牌
     * @param ip       访客 IP
     * @param password 访客提供的密码（可为 null）
     * @return true 表示允许访问
     */
    boolean verifyAccess(String token, String ip, String password);

    /**
     * 生成平台专属分享内容
     *
     * @param token    分享令牌
     * @param platform 平台
     * @return 平台内容 Map（title / description / image / hashtags 等）
     */
    Map<String, Object> generateShareContent(String token, String platform);

    /**
     * 获取分享统计（访问次数、访问者分布等）
     *
     * @param token 分享令牌
     * @return 统计 Map
     */
    Map<String, Object> getSharePerformance(String token);

    /**
     * 停用分享链接
     */
    void disableShare(String token, Long userId);

    // ===== 内部 DTO =====

    class ShareResultDTO {
        public String token;
        public String shareUrl;
        /** 二维码图片 URL（由前端生成，此处返回参数） */
        public String qrCodeParam;
        public String expiresAt;
        public Map<String, Object> platformContent;
        public Map<String, Object> shareButtons;
        public String bestTimeToPost;
    }

}
