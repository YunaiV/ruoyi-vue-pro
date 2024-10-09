package cn.iocoder.yudao.module.erp.service.purchase;

import java.util.*;

import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.PurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.PurchaseRequestSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.PurchaseRequestDO;
import jakarta.validation.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * ERP采购申请单 Service 接口
 *
 * @author 索迈管理员
 */
public interface PurchaseRequestService {

    /**
     * 创建ERP采购申请单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPurchaseRequest(@Valid PurchaseRequestSaveReqVO createReqVO);

    /**
     * 更新ERP采购申请单
     *
     * @param updateReqVO 更新信息
     */
    void updatePurchaseRequest(@Valid PurchaseRequestSaveReqVO updateReqVO);

    /**
     * 删除ERP采购申请单
     *
     * @param id 编号
     */
    void deletePurchaseRequest(Long id);

    /**
     * 获得ERP采购申请单
     *
     * @param id 编号
     * @return ERP采购申请单
     */
    PurchaseRequestDO getPurchaseRequest(Long id);

    /**
     * 获得ERP采购申请单分页
     *
     * @param pageReqVO 分页查询
     * @return ERP采购申请单分页
     */
    PageResult<PurchaseRequestDO> getPurchaseRequestPage(PurchaseRequestPageReqVO pageReqVO);

}