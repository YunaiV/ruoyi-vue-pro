package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestItemsSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP采购申请单 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErpPurchaseRequestServiceImpl implements ErpPurchaseRequestService {
    private final ErpPurchaseRequestMapper erpPurchaseRequestMapper;
    private final ErpPurchaseRequestItemsMapper erpPurchaseRequestItemsMapper;
    private final ErpWarehouseService erpWarehouseService;
    private final ErpProductService productService;
    private final ErpNoRedisDAO noRedisDAO;
    private final AdminUserApi adminUserApi;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseRequest(ErpPurchaseRequestSaveReqVO createReqVO) {
        //获取单据日期，不为空就拿，为空就当前时间
        LocalDateTime date = createReqVO.getRequestTime();
        if (date == null) {
            createReqVO.setRequestTime(LocalDateTime.now());
        }
        //1.校验
        //生成单据编号
        String no = noRedisDAO.generate(ErpNoRedisDAO.PURCHASE_REQUEST_NO_PREFIX, PURCHASE_REQUEST_NO_OUT_OF_BOUNDS);
        //1.1 校验编号no是否在数据库中重复
        ThrowUtil.ifThrow(erpPurchaseRequestMapper.selectByNo(no) != null, PURCHASE_REQUEST_NO_EXISTS);
        //1.2 校验子表合法
        List<ErpPurchaseRequestItemsDO> itemsDOList = validatePurchaseRequestItems(createReqVO.getItems());
        //1.3 校验部门合法
        erpWarehouseService.validWarehouseList(Collections.singleton(createReqVO.getApplicationDept()));
        //1.4 校验申请人合法
        adminUserApi.validateUser(createReqVO.getApplicant());
        //bean拷贝
        ErpPurchaseRequestDO purchaseRequest = BeanUtils.toBean(createReqVO, ErpPurchaseRequestDO.class);
        //为单据编号赋值
        purchaseRequest.setNo(no);
        //为单据编号设置初始审核状态
        purchaseRequest.setStatus(ErpAuditStatus.PROCESS.getStatus());
        //2. 插入主表的申请单数据
        ThrowUtil.ifThrow(erpPurchaseRequestMapper.insert(purchaseRequest) <= 0, PURCHASE_REQUEST_ADD_FAIL_APPROVE);
        //获取主表主键id
        Long id = purchaseRequest.getId();
        //将父id赋予子表
        itemsDOList.forEach(i -> i.setRequestId(id));
        //3. 批量插入子表数据
        ThrowUtil.ifThrow(!erpPurchaseRequestItemsMapper.insertBatch(itemsDOList), PURCHASE_REQUEST_ADD_FAIL_PRODUCT);
        // 返回单据id
        return id;
    }

    @Override
    public List<ErpPurchaseRequestItemsDO> validatePurchaseRequestItems(List<ErpPurchaseRequestItemsSaveReqVO> items) {
        // 1. 校验产品有效性
        productService.validProductList(convertSet(items, ErpPurchaseRequestItemsSaveReqVO::getProductId));
        // 1.1 校验仓库有效性
        erpWarehouseService.validWarehouseList(convertSet(items, ErpPurchaseRequestItemsSaveReqVO::getWarehouseId));
        // 2. 转化为 ErpPurchaseRequestItemsDO 列表
        return convertList(items, o -> BeanUtils.toBean(o, ErpPurchaseRequestItemsDO.class));
    }

    @Override
    public void validatePurchaseRequestItemsMasterId(Long masterId, List<Long> ids) {
        // 校验子单requestId是否关联主单的id
        ThrowUtil.ifThrow(erpPurchaseRequestItemsMapper.selectListByRequestId(masterId).stream().noneMatch(i -> ids.contains(i.getId())), PURCHASE_REQUEST_UPDATE_FAIL_REQUEST_ID, masterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseRequest(ErpPurchaseRequestSaveReqVO updateReqVO) {
        //1. 校验存在
        ErpPurchaseRequestDO erpPurchaseRequestDO = validatePurchaseRequestExists(updateReqVO.getId());
        //1.1 判断已审核
        ThrowUtil.ifThrow(erpPurchaseRequestDO.getStatus().equals(ErpAuditStatus.APPROVE.getStatus()), PURCHASE_REQUEST_UPDATE_FAIL_APPROVE, erpPurchaseRequestDO.getNo());
        //1.2 判断已关闭
        ThrowUtil.ifThrow(erpPurchaseRequestDO.getOffStatus().equals(ErpAuditStatus.CLOSED.getStatus()), PURCHASE_REQUEST_CLOSED, erpPurchaseRequestDO.getNo());
        //1.3 判断已手动关闭
        ThrowUtil.ifThrow(erpPurchaseRequestDO.getOffStatus().equals(ErpAuditStatus.MANUAL_CLOSED.getStatus()), PURCHASE_REQUEST_MANUAL_CLOSED, erpPurchaseRequestDO.getNo());
        //1.4 校验子单requestId是否关联主单的id
        List<ErpPurchaseRequestItemsDO> requestItemsDOS = erpPurchaseRequestItemsMapper.findItemsGroupedByRequestId(List.of(updateReqVO.getId())).get(updateReqVO.getId());
        for (@Valid ErpPurchaseRequestItemsSaveReqVO itemsSaveReqVO : updateReqVO.getItems()) {
            // 判断子单requestId是否关联主单的id，抛出异常
            ThrowUtil.ifThrow(requestItemsDOS.stream().noneMatch(itemDO -> itemDO.getId().equals(itemsSaveReqVO.getId())), PURCHASE_REQUEST_UPDATE_FAIL_REQUEST_ID, itemsSaveReqVO.getId());
        }
        //1.5 校验产品是否存在
        List<ErpPurchaseRequestItemsDO> itemsDOList = validatePurchaseRequestItems(updateReqVO.getItems());
        // 2.1 更新订单
        ErpPurchaseRequestDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseRequestDO.class);
        erpPurchaseRequestMapper.updateById(updateObj);
        // 2.2 更新订单项
        updatePurchaseRequestItemList(updateReqVO.getId(), itemsDOList);
    }

    private void updatePurchaseRequestItemList(Long id, List<ErpPurchaseRequestItemsDO> itemsDOList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpPurchaseRequestItemsDO> oldList = erpPurchaseRequestItemsMapper.selectListByRequestId(id);
        // id 不同，就认为是不同的记录
        List<List<ErpPurchaseRequestItemsDO>> diffList = diffList(oldList, itemsDOList,
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));
        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setRequestId(id));
            erpPurchaseRequestItemsMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpPurchaseRequestItemsMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpPurchaseRequestItemsMapper.deleteByIds(convertList(diffList.get(2), ErpPurchaseRequestItemsDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseRequestStatus(Long id, Integer status) {
        boolean approve = ErpAuditStatus.APPROVE.getStatus().equals(status);
        // 1.1 校验采购订单id存在
        ErpPurchaseRequestDO erpPurchaseRequest = validatePurchaseRequestExists(id);
        // 1.2 校验状态
        ThrowUtil.ifThrow(erpPurchaseRequest.getStatus().equals(status), approve ? PURCHASE_REQUEST_APPROVE_FAIL : PURCHASE_REQUEST_PROCESS_FAIL);
        // 2. 更新状态
        int updateCount = erpPurchaseRequestMapper.updateByIdAndStatus(id, erpPurchaseRequest.getStatus(),
            new ErpPurchaseRequestDO().setStatus(status));
        ThrowUtil.ifThrow(updateCount <= 0, approve ? PURCHASE_REQUEST_APPROVE_FAIL : PURCHASE_REQUEST_PROCESS_FAIL);
    }

    @Override
    public List<ErpPurchaseRequestItemsDO> getPurchaseRequestItemListByOrderIds(Collection<Long> requestIds) {
        if (CollUtil.isEmpty(requestIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseRequestItemsMapper.selectListByRequestIds(requestIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseRequest(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpPurchaseRequestDO> purchaseRequestDOs = erpPurchaseRequestMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(purchaseRequestDOs)) {
            return;
        }
        purchaseRequestDOs.forEach(erpPurchaseRequestDO -> {
            ThrowUtil.ifThrow(!erpPurchaseRequestDO.getStatus().equals(ErpAuditStatus.APPROVE.getStatus()), PURCHASE_REQUEST_DELETE_FAIL_APPROVE, erpPurchaseRequestDO.getNo());
        });

        // 2. 遍历删除，并记录操作日志
        purchaseRequestDOs.forEach(erpPurchaseRequest -> {
            //获取主表id
            Long id = erpPurchaseRequest.getId();
            // 2.1 删除订单
            erpPurchaseRequestMapper.deleteById(id);
            // 2.2 删除订单项
            erpPurchaseRequestItemsMapper.deleteByRequestId(id);

        });
    }

    @Override
    public ErpPurchaseRequestDO validatePurchaseRequestExists(Long id) {
        ErpPurchaseRequestDO erpPurchaseRequestDO = erpPurchaseRequestMapper.selectById(id);
        ThrowUtil.ifThrow(erpPurchaseRequestDO == null, PURCHASE_REQUEST_NOT_EXISTS);
        return erpPurchaseRequestDO;
    }


    @Override
    public ErpPurchaseRequestDO getPurchaseRequest(Long id) {
        return erpPurchaseRequestMapper.selectById(id);
    }

    @Override
    public PageResult<ErpPurchaseRequestDO> getPurchaseRequestPage(ErpPurchaseRequestPageReqVO pageReqVO) {
        return erpPurchaseRequestMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpPurchaseRequestItemsDO> getPurchaseRequestItemListByOrderId(Long requestId) {
        return erpPurchaseRequestItemsMapper.selectListByRequestId(requestId);
    }

}