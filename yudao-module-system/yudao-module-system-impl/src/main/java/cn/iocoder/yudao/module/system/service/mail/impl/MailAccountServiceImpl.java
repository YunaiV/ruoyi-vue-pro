package cn.iocoder.yudao.module.system.service.mail.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailAccountMapper;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import cn.iocoder.yudao.module.system.service.mail.MailAccountService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_ACCOUNT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_ACCOUNT_NOT_EXISTS;


/**
 *  邮箱账号 Service 实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
public class MailAccountServiceImpl implements MailAccountService {

    @Resource
    private MailAccountMapper mailAccountMapper;

    @Resource
    private MailTemplateMapper mailTemplateMapper;

    @Override
    public Long create(MailAccountCreateReqVO createReqVO) {
        // username 要校验唯一
        this.validateMailAccountOnlyByUserName(createReqVO.getUsername());
        MailAccountDO mailAccountDO = MailAccountConvert.INSTANCE.convert(createReqVO);
        mailAccountMapper.insert(mailAccountDO);
        return mailAccountDO.getId();
    }

    @Override
    public void update(MailAccountUpdateReqVO updateReqVO) {
        // username 要校验唯一 // TODO @wangjingyi：更新的就是自己，username 这样写，会重复呀。
        this.validateMailAccountOnlyByUserName(updateReqVO.getUsername());
        MailAccountDO mailAccountDO = MailAccountConvert.INSTANCE.convert(updateReqVO);
        // 校验是否存在
        this.validateMailAccountExists(mailAccountDO.getId());
        mailAccountMapper.updateById(mailAccountDO);
    }

    @Override
    public void delete(Long id) {
        // TODO @wangjingyi：删除时，要判断是否有使用的模板
        // 校验是否存在
        this.validateMailAccountExists(id);
        mailAccountMapper.deleteById(id);
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
        MailAccountDO mailAccountDO = mailAccountMapper.selectByUserName(userName);
        if (mailAccountDO != null) {
            throw exception(MAIL_ACCOUNT_EXISTS);
        }
    }
}
