package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.PurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.PurchaseRequestSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.PurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.PurchaseRequestMapper;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;


import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP采购申请单 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
public class PurchaseRequestServiceImpl implements PurchaseRequestService {

    @Resource
    private PurchaseRequestMapper purchaseRequestMapper;

    @Override
    public Long createPurchaseRequest(PurchaseRequestSaveReqVO createReqVO) {
        // 插入
        PurchaseRequestDO purchaseRequest = BeanUtils.toBean(createReqVO, PurchaseRequestDO.class);
        purchaseRequestMapper.insert(purchaseRequest);
        // 返回
        return purchaseRequest.getId();
    }

    @Override
    public void updatePurchaseRequest(PurchaseRequestSaveReqVO updateReqVO) {
        // 校验存在
        validatePurchaseRequestExists(updateReqVO.getId());
        // 更新
        PurchaseRequestDO updateObj = BeanUtils.toBean(updateReqVO, PurchaseRequestDO.class);
        purchaseRequestMapper.updateById(updateObj);
    }

    @Override
    public void deletePurchaseRequest(Long id) {
        // 校验存在
        validatePurchaseRequestExists(id);
        // 删除
        purchaseRequestMapper.deleteById(id);
    }

    private void validatePurchaseRequestExists(Long id) {
        if (purchaseRequestMapper.selectById(id) == null) {
            throw exception(PURCHASE_REQUEST_NOT_EXISTS);
        }
    }

    @Override
    public PurchaseRequestDO getPurchaseRequest(Long id) {
        return purchaseRequestMapper.selectById(id);
    }

    @Override
    public PageResult<PurchaseRequestDO> getPurchaseRequestPage(PurchaseRequestPageReqVO pageReqVO) {
        return purchaseRequestMapper.selectPage(pageReqVO);
    }

}