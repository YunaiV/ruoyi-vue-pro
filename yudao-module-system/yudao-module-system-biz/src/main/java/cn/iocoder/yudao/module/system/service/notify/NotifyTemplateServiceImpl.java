package cn.iocoder.yudao.module.system.service.notify;

import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateUpdateReqVO;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.system.convert.notify.NotifyTemplateConvert;
import cn.iocoder.yudao.module.system.dal.mysql.notify.NotifyTemplateMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 站内信模版 Service 实现类
 *
 * @author xrcoder
 */
@Service
@Validated
public class NotifyTemplateServiceImpl implements NotifyTemplateService {

    @Resource
    private NotifyTemplateMapper notifyTemplateMapper;

    @Override
    public Long createNotifyTemplate(NotifyTemplateCreateReqVO createReqVO) {
        // 插入
        NotifyTemplateDO notifyTemplate = NotifyTemplateConvert.INSTANCE.convert(createReqVO);
        notifyTemplateMapper.insert(notifyTemplate);
        // 返回
        return notifyTemplate.getId();
    }

    @Override
    public void updateNotifyTemplate(NotifyTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateNotifyTemplateExists(updateReqVO.getId());
        // 更新
        NotifyTemplateDO updateObj = NotifyTemplateConvert.INSTANCE.convert(updateReqVO);
        notifyTemplateMapper.updateById(updateObj);
    }

    @Override
    public void deleteNotifyTemplate(Long id) {
        // 校验存在
        this.validateNotifyTemplateExists(id);
        // 删除
        notifyTemplateMapper.deleteById(id);
    }

    private void validateNotifyTemplateExists(Long id) {
        if (notifyTemplateMapper.selectById(id) == null) {
            throw exception(NOTIFY_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public NotifyTemplateDO getNotifyTemplate(Long id) {
        return notifyTemplateMapper.selectById(id);
    }

    @Override
    public List<NotifyTemplateDO> getNotifyTemplateList(Collection<Long> ids) {
        return notifyTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<NotifyTemplateDO> getNotifyTemplatePage(NotifyTemplatePageReqVO pageReqVO) {
        return notifyTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<NotifyTemplateDO> getNotifyTemplateList(NotifyTemplateExportReqVO exportReqVO) {
        return notifyTemplateMapper.selectList(exportReqVO);
    }

}
