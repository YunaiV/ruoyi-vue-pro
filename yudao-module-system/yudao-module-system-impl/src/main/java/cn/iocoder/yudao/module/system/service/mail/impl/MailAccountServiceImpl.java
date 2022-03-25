package cn.iocoder.yudao.module.system.service.mail.impl;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountUpdateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.send.MailSendVO;
import cn.iocoder.yudao.module.system.convert.mail.MailAccountConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailAccountMapper;
import cn.iocoder.yudao.module.system.dal.mysql.mail.MailTemplateMapper;
import cn.iocoder.yudao.module.system.service.mail.MailAccountService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_ACCOUNT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_ACCOUNT_NOT_EXISTS;


/**
 * <p>
 *  邮箱账号 Service 实现类
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
public class MailAccountServiceImpl implements MailAccountService {

    @Resource
    private MailAccountMapper mailAccountMapper;

    @Resource
    private MailTemplateMapper mailTemplateMapper;

    @Override
    public Long create(MailAccountCreateReqVO createReqVO) {
        // username 要校验唯一
        Map<String , String> map = new HashMap<>();
        map.put("username" , createReqVO.getUsername());
        this.validateMailAccountOnly(map);
        MailAccountDO mailAccountDO = MailAccountConvert.INSTANCE.convert(createReqVO);
        mailAccountMapper.insert(mailAccountDO);
        return mailAccountDO.getId();
    }

    @Override
    public void update(MailAccountUpdateReqVO updateReqVO) {
        // username 要校验唯一
        Map<String , String> map = new HashMap<>();
        map.put("username" , updateReqVO.getUsername());
        this.validateMailAccountOnly(map);
        MailAccountDO mailAccountDO = MailAccountConvert.INSTANCE.convert(updateReqVO);
        // 校验是否存在
        this.validateMailAccountExists(mailAccountDO.getId());
        mailAccountMapper.updateById(mailAccountDO);
    }


    @Override
    public void delete(Long id) {
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

    @Override
    public void sendMail(MailSendVO mailSendVO) {
        // FIXME 查询模版信息 查询模版多条时 使用规则是什么
        List<MailTemplateDO> mailTemplateDOList =  mailTemplateMapper.selectList(
                "username",mailSendVO.getFrom()
        );
        //查询账号信息
        MailAccountDO mailAccountDO = mailAccountMapper.selectOne(
                "from",mailSendVO.getFrom()
        );
        // FIXME 模版和邮件内容合成方式未知
        String content = mailSendVO.getContent();
        String templateContent = "";
        // 后续功能 TODO ：附件查询
        //List<String> fileIds = mailSendVO.getFileIds();

        //装载账号信息
        MailAccount account = new MailAccount();
        account.setHost(mailAccountDO.getHost());
        account.setPort(mailAccountDO.getPort());
        account.setAuth(true);
        account.setFrom(mailAccountDO.getFrom());
        account.setUser(mailAccountDO.getUsername());
        account.setPass(mailAccountDO.getPassword());
        account.setSslEnable(mailAccountDO.getSslEnable());
        //发送
        MailUtil.send(account , mailSendVO.getTos() , mailSendVO.getTitle() , mailSendVO.getContent() , false);
    }

    private void validateMailAccountExists(Long id) {
        if (mailAccountMapper.selectById(id) == null) {
            throw exception(MAIL_ACCOUNT_NOT_EXISTS);
        }
    }

    private void validateMailAccountOnly(Map params){
        QueryWrapper queryWrapper = new QueryWrapper<MailAccountDO>();
        params.forEach((k , v)->{
            queryWrapper.like(k , v);
        });
        if (mailAccountMapper.selectOne(queryWrapper) != null) {
            throw exception(MAIL_ACCOUNT_EXISTS);
        }
    }
}
