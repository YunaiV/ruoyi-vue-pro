package cn.iocoder.yudao.module.system.service.mail.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailAccountMapper;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import cn.iocoder.yudao.module.system.mq.producer.mail.MailProducer;
import cn.iocoder.yudao.module.system.service.mail.MailAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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
    private MailTemplateMapper mailTemplateMapper;

    @Resource
    private MailProducer mailProducer;

    /**
     * 邮箱账号缓存
     * key：邮箱账号编码 {@link MailAccountDO#getId()}
     *
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    private volatile Map<Long, MailAccountDO> mailAccountCache;

    /**
     * 缓存菜单的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    private volatile Date maxUpdateTime;

    @Override
    @PostConstruct
    public void initLocalCache() {
        List<MailAccountDO> mailAccountDOList = this.loadMailAccountIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(mailAccountDOList)) {
            return;
        }

        // 写入缓存
        mailAccountCache = CollectionUtils.convertMap(mailAccountDOList, MailAccountDO::getId);
        maxUpdateTime = CollectionUtils.getMaxValue(mailAccountDOList, MailAccountDO::getUpdateTime);
        log.info("[initLocalCache][初始化 MailAccount 数量为 {}]", mailAccountDOList.size());
    }

    private List<MailAccountDO> loadMailAccountIfUpdate(Date maxUpdateTime) {
        //第一步 判断是否需要更新
        if(null == maxUpdateTime){ // 如果更新时间为空，说明 DB 一定有新数据
            log.info("[loadMailAccountIfUpdate][首次加载全量账号信息]");
        }else{ // 判断数据库中是否有更新的账号信息
            if (mailAccountMapper.selectCountByUpdateTimeGt(maxUpdateTime) == 0) {
                return null;
            }
            log.info("[loadMailAccountIfUpdate][增量加载全量账号信息]");
        }
        return mailAccountMapper.selectList();
    }

    @Override
    public Long create(MailAccountCreateReqVO createReqVO) {
        // username 要校验唯一
        this.validateMailAccountOnlyByUserName(createReqVO.getUsername());
        MailAccountDO mailAccountDO = MailAccountConvert.INSTANCE.convert(createReqVO);
        mailAccountMapper.insert(mailAccountDO);

        // 更新
        mailProducer.sendMailAccountRefreshMessage();
        return mailAccountDO.getId();
    }

    @Override
    public void update(MailAccountUpdateReqVO updateReqVO) {
        // username 要校验唯一 TODO @wangjingyi：校验唯一的时候，需要排除掉自己 DONE
        this.validateMailAccountOnlyByUserNameAndId(updateReqVO.getUsername(),updateReqVO.getId());
        MailAccountDO mailAccountDO = MailAccountConvert.INSTANCE.convert(updateReqVO);
        // 校验是否存在
        validateMailAccountExists(mailAccountDO.getId());

        // 更新
        mailProducer.sendMailAccountRefreshMessage();
        mailAccountMapper.updateById(mailAccountDO);
    }

    @Override
    public void delete(Long id) {
        // 校验是否存在账号
        validateMailAccountExists(id);
        // 校验是否存在关联模版
        validateMailTemplateByAccountId(id);
        // 删除
        mailAccountMapper.deleteById(id);
        // 更新
        mailProducer.sendMailAccountRefreshMessage();
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

    private void validateMailAccountExists(Long id) {
        if (mailAccountMapper.selectById(id) == null) {
            throw exception(MAIL_ACCOUNT_NOT_EXISTS);
        }
    }

    private void validateMailAccountOnlyByUserName(String userName){
        mailAccountCache.forEach((key,value)->{
            if(value.getUsername().equals(userName)){
                throw exception(MAIL_ACCOUNT_EXISTS);
            }
        });
    }
    private void validateMailAccountOnlyByUserNameAndId(String userName,Long id){
        mailAccountCache.forEach((key , value)->{
            if (value.getUsername().equals(userName)){
                if (!key.equals(id)){
                    throw exception(MAIL_ACCOUNT_EXISTS);
                }
            }
        });
    }
    private void validateMailTemplateByAccountId(Long accountId){
        MailTemplateDO mailTemplateDO =  mailTemplateMapper.selectOneByAccountId(accountId);
        if (mailTemplateDO != null) {
            // TODO wangjingyi：MAIL_ACCOUNT_RELATE_TEMPLATE_EXISTS DONE
            throw exception(MAIL_ACCOUNT_RELATE_TEMPLATE_EXISTS);
        }
    }
}
