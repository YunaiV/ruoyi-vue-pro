package cn.iocoder.yudao.module.trade.convert.delivery;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.*;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateFreeDO;
import cn.iocoder.yudao.module.trade.service.delivery.bo.DeliveryExpressTemplateRespBO;
import com.google.common.collect.Maps;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.findFirst;

@Mapper
public interface DeliveryExpressTemplateConvert {

    DeliveryExpressTemplateConvert INSTANCE = Mappers.getMapper(DeliveryExpressTemplateConvert.class);

    // ========== Template ==========

    DeliveryExpressTemplateDO convert(DeliveryExpressTemplateCreateReqVO bean);

    DeliveryExpressTemplateDO convert(DeliveryExpressTemplateUpdateReqVO bean);

    DeliveryExpressTemplateRespVO convert(DeliveryExpressTemplateDO bean);

    DeliveryExpressTemplateDetailRespVO convert2(DeliveryExpressTemplateDO bean);

    List<DeliveryExpressTemplateRespVO> convertList(List<DeliveryExpressTemplateDO> list);

    List<DeliveryExpressTemplateSimpleRespVO> convertList1(List<DeliveryExpressTemplateDO> list);

    PageResult<DeliveryExpressTemplateRespVO> convertPage(PageResult<DeliveryExpressTemplateDO> page);

    default DeliveryExpressTemplateDetailRespVO convert(DeliveryExpressTemplateDO bean,
                                                        List<DeliveryExpressTemplateChargeDO> chargeList,
                                                        List<DeliveryExpressTemplateFreeDO> freeList) {
        DeliveryExpressTemplateDetailRespVO respVO = convert2(bean);
        respVO.setCharges(convertTemplateChargeList(chargeList));
        respVO.setFrees(convertTemplateFreeList(freeList));
        return respVO;
    }

    // ========== Template Charge ==========

    DeliveryExpressTemplateChargeDO convertTemplateCharge(Long templateId, Integer chargeMode, DeliveryExpressTemplateChargeBaseVO vo);

    DeliveryExpressTemplateRespBO.Charge convertTemplateCharge(DeliveryExpressTemplateChargeDO bean);

    default List<DeliveryExpressTemplateChargeDO> convertTemplateChargeList(Long templateId, Integer chargeMode, List<DeliveryExpressTemplateChargeBaseVO> list) {
        return CollectionUtils.convertList(list, vo -> convertTemplateCharge(templateId, chargeMode, vo));
    }

    // ========== Template Free ==========

    DeliveryExpressTemplateFreeDO convertTemplateFree(Long templateId, DeliveryExpressTemplateFreeBaseVO vo);

    DeliveryExpressTemplateRespBO.Free convertTemplateFree(DeliveryExpressTemplateFreeDO bean);

    List<DeliveryExpressTemplateChargeBaseVO> convertTemplateChargeList(List<DeliveryExpressTemplateChargeDO> list);

    List<DeliveryExpressTemplateFreeBaseVO> convertTemplateFreeList(List<DeliveryExpressTemplateFreeDO> list);

    default List<DeliveryExpressTemplateFreeDO> convertTemplateFreeList(Long templateId, List<DeliveryExpressTemplateFreeBaseVO> list) {
        return CollectionUtils.convertList(list, vo -> convertTemplateFree(templateId, vo));
    }

    default Map<Long, DeliveryExpressTemplateRespBO> convertMap(Integer areaId, List<DeliveryExpressTemplateDO> templateList,
                                                                List<DeliveryExpressTemplateChargeDO> chargeList,
                                                                List<DeliveryExpressTemplateFreeDO> freeList) {
        Map<Long, List<DeliveryExpressTemplateChargeDO>> templateIdChargeMap = convertMultiMap(chargeList,
                DeliveryExpressTemplateChargeDO::getTemplateId);
        Map<Long, List<DeliveryExpressTemplateFreeDO>> templateIdFreeMap = convertMultiMap(freeList,
                DeliveryExpressTemplateFreeDO::getTemplateId);
        // 组合运费模板配置 RespBO
        Map<Long, DeliveryExpressTemplateRespBO> result = Maps.newHashMapWithExpectedSize(templateList.size());
        templateList.forEach(template -> {
            DeliveryExpressTemplateRespBO bo = new DeliveryExpressTemplateRespBO()
                    .setChargeMode(template.getChargeMode())
                    .setCharge(convertTemplateCharge(findFirst(templateIdChargeMap.get(template.getId()), charge -> charge.getAreaIds().contains(areaId))))
                    .setFree(convertTemplateFree(findFirst(templateIdFreeMap.get(template.getId()), free -> free.getAreaIds().contains(areaId))));
            result.put(template.getId(), bo);
        });
        return result;
    }

}
