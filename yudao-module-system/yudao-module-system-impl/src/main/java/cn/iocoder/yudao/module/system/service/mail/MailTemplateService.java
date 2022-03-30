package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;

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
     * @param createReqVO
     * @return
     */
    Long create(MailTemplateCreateReqVO createReqVO);

    /**
     * 邮箱模版修改
     * @param updateReqVO
     */
    void update(MailTemplateUpdateReqVO updateReqVO);

    /**
     * 邮箱模版删除
     * @param id
     */
    void delete(Long id);

    /**
     * 获取邮箱模版
     * @param id
     * @return
     */
    MailTemplateDO getMailTemplate(Long id);

    /**
     * 获取邮箱模版分页
     * @param pageReqVO
     * @return
     */
    PageResult<MailTemplateDO> getMailTemplatePage(MailTemplatePageReqVO pageReqVO);

    /**
     * 获取邮箱模板数组
     * @return
     */
    List<MailTemplateDO> getMailTemplateList();
}
