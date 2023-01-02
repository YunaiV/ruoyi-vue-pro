package cn.iocoder.yudao.module.mp.service.account;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.account.vo.MpAccountUpdateReqVO;
import cn.iocoder.yudao.module.mp.convert.account.MpAccountConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.mysql.account.MpAccountMapper;
import cn.iocoder.yudao.module.mp.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.mp.framework.mp.core.MpServiceFactory;
import cn.iocoder.yudao.module.mp.mq.producer.MpConfigProducer;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
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
public class MpAccountServiceImpl implements MpAccountService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 账号缓存
     * key：账号编号 {@link MpAccountDO#getAppId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    private volatile Map<String, MpAccountDO> accountCache;
    /**
     * 缓存菜单的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    @Getter
    private volatile LocalDateTime maxUpdateTime;

    @Resource
    private MpAccountMapper mpAccountMapper;

    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private MpServiceFactory mpServiceFactory;

    @Resource
    private MpConfigProducer mpConfigDataProducer;

    @Override
    @PostConstruct
    public void initLocalCache() {
        initLocalCacheIfUpdate(null);
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCacheIfUpdate(this.maxUpdateTime);
    }

    /**
     * 刷新本地缓存
     *
     * @param maxUpdateTime 最大更新时间
     *                      1. 如果 maxUpdateTime 为 null，则“强制”刷新缓存
     *                      2. 如果 maxUpdateTime 不为 null，判断自 maxUpdateTime 是否有数据发生变化，有的情况下才刷新缓存
     */
    private void initLocalCacheIfUpdate(LocalDateTime maxUpdateTime) {
        // 注意：忽略自动多租户，因为要全局初始化缓存
        TenantUtils.executeIgnore(() -> {
            // 第一步：基于 maxUpdateTime 判断缓存是否刷新。
            // 如果没有增量的数据变化，则不进行本地缓存的刷新
            if (maxUpdateTime != null
                    && mpAccountMapper.selectCountByUpdateTimeGt(maxUpdateTime) == 0) {
                log.info("[initLocalCacheIfUpdate][数据未发生变化({})，本地缓存不刷新]", maxUpdateTime);
                return;
            }
            List<MpAccountDO> accounts = mpAccountMapper.selectList();
            log.info("[initLocalCacheIfUpdate][缓存公众号账号，数量为:{}]", accounts.size());

            // 第二步：构建缓存。创建或更新支付 Client
            mpServiceFactory.init(accounts);
            accountCache = CollectionUtils.convertMap(accounts, MpAccountDO::getAppId);

            // 第三步：设置最新的 maxUpdateTime，用于下次的增量判断。
            this.maxUpdateTime = CollectionUtils.getMaxValue(accounts, MpAccountDO::getUpdateTime);
        });
    }

    @Override
    public Long createAccount(MpAccountCreateReqVO createReqVO) {
        // TODO 芋艿：校验唯一性
        // 插入
        MpAccountDO wxAccount = MpAccountConvert.INSTANCE.convert(createReqVO);
        mpAccountMapper.insert(wxAccount);

        // TODO 芋艿：刷新的方式
        mpConfigDataProducer.sendDictDataRefreshMessage();
        // 返回
        return wxAccount.getId();
    }

    @Override
    public void updateAccount(MpAccountUpdateReqVO updateReqVO) {
        // TODO 芋艿：校验唯一性
        // 校验存在
        validateWxAccountExists(updateReqVO.getId());
        // 更新
        MpAccountDO updateObj = MpAccountConvert.INSTANCE.convert(updateReqVO);
        mpAccountMapper.updateById(updateObj);

        // TODO 芋艿：刷新的方式
        mpConfigDataProducer.sendDictDataRefreshMessage();
    }

    @Override
    public void deleteAccount(Long id) {
        // 校验存在
        validateWxAccountExists(id);
        // 删除
        mpAccountMapper.deleteById(id);

        // TODO 芋艿：刷新的方式
        mpConfigDataProducer.sendDictDataRefreshMessage();
    }

    private void validateWxAccountExists(Long id) {
        if (mpAccountMapper.selectById(id) == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.WX_ACCOUNT_NOT_EXISTS);
        }
    }

    @Override
    public MpAccountDO getAccount(Long id) {
        return mpAccountMapper.selectById(id);
    }

    @Override
    public MpAccountDO getAccountFromCache(String appId) {
        return accountCache.get(appId);
    }

    @Override
    public PageResult<MpAccountDO> getAccountPage(MpAccountPageReqVO pageReqVO) {
        return mpAccountMapper.selectPage(pageReqVO);
    }

    @Override
    public MpAccountDO findBy(SFunction<MpAccountDO, ?> field, Object val) {
        return mpAccountMapper.selectOne(field, val);
    }

}
