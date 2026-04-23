package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayFeatureConfigDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayFeatureConfigMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Deepay 功能菜单配置接口。
 *
 * <pre>
 * ── 前端（用户端）─────────────────────────────────────────────────────
 * GET  /api/features               — 返回当前已启用的功能列表（按分组）
 *
 * ── 后台管理 ──────────────────────────────────────────────────────────
 * GET  /admin/deepay/feature/list  — 所有功能列表（含禁用）
 * POST /admin/deepay/feature/save  — 新增/编辑功能项
 * POST /admin/deepay/feature/delete— 删除功能项
 * POST /admin/deepay/feature/batch-enable  — 批量启用
 * POST /admin/deepay/feature/batch-disable — 批量禁用
 * POST /admin/deepay/feature/sort  — 更新单项排序
 * </pre>
 */
@Tag(name = "Deepay - 功能菜单配置")
@RestController
@Validated
public class DeepayFeatureConfigController {

    @Resource
    private DeepayFeatureConfigMapper featureMapper;

    // ====================================================================
    // 前端用户端接口：GET /api/features
    // ====================================================================

    /**
     * 返回当前已启用的功能列表，按 menuGroup 分组。
     *
     * <p>前端用此接口动态渲染首页菜单卡片，无需硬编码路由跳转。</p>
     *
     * <p>响应结构：
     * <pre>
     * {
     *   "ai_design":  [ { featureKey, featureName, icon, description, routePath, sortOrder }, … ],
     *   "material":   [ … ],
     *   "commerce":   [ … ],
     *   "ops":        [ … ]
     * }
     * </pre>
     * </p>
     */
    @GetMapping("/api/features")
    @Operation(summary = "获取已启用的功能列表（前端动态菜单）")
    public CommonResult<Map<String, List<Map<String, Object>>>> listEnabled() {
        List<DeepayFeatureConfigDO> items = featureMapper.selectEnabled();
        Map<String, List<Map<String, Object>>> grouped = items.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getMenuGroup() != null ? f.getMenuGroup() : "other",
                        LinkedHashMap::new,
                        Collectors.mapping(this::toPublicVO, Collectors.toList())
                ));
        return success(grouped);
    }

    // ====================================================================
    // 后台管理接口
    // ====================================================================

    /** 获取所有功能列表（含禁用，后台管理用）。 */
    @GetMapping("/admin/deepay/feature/list")
    @Operation(summary = "【后台】功能列表（含禁用）")
    public CommonResult<List<DeepayFeatureConfigDO>> adminList() {
        return success(featureMapper.selectAll());
    }

    /** 新增或编辑功能项。id 为空时新增，非空时更新。 */
    @PostMapping("/admin/deepay/feature/save")
    @Operation(summary = "【后台】新增/编辑功能项")
    public CommonResult<DeepayFeatureConfigDO> save(@RequestBody @Validated SaveReqVO req) {
        DeepayFeatureConfigDO po = req.getId() == null
                ? new DeepayFeatureConfigDO()
                : Optional.ofNullable(featureMapper.selectById(req.getId()))
                          .orElseGet(DeepayFeatureConfigDO::new);

        po.setFeatureKey(req.getFeatureKey());
        po.setFeatureName(req.getFeatureName());
        po.setIcon(req.getIcon());
        po.setDescription(req.getDescription());
        po.setRoutePath(req.getRoutePath());
        po.setMenuGroup(req.getMenuGroup());
        po.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 99);
        po.setEnabled(req.getEnabled() != null ? req.getEnabled() : 1);
        po.setVisibleTo(req.getVisibleTo() != null ? req.getVisibleTo() : "all");
        po.setUpdatedAt(LocalDateTime.now());

        if (po.getId() == null) {
            po.setCreatedAt(LocalDateTime.now());
            featureMapper.insert(po);
        } else {
            featureMapper.updateById(po);
        }
        return success(po);
    }

    /** 删除功能项。 */
    @PostMapping("/admin/deepay/feature/delete")
    @Operation(summary = "【后台】删除功能项")
    public CommonResult<Void> delete(@RequestParam @NotNull Long id) {
        featureMapper.deleteById(id);
        return success(null);
    }

    /** 批量启用。 */
    @PostMapping("/admin/deepay/feature/batch-enable")
    @Operation(summary = "【后台】批量启用")
    public CommonResult<Void> batchEnable(@RequestBody @Validated BatchReqVO req) {
        featureMapper.batchUpdateEnabled(req.getIds(), 1);
        return success(null);
    }

    /** 批量禁用。 */
    @PostMapping("/admin/deepay/feature/batch-disable")
    @Operation(summary = "【后台】批量禁用")
    public CommonResult<Void> batchDisable(@RequestBody @Validated BatchReqVO req) {
        featureMapper.batchUpdateEnabled(req.getIds(), 0);
        return success(null);
    }

    /** 更新单条记录的排序值（拖拽排序时逐条提交）。 */
    @PostMapping("/admin/deepay/feature/sort")
    @Operation(summary = "【后台】更新排序")
    public CommonResult<Void> updateSort(@RequestBody @Validated SortReqVO req) {
        featureMapper.updateSortOrder(req.getId(), req.getSortOrder());
        return success(null);
    }

    // ====================================================================
    // 内部工具
    // ====================================================================

    private Map<String, Object> toPublicVO(DeepayFeatureConfigDO f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("featureKey",   f.getFeatureKey());
        m.put("featureName",  f.getFeatureName());
        m.put("icon",         f.getIcon());
        m.put("description",  f.getDescription());
        m.put("routePath",    f.getRoutePath());
        m.put("sortOrder",    f.getSortOrder());
        m.put("visibleTo",    f.getVisibleTo());
        return m;
    }

    // ====================================================================
    // Request VOs
    // ====================================================================

    public static class SaveReqVO {
        private Long   id;
        @NotBlank(message = "featureKey 不能为空")
        private String featureKey;
        @NotBlank(message = "featureName 不能为空")
        private String featureName;
        private String icon;
        private String description;
        @NotBlank(message = "routePath 不能为空")
        private String routePath;
        @NotBlank(message = "menuGroup 不能为空")
        private String menuGroup;
        private Integer sortOrder;
        private Integer enabled;
        private String  visibleTo;

        public Long    getId()          { return id; }
        public void    setId(Long v)    { this.id = v; }
        public String  getFeatureKey()  { return featureKey; }
        public void    setFeatureKey(String v)  { this.featureKey = v; }
        public String  getFeatureName() { return featureName; }
        public void    setFeatureName(String v) { this.featureName = v; }
        public String  getIcon()        { return icon; }
        public void    setIcon(String v){ this.icon = v; }
        public String  getDescription() { return description; }
        public void    setDescription(String v) { this.description = v; }
        public String  getRoutePath()   { return routePath; }
        public void    setRoutePath(String v)   { this.routePath = v; }
        public String  getMenuGroup()   { return menuGroup; }
        public void    setMenuGroup(String v)   { this.menuGroup = v; }
        public Integer getSortOrder()   { return sortOrder; }
        public void    setSortOrder(Integer v)  { this.sortOrder = v; }
        public Integer getEnabled()     { return enabled; }
        public void    setEnabled(Integer v)    { this.enabled = v; }
        public String  getVisibleTo()   { return visibleTo; }
        public void    setVisibleTo(String v)   { this.visibleTo = v; }
    }

    public static class BatchReqVO {
        @NotNull(message = "ids 不能为空")
        private List<Long> ids;
        public List<Long> getIds()       { return ids; }
        public void       setIds(List<Long> v) { this.ids = v; }
    }

    public static class SortReqVO {
        @NotNull(message = "id 不能为空")
        private Long    id;
        @NotNull(message = "sortOrder 不能为空")
        private Integer sortOrder;
        public Long    getId()              { return id; }
        public void    setId(Long v)        { this.id = v; }
        public Integer getSortOrder()       { return sortOrder; }
        public void    setSortOrder(Integer v) { this.sortOrder = v; }
    }
}
