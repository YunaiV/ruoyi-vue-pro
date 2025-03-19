package cn.iocoder.yudao.module.erp.service.product.bedsidecabinet;

import org.springframework.stereotype.Service;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import org.springframework.stereotype.Service;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;
import cn.iocoder.yudao.module.erp.service.product.bedsidecabinet.bo.BedsideCabinetBO;

/**
 * 床头柜
 * 
 * @author Wqh
 */
@Service
public class ErpProductBedsideCabinetServiceImpl extends ErpProductServiceImpl implements ErpProductBedsideCabinetService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, BedsideCabinetBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, BedsideCabinetBO.class);
    }
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, BedsideCabinetBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
