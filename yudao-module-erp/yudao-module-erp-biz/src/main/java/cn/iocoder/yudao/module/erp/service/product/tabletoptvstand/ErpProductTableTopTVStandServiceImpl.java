package cn.iocoder.yudao.module.erp.service.product.tabletoptvstand;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.erp.service.product.bo.ErpProductBO;
import cn.iocoder.yudao.module.erp.service.product.tabletoptvstand.bo.TableTopTVStandBO;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

/**
 * 桌面电视架
 * 
 * @author Wqh
 */
@Service
public class ErpProductTableTopTVStandServiceImpl extends ErpProductServiceImpl implements ErpProductTableTopTVStandService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, TableTopTVStandBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, TableTopTVStandBO.class);
    }
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, TableTopTVStandBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
