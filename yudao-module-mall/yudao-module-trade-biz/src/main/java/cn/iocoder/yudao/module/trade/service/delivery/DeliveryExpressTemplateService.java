package cn.iocoder.yudao.module.trade.service.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.DeliveryExpressTemplateCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.DeliveryExpressTemplatePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.DeliveryExpressTemplateRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.DeliveryExpressTemplateUpdateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 快递运费模板 Service 接口
 *
 * @author jason
 */
public interface DeliveryExpressTemplateService {

    /**
     * 创建快递运费模板
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDeliveryExpressTemplate(@Valid DeliveryExpressTemplateCreateReqVO createReqVO);

    /**
     * 更新快递运费模板
     *
     * @param updateReqVO 更新信息
     */
    void updateDeliveryExpressTemplate(@Valid DeliveryExpressTemplateUpdateReqVO updateReqVO);

    /**
     * 删除快递运费模板
     *
     * @param id 编号
     */
    void deleteDeliveryExpressTemplate(Long id);

    /**
     * 获得快递运费模板
     *
     * @param id 编号
     * @return 快递运费模板详情
     */
    DeliveryExpressTemplateRespVO getDeliveryExpressTemplate(Long id);

    /**
     * 获得快递运费模板列表
     *
     * @param ids 编号
     * @return 快递运费模板列表
     */
    List<DeliveryExpressTemplateDO> getDeliveryExpressTemplateList(Collection<Long> ids);

    /**
     * 获得快递运费模板分页
     *
     * @param pageReqVO 分页查询
     * @return 快递运费模板分页
     */
    PageResult<DeliveryExpressTemplateDO> getDeliveryExpressTemplatePage(DeliveryExpressTemplatePageReqVO pageReqVO);

    /**
     * 校验快递运费模板
     *
     * 如果校验不通过，抛出 {@link cn.iocoder.yudao.framework.common.exception.ServiceException} 异常
     *
     * @param templateId 模板编号
     * @return 快递运费模板
     */
    DeliveryExpressTemplateDO validateDeliveryExpressTemplate(Long templateId);
}
