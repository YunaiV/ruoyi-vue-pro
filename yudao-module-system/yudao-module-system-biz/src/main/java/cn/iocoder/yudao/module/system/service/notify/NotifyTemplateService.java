package cn.iocoder.yudao.module.system.service.notify;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplatePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notify.vo.template.NotifyTemplateUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notify.NotifyTemplateDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 站内信模版 Service 接口
 *
 * @author xrcoder
 */
public interface NotifyTemplateService {

    /**
     * 创建站内信模版
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createNotifyTemplate(@Valid NotifyTemplateCreateReqVO createReqVO);

    /**
     * 更新站内信模版
     *
     * @param updateReqVO 更新信息
     */
    void updateNotifyTemplate(@Valid NotifyTemplateUpdateReqVO updateReqVO);

    /**
     * 删除站内信模版
     *
     * @param id 编号
     */
    void deleteNotifyTemplate(Long id);

    /**
     * 获得站内信模版
     *
     * @param id 编号
     * @return 站内信模版
     */
    NotifyTemplateDO getNotifyTemplate(Long id);

    /**
     * 获得站内信模版列表
     *
     * @param ids 编号
     * @return 站内信模版列表
     */
    List<NotifyTemplateDO> getNotifyTemplateList(Collection<Long> ids);

    /**
     * 获得站内信模版分页
     *
     * @param pageReqVO 分页查询
     * @return 站内信模版分页
     */
    PageResult<NotifyTemplateDO> getNotifyTemplatePage(NotifyTemplatePageReqVO pageReqVO);

    /**
     * 获得站内信模版列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 站内信模版列表
     */
    List<NotifyTemplateDO> getNotifyTemplateList(NotifyTemplateExportReqVO exportReqVO);

}
