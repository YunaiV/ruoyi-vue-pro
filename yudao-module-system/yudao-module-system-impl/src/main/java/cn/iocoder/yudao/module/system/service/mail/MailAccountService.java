package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountUpdateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.send.MailSendVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;

import java.util.List;


/**
 * <p>
 *  邮箱账号 Service 接口
 * </p>  // TODO wangjingyi：不用 <p></p> 标签；
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailAccountService { // TODO wangjingyi：方法的注释

    Long create(MailAccountCreateReqVO createReqVO);

    void update(MailAccountUpdateReqVO updateReqVO);

    void delete(Long id);

    MailAccountDO getMailAccount(Long id);

    PageResult<MailAccountDO> getMailAccountPage(MailAccountPageReqVO pageReqVO);

    List<MailAccountDO> getMailAccountList();

    void sendMail(MailSendVO mailSendVO);
}
