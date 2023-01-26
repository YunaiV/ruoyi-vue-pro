package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailAccountMapper;
import cn.iocoder.yudao.module.system.mq.producer.mail.MailProducer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 邮箱账号 Service 实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
@Slf4j
public class MailAccountServiceImpl implements MailAccountService {

    @Resource
    private MailAccountMapper mailAccountMapper;

    @Resource
    private MailTemplateService mailTemplateService;

    @Resource
    private MailProducer mailProducer;

    /**
     * 邮箱账号缓存
     * key：邮箱账号编码 {@link MailAccountDO#getId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    private volatile Map<Long, MailAccountDO> mailAccountCache;

    @Override
    @PostConstruct
    public void initLocalCache() {
        // 第一步：查询数据
        List<MailAccountDO> accounts = mailAccountMapper.selectList();
        log.info("[initLocalCache][缓存邮箱账号，数量:{}]", accounts.size());

        // 第二步：构建缓存
        mailAccountCache = convertMap(accounts, MailAccountDO::getId);
    }

    @Override
    public MailAccountDO getMailAccountFromCache(Long id) {
        return mailAccountCache.get(id);
    }

    @Override
    public Long createMailAccount(MailAccountCreateReqVO createReqVO) {
        // 插入
        MailAccountDO account = MailAccountConvert.INSTANCE.convert(createReqVO);
        mailAccountMapper.insert(account);

        // 发送刷新消息
        mailProducer.sendMailAccountRefreshMessage();
        return account.getId();
    }

    @Override
    public void updateMailAccount(MailAccountUpdateReqVO updateReqVO) {
        // 校验是否存在
        validateMailAccountExists(updateReqVO.getId());

        // 更新
        MailAccountDO updateObj = MailAccountConvert.INSTANCE.convert(updateReqVO);
        mailAccountMapper.updateById(updateObj);
        // 发送刷新消息
        mailProducer.sendMailAccountRefreshMessage();
    }

    @Override
    public void deleteMailAccount(Long id) {
        // 校验是否存在账号
        validateMailAccountExists(id);
        // 校验是否存在关联模版
        if (mailTemplateService.countByAccountId(id) > 0) {
            throw exception(MAIL_ACCOUNT_RELATE_TEMPLATE_EXISTS);
        }

        // 删除
        mailAccountMapper.deleteById(id);
        // 发送刷新消息
        mailProducer.sendMailAccountRefreshMessage();
    }

    private void validateMailAccountExists(Long id) {
        if (mailAccountMapper.selectById(id) == null) {
            throw exception(MAIL_ACCOUNT_NOT_EXISTS);
        }
    }

    @Override
    public MailAccountDO getMailAccount(Long id) {
        return mailAccountMapper.selectById(id);
    }

    @Override
    public PageResult<MailAccountDO> getMailAccountPage(MailAccountPageReqVO pageReqVO) {
        return mailAccountMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MailAccountDO> getMailAccountList() {
        return mailAccountMapper.selectList();
    }

}
