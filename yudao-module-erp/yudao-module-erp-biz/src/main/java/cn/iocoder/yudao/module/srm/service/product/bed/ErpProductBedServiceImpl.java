package cn.iocoder.yudao.module.srm.service.product.bed;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.srm.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.bed.bo.BedBO;
import cn.iocoder.yudao.module.srm.service.product.bo.ErpProductBO;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.srm.enums.ErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

/**
 * 床
 * 
 * @author Wqh
 */
@Service
public class ErpProductBedServiceImpl extends ErpProductServiceImpl implements ErpProductBedService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, BedBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, BedBO.class);
    }
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, BedBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
