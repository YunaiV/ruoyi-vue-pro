package cn.iocoder.yudao.module.srm.service.product.ironwoodfilingcabinet;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.controller.admin.product.vo.product.ErpProductSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.srm.service.product.ErpProductServiceImpl;
import cn.iocoder.yudao.module.srm.service.product.bo.ErpProductBO;
import cn.iocoder.yudao.module.srm.service.product.ironwoodfilingcabinet.bo.IronWoodFilingCabinetBO;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.srm.enums.ErrorCodeConstants.PRODUCT_FIELD_NOT_MATCH;

/**
 * 铁木文件柜
 * 
 * @author Wqh
 */
@Service
public class ErpProductIronWoodFilingCabinetServiceImpl extends ErpProductServiceImpl implements ErpProductIronWoodFilingCabinetService {
    @Override
    public ErpProductBO toBO(ErpProductSaveReqVO saveReqVO) {
        validateFields(saveReqVO);
        return BeanUtils.toBean(saveReqVO, IronWoodFilingCabinetBO.class);
    }

    @Override
    public ErpProductBO toBO(ErpProductDO productDO) {
        return BeanUtils.toBean(productDO, IronWoodFilingCabinetBO.class);
    }
    @Override
    public void validateFields(ErpProductSaveReqVO saveReqVO){
        ThrowUtil.ifThrow(BeanUtils.areAllNonNullFieldsPresent(saveReqVO, IronWoodFilingCabinetBO.class),PRODUCT_FIELD_NOT_MATCH);
    }
}
