package cn.iocoder.yudao.module.trade.convert.delivery;

import java.util.*;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.*;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateFreeDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 快递运费模板 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface DeliveryExpressTemplateConvert {

    DeliveryExpressTemplateConvert INSTANCE = Mappers.getMapper(DeliveryExpressTemplateConvert.class);

    DeliveryExpressTemplateDO convert(DeliveryExpressTemplateCreateReqVO bean);

    DeliveryExpressTemplateDO convert(DeliveryExpressTemplateUpdateReqVO bean);

    DeliveryExpressTemplateSimpleRespVO convert(DeliveryExpressTemplateDO bean);

    DeliveryExpressTemplateRespVO convert2(DeliveryExpressTemplateDO bean);

    List<DeliveryExpressTemplateSimpleRespVO> convertList(List<DeliveryExpressTemplateDO> list);

    PageResult<DeliveryExpressTemplateSimpleRespVO> convertPage(PageResult<DeliveryExpressTemplateDO> page);

    List<DeliveryExpressTemplateExcelVO> convertList02(List<DeliveryExpressTemplateDO> list);

    DeliveryExpressTemplateChargeDO convertTemplateCharge(Long templateId, Integer chargeMode, ExpressTemplateChargeBaseVO vo);

    DeliveryExpressTemplateChargeDO convertTemplateCharge(ExpressTemplateChargeUpdateVO vo);

    DeliveryExpressTemplateFreeDO convertTemplateFree(Long templateId, ExpressTemplateFreeBaseVO vo);

    DeliveryExpressTemplateFreeDO convertTemplateFree(ExpressTemplateFreeUpdateVO vo);

    List<ExpressTemplateChargeBaseVO> convertTemplateChargeList(List<DeliveryExpressTemplateChargeDO> list);

    List<ExpressTemplateFreeBaseVO> convertTemplateFreeList(List<DeliveryExpressTemplateFreeDO> list);

    default List<DeliveryExpressTemplateChargeDO> convertTemplateChargeList(Long templateId, Integer chargeMode, List<ExpressTemplateChargeBaseVO> list){
        if(CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }
        List<DeliveryExpressTemplateChargeDO> templateChargeList = new ArrayList<>( list.size() );
        for (ExpressTemplateChargeBaseVO item : list) {
            templateChargeList.add(convertTemplateCharge(templateId, chargeMode, item));
        }
        return templateChargeList;
    }



    default List<DeliveryExpressTemplateFreeDO> convertTemplateFreeList(Long templateId,  List<ExpressTemplateFreeBaseVO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<DeliveryExpressTemplateFreeDO> templateFreeList = new ArrayList<>(list.size());
        for (ExpressTemplateFreeBaseVO item : list) {
            templateFreeList.add(convertTemplateFree(templateId, item));
        }
        return templateFreeList;
    }

    default DeliveryExpressTemplateRespVO convert(DeliveryExpressTemplateDO bean,
                                          List<DeliveryExpressTemplateChargeDO> chargeList,
                                          List<DeliveryExpressTemplateFreeDO> freeList){
        DeliveryExpressTemplateRespVO respVO = convert2(bean);
        respVO.setTemplateCharge(convertTemplateChargeList(chargeList));
        respVO.setTemplateFree(convertTemplateFreeList(freeList));
        return respVO;
    }
}
