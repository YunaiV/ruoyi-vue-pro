package cn.iocoder.yudao.module.oa.service.mail;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.mail.vo.account.OaMailAccountSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailAccountDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.mail.OaMailProviderDO;
import cn.iocoder.yudao.module.oa.dal.mysql.mail.OaMailAccountMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 企业邮箱账号 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaMailAccountServiceImpl implements OaMailAccountService {

    @Resource(name = "oaMailAccountMapper") // 区分 System 模块的同名 Mapper
    private OaMailAccountMapper mailAccountMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaMailProviderService mailProviderService;

    @Resource
    private OaMailMessageClient mailMessageClient;

    @Override
    public OaMailAccountDO lockMailAccount(Long id, Long userId) {
        // 1. 校验本人账号
        validateMailAccount(id, userId);

        // 2. 加锁后重新确认归属
        OaMailAccountDO account = mailAccountMapper.selectByIdForUpdate(id);
        if (account == null || ObjUtil.notEqual(userId.toString(), account.getCreator())) {
            throw exception(MAIL_ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMailAccount(OaMailAccountSaveReqVO reqVO, Long userId) {
        // 1.1 校验服务配置启用
        validateMailProviderEnabled(reqVO.getProviderId());
        // 1.2 创建时必须提供凭据
        if (StrUtil.isBlank(reqVO.getPassword())) {
            throw exception(MAIL_ACCOUNT_PASSWORD_REQUIRED);
        }

        // 2. 锁定本人账号，按请求调整默认值
        List<OaMailAccountDO> accounts = mailAccountMapper.selectListByCreatorForUpdate(userId.toString());
        if (Boolean.TRUE.equals(reqVO.getDefaultStatus())) {
            clearDefaultAccounts(accounts);
        }

        // 3. 新增账号
        OaMailAccountDO account = BeanUtils.toBean(reqVO, OaMailAccountDO.class).setId(null);
        mailAccountMapper.insert(account);
        return account.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMailAccount(OaMailAccountSaveReqVO reqVO, Long userId) {
        // 1.1 锁定本人账号并校验归属
        List<OaMailAccountDO> accounts = mailAccountMapper.selectListByCreatorForUpdate(userId.toString());
        OaMailAccountDO oldAccount = validateMailAccount(reqVO.getId(), userId);
        // 1.2 绑定身份固定，换邮箱需新增绑定，避免旧邮件归到新账号
        if (ObjUtil.notEqual(oldAccount.getProviderId(), reqVO.getProviderId())
                || ObjUtil.notEqual(oldAccount.getMail(), reqVO.getMail())
                || ObjUtil.notEqual(oldAccount.getUsername(), reqVO.getUsername())) {
            throw exception(MAIL_ACCOUNT_IDENTITY_IMMUTABLE);
        }
        // 1.3 校验服务配置
        validateMailProviderEnabled(reqVO.getProviderId());

        // 2. 更新默认账号及凭据
        if (Boolean.TRUE.equals(reqVO.getDefaultStatus())) {
            clearDefaultAccounts(accounts);
        }
        OaMailAccountDO account = BeanUtils.toBean(reqVO, OaMailAccountDO.class)
                .setPassword(StrUtil.blankToDefault(reqVO.getPassword(), null));
        mailAccountMapper.updateById(account);
    }

    @Override
    public void deleteMailAccount(Long id, Long userId) {
        // 1. 校验本人账号
        validateMailAccount(id, userId);

        // 2. 仅删除本地绑定
        mailAccountMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMailAccountDefault(Long id, Long userId) {
        // 1.1 锁定当前用户的邮箱账号列表
        List<OaMailAccountDO> accounts = mailAccountMapper.selectListByCreatorForUpdate(userId.toString());
        // 1.2 校验目标账号归属
        OaMailAccountDO account = validateMailAccount(id, userId);
        // 1.3 校验目标账号已启用
        if (CommonStatusEnum.isDisable(account.getStatus())) {
            throw exception(MAIL_ACCOUNT_DISABLED);
        }
        // 1.4 校验邮箱服务配置存在且启用
        validateMailProviderEnabled(account.getProviderId());

        // 2.1 清除原默认账号
        clearDefaultAccounts(accounts);
        // 2.2 设置目标账号为默认账号
        mailAccountMapper.updateById(new OaMailAccountDO().setId(id).setDefaultStatus(true));
    }

    @Override
    public OaMailAccountDO validateMailAccount(Long id, Long userId) {
        OaMailAccountDO account = mailAccountMapper.selectById(id);
        if (account == null || ObjUtil.notEqual(userId.toString(), account.getCreator())) {
            throw exception(MAIL_ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    @Override
    public List<OaMailAccountDO> getMailAccountListByUserIdAndStatus(Long userId, Integer status) {
        return mailAccountMapper.selectListByCreatorAndStatus(userId.toString(), status);
    }

    @Override
    public List<OaMailAccountDO> getMailAccountListByStatus(Integer status) {
        return mailAccountMapper.selectListByStatus(status);
    }

    @Override
    public Long getMailAccountCountByProviderId(Long providerId) {
        return mailAccountMapper.selectCountByProviderId(providerId);
    }

    @Override
    public Map<String, Boolean> testMailAccountConnection(Long id, Long userId) {
        // 1.1 校验账号归属和启用状态
        OaMailAccountDO account = validateMailAccount(id, userId);
        if (CommonStatusEnum.isDisable(account.getStatus())) {
            throw exception(MAIL_ACCOUNT_DISABLED);
        }
        // 1.2 校验服务配置
        OaMailProviderDO provider = validateMailProviderEnabled(account.getProviderId());

        // 2. 分别连接两个协议，不发送真实邮件
        String password = account.getPassword();
        Map<String, Boolean> result = new LinkedHashMap<>();
        result.put("imap", mailMessageClient.testConnection(provider.getImap(), account.getUsername(), password, true));
        result.put("smtp", mailMessageClient.testConnection(provider.getSmtp(), account.getUsername(), password, false));
        return result;
    }

    /**
     * 校验邮箱服务配置存在且启用
     *
     * @param providerId 邮箱服务配置编号
     * @return 邮箱服务配置
     */
    private OaMailProviderDO validateMailProviderEnabled(Long providerId) {
        OaMailProviderDO provider = mailProviderService.validateMailProviderExists(providerId);
        if (CommonStatusEnum.isDisable(provider.getStatus())) {
            throw exception(MAIL_PROVIDER_DISABLED);
        }
        return provider;
    }

    /**
     * 清除邮箱账号的默认标志
     *
     * @param accounts 已加锁的邮箱账号列表
     */
    private void clearDefaultAccounts(List<OaMailAccountDO> accounts) {
        for (OaMailAccountDO account : accounts) {
            if (Boolean.TRUE.equals(account.getDefaultStatus())) {
                mailAccountMapper.updateById(new OaMailAccountDO().setId(account.getId()).setDefaultStatus(false));
            }
        }
    }

}
