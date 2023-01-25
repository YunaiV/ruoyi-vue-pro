package cn.iocoder.yudao.module.mp.service.account;

import cn.hutool.core.util.ObjUtil;
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
import cn.iocoder.yudao.module.mp.mq.producer.MpAccountProducer;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_USERNAME_EXISTS;

/**
 * 公众号账号 Service 实现类
 *
 * @author fengdan
 */
@Slf4j
@Service
@Validated
public class MpAccountServiceImpl implements MpAccountService {

    /**
     * 账号缓存
     * key：账号编号 {@link MpAccountDO#getAppId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    private volatile Map<String, MpAccountDO> accountCache;

    @Resource
    private MpAccountMapper mpAccountMapper;

    @Resource
    @Lazy // 延迟加载，解决循环依赖的问题
    private MpServiceFactory mpServiceFactory;

    @Resource
    private MpAccountProducer mpAccountProducer;

    @Override
    @PostConstruct
    public void initLocalCache() {
        // 注意：忽略自动多租户，因为要全局初始化缓存
        TenantUtils.executeIgnore(() -> {
            // 第一步：查询数据
            List<MpAccountDO> accounts = mpAccountMapper.selectList();
            log.info("[initLocalCacheIfUpdate][缓存公众号账号，数量为:{}]", accounts.size());

            // 第二步：构建缓存。创建或更新支付 Client
            mpServiceFactory.init(accounts);
            accountCache = CollectionUtils.convertMap(accounts, MpAccountDO::getAppId);
        });
    }

    @Override
    public Long createAccount(MpAccountCreateReqVO createReqVO) {
        // 校验 appId 唯一
        validateAppIdUnique(null, createReqVO.getAppId());

        // 插入
        MpAccountDO account = MpAccountConvert.INSTANCE.convert(createReqVO);
        mpAccountMapper.insert(account);

        // 发送刷新消息
        mpAccountProducer.sendAccountRefreshMessage();
        return account.getId();
    }

    @Override
    public void updateAccount(MpAccountUpdateReqVO updateReqVO) {
        // 校验存在
        validateAccountExists(updateReqVO.getId());
        // 校验 appId 唯一
        validateAppIdUnique(updateReqVO.getId(), updateReqVO.getAppId());

        // 更新
        MpAccountDO updateObj = MpAccountConvert.INSTANCE.convert(updateReqVO);
        mpAccountMapper.updateById(updateObj);

        // 发送刷新消息
        mpAccountProducer.sendAccountRefreshMessage();
    }

    @Override
    public void deleteAccount(Long id) {
        // 校验存在
        validateAccountExists(id);
        // 删除
        mpAccountMapper.deleteById(id);

        // 发送刷新消息
        mpAccountProducer.sendAccountRefreshMessage();
    }

    private MpAccountDO validateAccountExists(Long id) {
        MpAccountDO account = mpAccountMapper.selectById(id);
        if (account == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    @VisibleForTesting
    public void validateAppIdUnique(Long id, String appId) {
        // 多个租户，appId 是不能重复，否则公众号回调会无法识别
        TenantUtils.executeIgnore(() -> {
            MpAccountDO account = mpAccountMapper.selectByAppId(appId);
            if (account == null) {
                return;
            }
            // 存在 account 记录的情况下
            if (id == null // 新增时，说明重复
                    || ObjUtil.notEqual(id, account.getId())) { // 更新时，如果 id 不一致，说明重复
                throw exception(USER_USERNAME_EXISTS);
            }
        });
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
    public List<MpAccountDO> getAccountList() {
        return mpAccountMapper.selectList();
    }

    @Override
    public void generateAccountQrCode(Long id) {
        // 校验存在
        MpAccountDO account = validateAccountExists(id);

        // 生成二维码
        WxMpService mpService = mpServiceFactory.getRequiredMpService(account.getAppId());
        String qrCodeUrl;
        try {
            WxMpQrCodeTicket qrCodeTicket = mpService.getQrcodeService().qrCodeCreateLastTicket("default");
            qrCodeUrl = mpService.getQrcodeService().qrCodePictureUrl(qrCodeTicket.getTicket());
        } catch (WxErrorException e) {
            throw exception(ErrorCodeConstants.ACCOUNT_GENERATE_QR_CODE_FAIL, e.getError().getErrorMsg());
        }

        // 保存二维码
        mpAccountMapper.updateById(new MpAccountDO().setId(id).setQrCodeUrl(qrCodeUrl));
    }

    @Override
    public void clearAccountQuota(Long id) {
        // 校验存在
        MpAccountDO account = validateAccountExists(id);

        // 生成二维码
        WxMpService mpService = mpServiceFactory.getRequiredMpService(account.getAppId());
        try {
            mpService.clearQuota(account.getAppId());
        } catch (WxErrorException e) {
            throw exception(ErrorCodeConstants.ACCOUNT_CLEAR_QUOTA_FAIL, e.getError().getErrorMsg());
        }
    }

}
