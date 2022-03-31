package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.send.MailReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;

import javax.validation.Valid;
import java.util.List;

/**
 *  邮箱模版服务类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailTemplateService {

    /**
     * 邮箱模版创建
     * @param createReqVO 邮箱信息
     * @return 编号
     */
    Long create(@Valid MailTemplateCreateReqVO createReqVO);

    /**
     * 邮箱模版修改
     * @param updateReqVO 邮箱信息
     */
    void update(@Valid MailTemplateUpdateReqVO updateReqVO);

    /**
     * 邮箱模版删除
     * @param id 编号
     */
    void delete(Long id);

    /**
     * 获取邮箱模版
     *
     * @param id 编号
     * @return 邮件模版
     */
    MailTemplateDO getMailTemplate(Long id);

    /**
     * 获取邮箱模版分页
     *
     * @param pageReqVO 模版信息
     * @return 邮箱模版分页信息
     */
    PageResult<MailTemplateDO> getMailTemplatePage(MailTemplatePageReqVO pageReqVO);

    /**
     * 获取邮箱模板数组
     *
     * @return 模版数组
     */
    List<MailTemplateDO> getMailTemplateList();

    /**
     * 发送邮件
     *
     * @param mailReqVO 邮件发送信息
     */
    void sendMail(MailReqVO mailReqVO);
}
