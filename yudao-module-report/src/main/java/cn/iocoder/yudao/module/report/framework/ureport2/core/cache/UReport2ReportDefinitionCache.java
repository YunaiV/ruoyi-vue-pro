package cn.iocoder.yudao.module.report.framework.ureport2.core.cache;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.report.dal.redis.ureport2.UReport2FileVersionRedisDAO;
import cn.iocoder.yudao.module.report.framework.ureport2.core.util.UReport2FileNameUtils;
import com.bstek.ureport.cache.ReportDefinitionCache;
import com.bstek.ureport.definition.ReportDefinition;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持多实例、按租户隔离的 {@link ReportDefinitionCache} 实现类
 *
 * 1. 本地缓存解析后的模板定义，key 为「租户编号 + 模板名称」，避免不同租户的同名模板互相覆盖；
 *    模板名称先通过 {@link UReport2FileNameUtils#normalize(String)} 规范化，保证同一条数据库记录的不同写法，共用同一份缓存、版本号
 * 2. Redis 只保存每个模板的版本号，由 {@link UReport2FileVersionRedisDAO} 维护。
 *    模板保存、删除时，由 UReport2FileService 先写入数据库、再发布新的版本号；
 *    各实例读取时比对版本号，不一致则返回 null，由 UReport2 重新从数据库加载、解析
 *
 * 版本号的一致性：缓存中的每份模板定义，都至少与它记录的版本号一样新
 * 1. 读取：UReport2 的 ReportRender 在同一线程内，依次调用 {@link #getReportDefinition(String)}（未命中）、加载并解析模板、
 *    {@link #cacheReportDefinition(String, ReportDefinition)}。所以在未命中时，记录「加载数据库之前」读取到的版本号，写入缓存时使用它
 * 2. 保存：UReport2 设计器保存后，会缓存本次解析的模板定义，所以使用本次保存发布的版本号，见 {@link #onFileSaved(String, String)}
 * 同名模板的并发保存、删除不做额外处理。
 *
 * 待写入缓存的版本号记录在当前线程，由 UReport2SecurityFilter 在请求结束时调用 {@link #clearPendingVersions()} 清理
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class UReport2ReportDefinitionCache implements ReportDefinitionCache {

    /**
     * 当前线程待写入缓存的版本号，key 为 {@link #buildKey(Long, String)}，value 允许为 null（模板从未保存、删除过）
     *
     * 写入缓存时移除；如果模板不存在、解析失败，未被移除的记录在请求结束时，通过 {@link #clearPendingVersions()} 清理
     */
    private static final ThreadLocal<Map<String, String>> PENDING_VERSIONS = new ThreadLocal<>();

    /**
     * 本地缓存，key 为 {@link #buildKey(Long, String)}
     */
    private final Map<String, CachedReportDefinition> reportDefinitions = new ConcurrentHashMap<>();

    private final UReport2FileVersionRedisDAO fileVersionRedisDAO;

    @Override
    public ReportDefinition getReportDefinition(String file) {
        file = UReport2FileNameUtils.normalize(file);
        Long tenantId = getTenantId();
        String key = buildKey(tenantId, file);
        // 先读取版本号，再读取本地缓存：版本号一致，说明本地缓存是最新的
        String version = fileVersionRedisDAO.get(tenantId, file);
        CachedReportDefinition cached = reportDefinitions.get(key);
        if (cached != null && Objects.equals(cached.version, version)) {
            return cached.reportDefinition;
        }
        // 未命中：记录本次读取到的版本号，UReport2 加载、解析模板后，写入缓存时使用
        setPendingVersion(key, version);
        return null;
    }

    @Override
    public void cacheReportDefinition(String file, ReportDefinition reportDefinition) {
        String key = buildKey(getTenantId(), UReport2FileNameUtils.normalize(file));
        Map<String, String> pendingVersions = PENDING_VERSIONS.get();
        // 没有记录版本号，无法判断模板定义是否最新，所以不缓存，下次读取时重新解析
        if (pendingVersions == null || !pendingVersions.containsKey(key)) {
            return;
        }
        String version = pendingVersions.remove(key);
        if (pendingVersions.isEmpty()) {
            PENDING_VERSIONS.remove();
        }
        reportDefinitions.put(key, new CachedReportDefinition(version, reportDefinition));
    }

    /**
     * 模板保存、并发布版本号后调用：移除本实例的缓存
     *
     * UReport2 设计器保存模板后，会把本次解析的模板定义写入缓存，所以记录本次保存发布的版本号，供写入缓存时使用
     *
     * @param file 模板名称，包含存储前缀
     * @param version 本次保存发布的版本号
     */
    public void onFileSaved(String file, String version) {
        String key = buildKey(getTenantId(), UReport2FileNameUtils.normalize(file));
        reportDefinitions.remove(key);
        setPendingVersion(key, version);
    }

    /**
     * 模板删除后，由 UReport2FileServiceImpl 调用：移除本实例的缓存
     *
     * UReport2 删除模板时不会清理模板缓存，并且本地缓存没有过期时间，所以需要主动移除
     *
     * @param file 模板名称，包含存储前缀
     */
    public void onFileDeleted(String file) {
        reportDefinitions.remove(buildKey(getTenantId(), UReport2FileNameUtils.normalize(file)));
    }

    /**
     * 清理当前线程待写入缓存的版本号，在 UReport2 请求结束时调用
     */
    public static void clearPendingVersions() {
        PENDING_VERSIONS.remove();
    }

    private static void setPendingVersion(String key, String version) {
        Map<String, String> pendingVersions = PENDING_VERSIONS.get();
        if (pendingVersions == null) {
            pendingVersions = new HashMap<>();
            PENDING_VERSIONS.set(pendingVersions);
        }
        pendingVersions.put(key, version);
    }

    private static Long getTenantId() {
        return ObjUtil.defaultIfNull(TenantContextHolder.getTenantId(), TenantUtils.GLOBAL_TENANT_ID);
    }

    private static String buildKey(Long tenantId, String file) {
        return tenantId + ":" + file;
    }

    /**
     * 本地缓存的模板定义，以及它对应的版本号
     *
     * @author 芋道源码
     */
    private static class CachedReportDefinition {

        private final String version;
        private final ReportDefinition reportDefinition;

        private CachedReportDefinition(String version, ReportDefinition reportDefinition) {
            this.version = version;
            this.reportDefinition = reportDefinition;
        }

    }

}
