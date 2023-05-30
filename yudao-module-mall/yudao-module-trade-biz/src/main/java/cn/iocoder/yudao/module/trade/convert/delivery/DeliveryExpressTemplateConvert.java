package cn.iocoder.yudao.module.trade.convert.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.*;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateFreeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DeliveryExpressTemplateConvert {

    DeliveryExpressTemplateConvert INSTANCE = Mappers.getMapper(DeliveryExpressTemplateConvert.class);

    // ========== Template ==========

    DeliveryExpressTemplateDO convert(DeliveryExpressTemplateCreateReqVO bean);

    DeliveryExpressTemplateDO convert(DeliveryExpressTemplateUpdateReqVO bean);

    DeliveryExpressTemplateSimpleRespVO convert(DeliveryExpressTemplateDO bean);

    DeliveryExpressTemplateRespVO convert2(DeliveryExpressTemplateDO bean);

    List<DeliveryExpressTemplateSimpleRespVO> convertList(List<DeliveryExpressTemplateDO> list);

    PageResult<DeliveryExpressTemplateSimpleRespVO> convertPage(PageResult<DeliveryExpressTemplateDO> page);

    default DeliveryExpressTemplateRespVO convert(DeliveryExpressTemplateDO bean,
                                                  List<DeliveryExpressTemplateChargeDO> chargeList,
                                                  List<DeliveryExpressTemplateFreeDO> freeList){
        DeliveryExpressTemplateRespVO respVO = convert2(bean);
        respVO.setTemplateCharge(convertTemplateChargeList(chargeList));
        respVO.setTemplateFree(convertTemplateFreeList(freeList));
        return respVO;
    }

    // ========== Template Charge ==========

    DeliveryExpressTemplateChargeDO convertTemplateCharge(Long templateId, Integer chargeMode, ExpressTemplateChargeBaseVO vo);

    DeliveryExpressTemplateChargeDO convertTemplateCharge(DeliveryExpressTemplateUpdateReqVO.ExpressTemplateChargeUpdateVO vo);

    default List<DeliveryExpressTemplateChargeDO> convertTemplateChargeList(Long templateId, Integer chargeMode, List<ExpressTemplateChargeBaseVO> list) {
        return CollectionUtils.convertList(list, vo -> convertTemplateCharge(templateId, chargeMode, vo));
    }

    // ========== Template Free ==========

    DeliveryExpressTemplateFreeDO convertTemplateFree(Long templateId, ExpressTemplateFreeBaseVO vo);

    DeliveryExpressTemplateFreeDO convertTemplateFree(DeliveryExpressTemplateUpdateReqVO.ExpressTemplateFreeUpdateVO vo);

    List<ExpressTemplateChargeBaseVO> convertTemplateChargeList(List<DeliveryExpressTemplateChargeDO> list);

    List<ExpressTemplateFreeBaseVO> convertTemplateFreeList(List<DeliveryExpressTemplateFreeDO> list);

    default List<DeliveryExpressTemplateFreeDO> convertTemplateFreeList(Long templateId, List<ExpressTemplateFreeBaseVO> list) {
        return CollectionUtils.convertList(list, vo -> convertTemplateFree(templateId, vo));
    }

}
