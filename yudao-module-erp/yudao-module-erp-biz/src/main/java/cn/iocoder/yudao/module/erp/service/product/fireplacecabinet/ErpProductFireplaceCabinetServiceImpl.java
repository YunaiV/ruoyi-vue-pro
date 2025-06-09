package cn.iocoder.yudao.module.erp.service.product.fireplacecabinet;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import cn.iocoder.yudao.module.erp.service.product.fireplacecabinet.bo.FireplaceCabinetBO;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.erp.enums.ErpErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

/**
 * 壁炉柜
 * 
 * @author Wqh
 */
@Service
public class ErpProductFireplaceCabinetServiceImpl extends ErpProductServiceImpl implements ErpProductFireplaceCabinetService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, FireplaceCabinetBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, FireplaceCabinetBO.class);
    }
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, FireplaceCabinetBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
