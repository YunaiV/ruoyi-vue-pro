package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Deepay 功能菜单配置表。
 *
 * <p>每一行代表一个功能入口，管理员可在后台对其进行启用/禁用、排序等操作。
 * 前端通过 {@code GET /api/features} 获取当前已启用的功能列表，动态渲染菜单。</p>
 *
 * <h3>菜单分组（menu_group）</h3>
 * <ul>
 *   <li>{@code ai_design}    — AI设计（出款/改款/整季/技术包）</li>
 *   <li>{@code material}     — 设计素材（灵感库/模板库）</li>
 *   <li>{@code commerce}     — 商业中心（店铺/排行/邀请/分润）</li>
 *   <li>{@code ops}          — 系统运营（商品/订单/支付/钱包/菜单配置）</li>
 * </ul>
 *
 * <h3>可见范围（visible_to）</h3>
 * <ul>
 *   <li>{@code all}   — 所有用户</li>
 *   <li>{@code vip}   — VIP 用户</li>
 *   <li>{@code beta}  — 内测用户</li>
 *   <li>{@code admin} — 仅管理员</li>
 * </ul>
 */
@TableName("deepay_feature_config")
@Data
public class DeepayFeatureConfigDO {

    /** 自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 功能唯一键（英文下划线命名，前端用此做路由映射）。
     * 示例：{@code ai_generate}、{@code inspiration}、{@code template}
     */
    private String featureKey;

    /** 功能显示名称（中文，前端展示用）。示例：AI出款生成 */
    private String featureName;

    /** 功能图标（emoji 或 icon class）。示例：🎯 */
    private String icon;

    /** 功能描述（前端卡片副标题）。示例：可控出款，系列生成 */
    private String description;

    /** 前端路由路径。示例：/ai/design */
    private String routePath;

    /**
     * 菜单分组。
     * 取值：{@code ai_design} / {@code material} / {@code commerce} / {@code ops}
     */
    private String menuGroup;

    /** 组内排序（数字越小越靠前）。 */
    private Integer sortOrder;

    /**
     * 是否启用（1=启用，0=禁用）。
     * 禁用后前端不会返回该功能，入口自动隐藏。
     */
    private Integer enabled;

    /**
     * 可见范围。
     * 取值：{@code all} / {@code vip} / {@code beta} / {@code admin}
     */
    private String visibleTo;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
