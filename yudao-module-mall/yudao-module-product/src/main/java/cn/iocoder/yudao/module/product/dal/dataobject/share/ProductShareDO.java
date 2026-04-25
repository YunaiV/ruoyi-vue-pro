package cn.iocoder.yudao.module.product.dal.dataobject.share;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商品 / 订单分享记录 DO
 * <p>
 * 对应 Python SecureSharingService + IntelligentSharingSystem
 *
 * @author deepay
 */
@TableName(value = "product_share", autoResultMap = true)
@KeySequence("product_share_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductShareDO extends BaseDO {

    @TableId
    private Long id;

    /** 唯一分享令牌（UUID，32 位） */
    private String token;

    /**
     * 资源类型：product / order / contract / blockchain_proof
     */
    private String resourceType;

    /** 资源编号（如商品 SPU 编号 或 订单号） */
    private String resourceId;

    /** 创建分享的用户编号 */
    private Long userId;

    /**
     * 目标平台：wechat / weibo / general（用于内容生成）
     */
    private String platform;

    /**
     * 权限配置（JSON）
     * 字段：view, download, sign, verify, maxViews, watermark, passwordProtected
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> permissions;

    /** 已查看次数 */
    private Integer viewCount;

    /** 最大查看次数 */
    private Integer maxViews;

    /** 密码哈希（可选，SHA-256） */
    private String passwordHash;

    /** 是否加水印 */
    private Boolean watermark;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /**
     * 状态：1=活跃 0=禁用
     */
    private Integer status;

    /**
     * 访问追踪数据（JSON 数组，记录每次访问的 IP + 时间）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AccessRecord> trackingData;

    // ===== 内嵌 DTO =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessRecord {
        private String ip;
        private String userAgent;
        private String accessTime;
    }

}
