package cn.iocoder.yudao.module.system.service.notify;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.notify.NotifyTemplateConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyTemplateMapper;
import cn.iocoder.yudao.module.system.mq.producer.notify.NotifyProducer;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTIFY_TEMPLATE_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTIFY_TEMPLATE_NOT_EXISTS;

/**
 * 站内信模版 Service 实现类
 *
 * @author xrcoder
 */
@Service
@Validated
@Slf4j
public class NotifyTemplateServiceImpl implements NotifyTemplateService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 正则表达式，匹配 {} 中的变量
     */
    private static final Pattern PATTERN_PARAMS = Pattern.compile("\\{(.*?)}");

    @Resource
    private NotifyTemplateMapper notifyTemplateMapper;

    @Resource
    private NotifyProducer notifyProducer;

    /**
     * 站内信模板缓存
     * key：站内信模板编码 {@link NotifyTemplateDO#getCode()}
     * <p>
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<String, NotifyTemplateDO> notifyTemplateCache;

    /**
     * 缓存站内信模板的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile Date maxUpdateTime;

    /**
     * 初始化站内信模板的本地缓存
     */
    @Override
    public void initLocalCache() {
        // 获取站内信模板列表，如果有更新
        List<NotifyTemplateDO> notifyTemplateList = this.loadNotifyTemplateIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(notifyTemplateList)) {
            return;
        }

        // 写入缓存
        notifyTemplateCache = CollectionUtils.convertMap(notifyTemplateList, NotifyTemplateDO::getCode);
        maxUpdateTime = CollectionUtils.getMaxValue(notifyTemplateList, NotifyTemplateDO::getUpdateTime);
        log.info("[initLocalCache][初始化 NotifyTemplate 数量为 {}]", notifyTemplateList.size());
    }

    /**
     * 如果站内信模板发生变化，从数据库中获取最新的全量站内信模板。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前站内信模板的最大更新时间
     * @return 站内信模板列表
     */
    private List<NotifyTemplateDO> loadNotifyTemplateIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        if (maxUpdateTime == null) { // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadNotifyTemplateIfUpdate][首次加载全量站内信模板]");
        } else { // 判断数据库中是否有更新的站内信模板
            if (notifyTemplateMapper.selectCountByUpdateTimeGt(maxUpdateTime) == 0) {
                return null;
            }
            log.info("[loadNotifyTemplateIfUpdate][增量加载全量站内信模板]");
        }
        // 第二步，如果有更新，则从数据库加载所有站内信模板
        return notifyTemplateMapper.selectList();
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCache();
    }

    /**
     * 获得站内信模板，从缓存中
     *
     * @param code 模板编码
     * @return 站内信模板
     */
    @Override
    public NotifyTemplateDO getNotifyTemplateByCodeFromCache(String code) {
        return notifyTemplateCache.get(code);
    }

    /**
     * 格式化站内信内容
     *
     * @param content 站内信模板的内容
     * @param params  站内信内容的参数
     * @return 格式化后的内容
     */
    @Override
    public String formatNotifyTemplateContent(String content, Map<String, Object> params) {
        return StrUtil.format(content, params);
    }

    @VisibleForTesting
    public List<String> parseTemplateContentParams(String content) {
        return ReUtil.findAllGroup1(PATTERN_PARAMS, content);
    }

    @Override
    public Long createNotifyTemplate(NotifyTemplateCreateReqVO createReqVO) {
        // 校验站内信编码是否重复
        checkNotifyTemplateCodeDuplicate(null, createReqVO.getCode());

        // 插入
        NotifyTemplateDO notifyTemplate = NotifyTemplateConvert.INSTANCE.convert(createReqVO);
        notifyTemplate.setParams(parseTemplateContentParams(notifyTemplate.getContent()));
        notifyTemplateMapper.insert(notifyTemplate);
        // 发送刷新消息
        notifyProducer.sendNotifyTemplateRefreshMessage();
        // 返回
        return notifyTemplate.getId();
    }

    @Override
    public void updateNotifyTemplate(NotifyTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateNotifyTemplateExists(updateReqVO.getId());
        // 校验站内信编码是否重复
        checkNotifyTemplateCodeDuplicate(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        NotifyTemplateDO updateObj = NotifyTemplateConvert.INSTANCE.convert(updateReqVO);
        updateObj.setParams(parseTemplateContentParams(updateObj.getContent()));

        notifyTemplateMapper.updateById(updateObj);
        // 发送刷新消息
        notifyProducer.sendNotifyTemplateRefreshMessage();
    }

    @Override
    public void deleteNotifyTemplate(Long id) {
        // 校验存在
        this.validateNotifyTemplateExists(id);
        // 删除
        notifyTemplateMapper.deleteById(id);
        // 发送刷新消息
        notifyProducer.sendNotifyTemplateRefreshMessage();
    }

    private void validateNotifyTemplateExists(Long id) {
        if (notifyTemplateMapper.selectById(id) == null) {
            throw exception(NOTIFY_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public NotifyTemplateDO getNotifyTemplate(Long id) {
        return notifyTemplateMapper.selectById(id);
    }

    @Override
    public List<NotifyTemplateDO> getNotifyTemplateList(Collection<Long> ids) {
        return notifyTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<NotifyTemplateDO> getNotifyTemplatePage(NotifyTemplatePageReqVO pageReqVO) {
        return notifyTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<NotifyTemplateDO> getNotifyTemplateList(NotifyTemplateExportReqVO exportReqVO) {
        return notifyTemplateMapper.selectList(exportReqVO);
    }

    @VisibleForTesting
    public void checkNotifyTemplateCodeDuplicate(Long id, String code) {
        NotifyTemplateDO template = notifyTemplateMapper.selectByCode(code);
        if (template == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null) {
            throw exception(NOTIFY_TEMPLATE_CODE_DUPLICATE, code);
        }
        if (!template.getId().equals(id)) {
            throw exception(NOTIFY_TEMPLATE_CODE_DUPLICATE, code);
        }
    }
}
