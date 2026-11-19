package cn.iocoder.yudao.module.im.service.sensitiveword;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.cache.CacheUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import cn.iocoder.yudao.module.im.dal.mysql.sensitiveword.ImSensitiveWordMapper;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.MESSAGE_SENSITIVE_WORD_BLOCKED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.SENSITIVE_WORD_DUPLICATED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.SENSITIVE_WORD_NOT_EXISTS;

/**
 * IM 敏感词 Service 实现类
 * <p>
 * 词库匹配交给 houbb sensitive-word 库（trie 树 + 全/半角 / 大小写 / 繁简体 / 数字风格规范化）
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImSensitiveWordServiceImpl implements ImSensitiveWordService {

    /**
     * 关闭多租户时的统一缓存键，仅用于缓存，不作为查询的租户编号
     */
    private static final Long SINGLE_TENANT_CACHE_KEY = 0L;

    @Value("${yudao.tenant.enable:true}")
    private boolean tenantEnable = true;

    @Resource
    private ImSensitiveWordMapper sensitiveWordMapper;

    /**
     * 缓存条目
     */
    @Data
    @AllArgsConstructor
    private static class SensitiveWordBsCache {

        /**
         * 敏感词检测器
         */
        private SensitiveWordBs bs;
        /**
         * 构建本实例时数据库里的 max(update_time)，作为下次刷新比对的基线
         */
        private LocalDateTime maxUpdateTime;

    }

    /**
     * 租户 → SensitiveWordBs 实例的本地缓存；关闭多租户时共用一个缓存
     * <p>
     * 每分钟触发一次异步 reload，先读 max(update_time)，没变就复用旧实例（避免 trie 重建），变了才重新读词库 + 重建。
     * 单实例 CRUD 后另外通过 {@link #invalidateSensitiveWordBsCaches()} 立即让本机失效，多实例靠定时刷新最长 1 分钟内收敛。
     */
    @SuppressWarnings({"Convert2Diamond", "NullableProblems"})
    private final LoadingCache<Long, SensitiveWordBsCache> sensitiveWordBsCaches = CacheUtils.buildAsyncReloadingCache(
            Duration.ofMinutes(1L), // 1 分钟过期
            new CacheLoader<Long, SensitiveWordBsCache>() {

                @Override
                public SensitiveWordBsCache load(Long tenantId) {
                    return loadFresh(tenantId);
                }

                @Override
                public ListenableFuture<SensitiveWordBsCache> reload(Long tenantId, SensitiveWordBsCache oldValue) {
                    // 异步刷新线程独立于业务线程，没有租户上下文；必须显式 TenantUtils.execute 设置，否则租户拦截器会按当前线程的空上下文拼 SQL
                    return Futures.immediateFuture(TenantUtils.execute(tenantEnable ? tenantId : null, () -> {
                        LocalDateTime currentMax = sensitiveWordMapper.selectMaxUpdateTime(tenantId);
                        // 没变 → 复用旧实例，避免无谓地重建 trie
                        if (ObjectUtil.equal(oldValue.getMaxUpdateTime(), currentMax)) {
                            return oldValue;
                        }
                        // 变了 → 重新读词库并重建 trie
                        return loadFresh(tenantId);
                    }));
                }

            });

    @Override
    public void validateText(String text) {
        if (StrUtil.isBlank(text)) {
            return;
        }
        Long cacheKey = tenantEnable ? TenantContextHolder.getRequiredTenantId() : SINGLE_TENANT_CACHE_KEY;
        SensitiveWordBs bs = sensitiveWordBsCaches.getUnchecked(cacheKey).getBs();
        if (bs.contains(text)) {
            throw exception(MESSAGE_SENSITIVE_WORD_BLOCKED);
        }
    }

    // ==================== 管理后台 ====================

    @Override
    public Long createSensitiveWord(ImSensitiveWordSaveReqVO createReqVO) {
        // 1. 校验唯一
        validateSensitiveWordUnique(null, createReqVO.getWord());

        // 2.1 入库
        ImSensitiveWordDO sensitiveWord = BeanUtils.toBean(createReqVO, ImSensitiveWordDO.class);
        sensitiveWordMapper.insert(sensitiveWord);
        // 2.2 强制失效本机缓存（多实例靠定时刷新收敛）
        invalidateSensitiveWordBsCaches();
        return sensitiveWord.getId();
    }

    @Override
    public void updateSensitiveWord(ImSensitiveWordSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateSensitiveWordExists(updateReqVO.getId());
        // 1.2 校验唯一（排除自身）
        validateSensitiveWordUnique(updateReqVO.getId(), updateReqVO.getWord());

        // 2.1 更新
        sensitiveWordMapper.updateById(BeanUtils.toBean(updateReqVO, ImSensitiveWordDO.class));
        // 2.2 强制失效本机缓存
        invalidateSensitiveWordBsCaches();
    }

    @Override
    public void deleteSensitiveWord(Long id) {
        // 1. 校验存在
        validateSensitiveWordExists(id);

        // 2.1 删除
        sensitiveWordMapper.deleteById(id);
        // 2.2 强制失效本机缓存
        invalidateSensitiveWordBsCaches();
    }

    @Override
    public void deleteSensitiveWordList(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 1. 删除
        sensitiveWordMapper.deleteByIds(ids);
        // 2. 强制失效本机缓存
        invalidateSensitiveWordBsCaches();
    }

    @Override
    public ImSensitiveWordDO getSensitiveWord(Long id) {
        return sensitiveWordMapper.selectById(id);
    }

    @Override
    public PageResult<ImSensitiveWordDO> getSensitiveWordPage(ImSensitiveWordPageReqVO pageReqVO) {
        return sensitiveWordMapper.selectPage(pageReqVO);
    }

    private SensitiveWordBsCache loadFresh(Long tenantId) {
        return TenantUtils.execute(tenantEnable ? tenantId : null, () -> {
            // 先取基线时间再读词库：反过来在两次查询之间出现的新插入会被漏感知
            LocalDateTime maxUpdateTime = sensitiveWordMapper.selectMaxUpdateTime(tenantId);
            Integer status = CommonStatusEnum.ENABLE.getStatus();
            List<ImSensitiveWordDO> words = sensitiveWordMapper.selectListByStatus(status);
            // 构建敏感词检测器
            // 词库只认后台配置：清空库内置白名单，避免“曹操”“幸运”等内置放行词屏蔽已启用的敏感词
            SensitiveWordBs bs = SensitiveWordBs.newInstance()
                    .wordDeny(() -> convertList(words, ImSensitiveWordDO::getWord))
                    .wordAllow(WordAllows.empty())
                    .init();
            return new SensitiveWordBsCache(bs, maxUpdateTime);
        });
    }

    /**
     * 强制让敏感词缓存失效，下次访问按最新 DB 重建
     * <p>
     * 关闭多租户时失效统一缓存；开启时仅失效当前租户，无租户上下文则失效所有租户。
     */
    private void invalidateSensitiveWordBsCaches() {
        if (!tenantEnable) {
            sensitiveWordBsCaches.invalidate(SINGLE_TENANT_CACHE_KEY);
            return;
        }
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            sensitiveWordBsCaches.invalidate(tenantId);
            return;
        }
        sensitiveWordBsCaches.invalidateAll();
    }

    private void validateSensitiveWordExists(Long id) {
        if (sensitiveWordMapper.selectById(id) == null) {
            throw exception(SENSITIVE_WORD_NOT_EXISTS);
        }
    }

    /**
     * 校验敏感词唯一（修改时排除自身）
     */
    private void validateSensitiveWordUnique(Long id, String word) {
        ImSensitiveWordDO sensitiveWord = sensitiveWordMapper.selectByWord(word);
        if (sensitiveWord == null) {
            return;
        }
        if (id == null || ObjectUtil.notEqual(sensitiveWord.getId(), id)) {
            throw exception(SENSITIVE_WORD_DUPLICATED, word);
        }
    }

}
