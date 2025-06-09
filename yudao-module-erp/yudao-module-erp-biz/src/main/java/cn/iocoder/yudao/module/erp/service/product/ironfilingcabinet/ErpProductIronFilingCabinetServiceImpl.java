package cn.iocoder.yudao.module.erp.service.product.ironfilingcabinet;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import cn.iocoder.yudao.module.erp.service.product.ironfilingcabinet.bo.IronFilingCabinetBO;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.erp.enums.ErpErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

/**
 * 铁质文件柜
 * 
 * @author Wqh
 */
@Service
public class ErpProductIronFilingCabinetServiceImpl extends ErpProductServiceImpl implements ErpProductIronFilingCabinetService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, IronFilingCabinetBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, IronFilingCabinetBO.class);
    }
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, IronFilingCabinetBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
