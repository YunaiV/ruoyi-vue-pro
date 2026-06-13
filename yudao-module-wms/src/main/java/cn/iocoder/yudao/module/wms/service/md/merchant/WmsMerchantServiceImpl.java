package cn.iocoder.yudao.module.wms.service.md.merchant;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.md.merchant.vo.WmsMerchantListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.merchant.vo.WmsMerchantPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.merchant.vo.WmsMerchantSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.merchant.WmsMerchantDO;
import cn.iocoder.yudao.module.wms.dal.mysql.md.merchant.WmsMerchantMapper;
import cn.iocoder.yudao.module.wms.enums.md.WmsMerchantTypeEnum;
import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import cn.iocoder.yudao.module.wms.service.order.receipt.WmsReceiptOrderService;
import cn.iocoder.yudao.module.wms.service.order.shipment.WmsShipmentOrderService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 往来企业 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsMerchantServiceImpl implements WmsMerchantService {

    @Resource
    private WmsMerchantMapper merchantMapper;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsReceiptOrderService receiptOrderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private WmsShipmentOrderService shipmentOrderService;

    @Override
    public Long createMerchant(WmsMerchantSaveReqVO createReqVO) {
        validateMerchantSaveData(null, createReqVO);

        // 新增
        WmsMerchantDO merchant = BeanUtils.toBean(createReqVO, WmsMerchantDO.class);
        merchantMapper.insert(merchant);
        return merchant.getId();
    }

    @Override
    public void updateMerchant(WmsMerchantSaveReqVO updateReqVO) {
        // 校验存在
        validateMerchantExists(updateReqVO.getId());
        validateMerchantSaveData(updateReqVO.getId(), updateReqVO);

        // 更新
        WmsMerchantDO updateObj = BeanUtils.toBean(updateReqVO, WmsMerchantDO.class);
        merchantMapper.updateById(updateObj);
    }

    private void validateMerchantSaveData(Long id, WmsMerchantSaveReqVO reqVO) {
        // 校验 code 唯一
        validateMerchantCodeUnique(id, reqVO.getCode());
        // 校验 name 唯一
        validateMerchantNameUnique(id, reqVO.getName());
    }

    private void validateMerchantCodeUnique(Long id, String code) {
        WmsMerchantDO merchant = merchantMapper.selectByCode(code);
        if (merchant == null) {
            return;
        }
        if (id == null || ObjectUtil.notEqual(merchant.getId(), id)) {
            throw exception(MERCHANT_CODE_DUPLICATE);
        }
    }

    private void validateMerchantNameUnique(Long id, String name) {
        WmsMerchantDO merchant = merchantMapper.selectByName(name);
        if (merchant == null) {
            return;
        }
        if (id == null || ObjectUtil.notEqual(merchant.getId(), id)) {
            throw exception(MERCHANT_NAME_DUPLICATE);
        }
    }

    @Override
    public void deleteMerchant(Long id) {
        // 校验存在
        validateMerchantExists(id);
        // 校验未被单据使用
        validateMerchantUnused(id);

        // 删除
        merchantMapper.deleteById(id);
    }

    private void validateMerchantUnused(Long id) {
        if (receiptOrderService.getReceiptOrderCountByMerchantId(id) > 0) {
            throw exception(MERCHANT_HAS_ORDER, WmsOrderTypeEnum.RECEIPT.getName());
        }
        if (shipmentOrderService.getShipmentOrderCountByMerchantId(id) > 0) {
            throw exception(MERCHANT_HAS_ORDER, WmsOrderTypeEnum.SHIPMENT.getName());
        }
    }

    @Override
    public WmsMerchantDO validateMerchantExists(Long id) {
        WmsMerchantDO merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw exception(MERCHANT_NOT_EXISTS);
        }
        return merchant;
    }

    @Override
    public WmsMerchantDO validateSupplierMerchantExists(Long id) {
        WmsMerchantDO merchant = validateMerchantExists(id);
        if (ObjectUtil.notEqual(merchant.getType(), WmsMerchantTypeEnum.SUPPLIER.getType())
                && ObjectUtil.notEqual(merchant.getType(), WmsMerchantTypeEnum.CUSTOMER_SUPPLIER.getType())) {
            throw exception(MERCHANT_NOT_SUPPLIER);
        }
        return merchant;
    }

    @Override
    public WmsMerchantDO validateCustomerMerchantExists(Long id) {
        WmsMerchantDO merchant = validateMerchantExists(id);
        if (ObjectUtil.notEqual(merchant.getType(), WmsMerchantTypeEnum.CUSTOMER.getType())
                && ObjectUtil.notEqual(merchant.getType(), WmsMerchantTypeEnum.CUSTOMER_SUPPLIER.getType())) {
            throw exception(MERCHANT_NOT_CUSTOMER);
        }
        return merchant;
    }

    @Override
    public WmsMerchantDO getMerchant(Long id) {
        return merchantMapper.selectById(id);
    }

    @Override
    public PageResult<WmsMerchantDO> getMerchantPage(WmsMerchantPageReqVO pageReqVO) {
        return merchantMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsMerchantDO> getMerchantList(WmsMerchantListReqVO listReqVO) {
        return merchantMapper.selectList(listReqVO);
    }

    @Override
    public List<WmsMerchantDO> getMerchantList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.of();
        }
        return merchantMapper.selectByIds(ids);
    }

}
