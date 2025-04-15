package cn.iocoder.yudao.module.erp.service.product.chestdrawers;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import cn.iocoder.yudao.module.erp.service.product.chestdrawers.bo.ChestDrawersBO;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

/**
 * 斗柜
 * 
 * @author Wqh
 */
@Service
public class ErpProductChestDrawersServiceImpl extends ErpProductServiceImpl implements ErpProductChestDrawersService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, ChestDrawersBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, ChestDrawersBO.class);
    }
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, ChestDrawersBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
