package cn.iocoder.yudao.module.mp.service.account;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountExportReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.WxAccountUpdateReqVO;
import cn.iocoder.yudao.module.mp.convert.account.WxAccountConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.WxAccountDO;
import cn.iocoder.yudao.module.mp.dal.mysql.account.WxAccountMapper;
import cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.mp.mq.producer.WxMpConfigDataProducer;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.binarywang.spring.starter.wxjava.mp.properties.WxMpProperties;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.WxMpConfigStorage;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 公众号账户 Service 实现类
 *
 * @author fengdan
 */
@Slf4j
@Service
@Validated
public class WxAccountServiceImpl implements WxAccountService {

    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;
    @Resource
    private WxAccountMapper wxAccountMapper;
    @Resource
    private WxMpConfigDataProducer wxMpConfigDataProducer;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private WxMpService wxMpService;
    @Resource
    private WxMpProperties wxMpProperties;
    @Resource
    @Lazy // 注入自己，所以延迟加载
    private WxAccountService self;

    @Override
    public Long createWxAccount(WxAccountCreateReqVO createReqVO) {
        // 插入
        WxAccountDO wxAccount = WxAccountConvert.INSTANCE.convert(createReqVO);
        wxAccountMapper.insert(wxAccount);
        wxMpConfigDataProducer.sendDictDataRefreshMessage();
        // 返回
        return wxAccount.getId();
    }

    @Override
    public void updateWxAccount(WxAccountUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateWxAccountExists(updateReqVO.getId());
        // 更新
        WxAccountDO updateObj = WxAccountConvert.INSTANCE.convert(updateReqVO);
        wxAccountMapper.updateById(updateObj);
        wxMpConfigDataProducer.sendDictDataRefreshMessage();
    }

    @Override
    public void deleteWxAccount(Long id) {
        // 校验存在
        this.validateWxAccountExists(id);
        // 删除
        wxAccountMapper.deleteById(id);
        wxMpConfigDataProducer.sendDictDataRefreshMessage();
    }

    private void validateWxAccountExists(Long id) {
        if (wxAccountMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.WX_ACCOUNT_NOT_EXISTS);
        }
    }

    @Override
    public WxAccountDO getWxAccount(Long id) {
        return wxAccountMapper.selectById(id);
    }

    @Override
    public List<WxAccountDO> getWxAccountList(Collection<Long> ids) {
        return wxAccountMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<WxAccountDO> getWxAccountPage(WxAccountPageReqVO pageReqVO) {
        return wxAccountMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WxAccountDO> getWxAccountList(WxAccountExportReqVO exportReqVO) {
        return wxAccountMapper.selectList(exportReqVO);
    }

    @Override
    public WxAccountDO findBy(SFunction<WxAccountDO, ?> field, Object val) {
        return wxAccountMapper.selectOne(field, val);
    }

    @PostConstruct
    @TenantIgnore
    @Override
    public void initLoadWxMpConfigStorages() {
        List<WxAccountDO> wxAccountList = this.wxAccountMapper.selectList();
        if (CollectionUtils.isEmpty(wxAccountList)) {
            log.info("未读取到公众号配置，请在管理后台添加");
            return;
        }
        log.info("加载到{}条公众号配置", wxAccountList.size());
        wxAccountList.forEach(account -> addAccountToRuntime(account, new RedisTemplateWxRedisOps(stringRedisTemplate)));
        log.info("公众号配置加载完成");
    }

    /**
     * 添加账号到当前程序，如首次添加需初始化configStorageMap
     *
     * @param account 公众号
     */
    private synchronized void addAccountToRuntime(WxAccountDO account, RedisTemplateWxRedisOps redisOps) {
        String appId = account.getAppId();
        WxMpConfigStorage wxMpRedisConfig = account.toWxMpConfigStorage(redisOps, wxMpProperties);
        try {
            wxMpService.addConfigStorage(appId, wxMpRedisConfig);
        } catch (NullPointerException e) {
            log.info("需初始化configStorageMap...");
            Map<String, WxMpConfigStorage> configStorages = new HashMap<>(4);
            configStorages.put(appId, wxMpRedisConfig);
            wxMpService.setMultiConfigStorages(configStorages, appId);
        }
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        self.initLoadWxMpConfigStorages();
    }


}
