package cn.iocoder.yudao.module.system.service.mail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.mail.vo.template.MailTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
public interface MailTemplateService {

    Long create(MailTemplateCreateReqVO createReqVO);

    void update(MailTemplateUpdateReqVO updateReqVO);

    void delete(Long id);

    MailTemplateDO getMailTemplate(Long id);

    PageResult<MailTemplateDO> getMailTemplatePage(MailTemplatePageReqVO pageReqVO);

    List<MailTemplateDO> getMailTemplateList();
}
