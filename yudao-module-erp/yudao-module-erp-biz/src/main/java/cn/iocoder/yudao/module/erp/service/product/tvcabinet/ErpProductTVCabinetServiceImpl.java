package cn.iocoder.yudao.module.erp.service.product.tvcabinet;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import cn.iocoder.yudao.module.erp.service.product.tvcabinet.bo.TVCabinetBO;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.erp.enums.ErpErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

/**
 * 电视柜
 * 
 * @author Wqh
 */
@Service
public class ErpProductTVCabinetServiceImpl extends ErpProductServiceImpl implements ErpProductTVCabinetService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, TVCabinetBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, TVCabinetBO.class);
    }
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, TVCabinetBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
