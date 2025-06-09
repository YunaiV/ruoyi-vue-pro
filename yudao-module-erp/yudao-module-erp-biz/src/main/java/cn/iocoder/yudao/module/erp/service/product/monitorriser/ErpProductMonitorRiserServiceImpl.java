package cn.iocoder.yudao.module.erp.service.product.monitorriser;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import cn.iocoder.yudao.module.erp.service.product.monitorriser.bo.MonitorRiserBO;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.erp.enums.ErpErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

/**
 * 显示器增高架
 * 
 * @author Wqh
 */
@Service
public class ErpProductMonitorRiserServiceImpl extends ErpProductServiceImpl implements ErpProductMonitorRiserService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, MonitorRiserBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, MonitorRiserBO.class);
    }
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, MonitorRiserBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
