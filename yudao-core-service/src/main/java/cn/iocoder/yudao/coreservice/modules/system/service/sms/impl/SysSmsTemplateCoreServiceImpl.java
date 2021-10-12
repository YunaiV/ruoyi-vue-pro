package cn.iocoder.yudao.coreservice.modules.system.service.sms.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.mysql.sms.SysSmsTemplateCoreMapper;
import cn.iocoder.yudao.coreservice.modules.system.service.sms.SysSmsTemplateCoreService;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 短信模板 Core Service 接口
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class SysSmsTemplateCoreServiceImpl implements SysSmsTemplateCoreService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 短信模板缓存
     * key：短信模板编码 {@link SysSmsTemplateDO#getCode()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<String, SysSmsTemplateDO> smsTemplateCache;
    /**
     * 缓存短信模板的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile Date maxUpdateTime;

    @Resource
    private SysSmsTemplateCoreMapper smsTemplateCoreMapper;

    @Override
    @PostConstruct
    public void initLocalCache() {
        // 获取短信模板列表，如果有更新
        List<SysSmsTemplateDO> smsTemplateList = this.loadSmsTemplateIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(smsTemplateList)) {
            return;
        }

        // 写入缓存
        ImmutableMap.Builder<String, SysSmsTemplateDO> builder = ImmutableMap.builder();
        smsTemplateList.forEach(sysSmsTemplateDO -> builder.put(sysSmsTemplateDO.getCode(), sysSmsTemplateDO));
        smsTemplateCache = builder.build();
        assert smsTemplateList.size() > 0; // 断言，避免告警
        maxUpdateTime = smsTemplateList.stream().max(Comparator.comparing(BaseDO::getUpdateTime)).get().getUpdateTime();
        log.info("[initLocalCache][初始化 SmsTemplate 数量为 {}]", smsTemplateList.size());
    }

    /**
     * 如果短信模板发生变化，从数据库中获取最新的全量短信模板。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前短信模板的最大更新时间
     * @return 短信模板列表
     */
    private List<SysSmsTemplateDO> loadSmsTemplateIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadSmsTemplateIfUpdate][首次加载全量短信模板]");
        } else { // 判断数据库中是否有更新的短信模板
            if (smsTemplateCoreMapper.selectExistsByUpdateTimeAfter(maxUpdateTime) == null) {
                return null;
            }
            log.info("[loadSmsTemplateIfUpdate][增量加载全量短信模板]");
        }
        // 第二步，如果有更新，则从数据库加载所有短信模板
        return smsTemplateCoreMapper.selectList();
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCache();
    }

    @Override
    public SysSmsTemplateDO getSmsTemplateByCodeFromCache(String code) {
        return smsTemplateCache.get(code);
    }

    @Override
    public String formatSmsTemplateContent(String content, Map<String, Object> params) {
        return StrUtil.format(content, params);
    }

}
