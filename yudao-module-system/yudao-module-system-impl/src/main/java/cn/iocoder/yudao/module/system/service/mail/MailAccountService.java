package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.account.MailAccountUpdateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.send.MailReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;

import java.util.List;


/**
 * 邮箱账号 Service 接口
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailAccountService {

    // TODO @wangjingyi：注释，完整；参数校验还是要做的；

    /**
     * 创建邮箱账号 // TODO @wangjingyi：方法描述，和参数要空行
     * @param createReqVO
     * @return
     */
    Long create(MailAccountCreateReqVO createReqVO);

    /**
     * 修改邮箱账号
     * @param updateReqVO
     */
    void update(MailAccountUpdateReqVO updateReqVO);

    /**
     * 删除邮箱账号
     * @param id
     */
    void delete(Long id);

    /**
     * 获取邮箱账号信息
     * @param id
     * @return
     */
    MailAccountDO getMailAccount(Long id);

    /**
     * 获取邮箱账号分页信息
     * @param pageReqVO
     * @return
     */
    PageResult<MailAccountDO> getMailAccountPage(MailAccountPageReqVO pageReqVO);

    /**
     * 获取邮箱数组信息
     * @return
     */
    List<MailAccountDO> getMailAccountList();

    /**
     * 发送邮件
     * @param mailReqVO
     */
    void sendMail(MailReqVO mailReqVO);
}
