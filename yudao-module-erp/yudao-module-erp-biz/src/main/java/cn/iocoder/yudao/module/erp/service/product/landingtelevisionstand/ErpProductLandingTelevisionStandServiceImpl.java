package cn.iocoder.yudao.module.erp.service.product.landingtelevisionstand;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import org.springframework.stereotype.Service;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

import cn.iocoder.yudao.module.erp.service.product.landingtelevisionstand.bo.LandingTelevisionStandBO;

/**
 * @author: Wqh
 * @date: 2024/12/3 16:40
 */
@Service
public class ErpProductLandingTelevisionStandServiceImpl extends ErpProductServiceImpl implements ErpProductLandingTelevisionStandService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, LandingTelevisionStandBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, LandingTelevisionStandBO.class);
    }

    /***
    * @Author Wqh
    * @Description 校验电视机架字段是否匹配
    * @Date 15:00 2024/12/11
    * @Param [vo]
    * @return void
    **/
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, LandingTelevisionStandBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
