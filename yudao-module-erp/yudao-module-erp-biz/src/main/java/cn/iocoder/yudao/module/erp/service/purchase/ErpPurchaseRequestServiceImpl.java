package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestAuditStatusReq;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestItemsSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpEventEnum;
import cn.iocoder.yudao.module.erp.enums.status.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpOrderStatus;
import cn.iocoder.yudao.module.erp.enums.status.ErpStorageStatus;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.erp.enums.ErpStateMachines.*;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP采购申请单 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ErpPurchaseRequestServiceImpl implements ErpPurchaseRequestService {
    private final ErpPurchaseRequestMapper erpPurchaseRequestMapper;
    private final ErpPurchaseRequestItemsMapper erpPurchaseRequestItemsMapper;
    private final ErpWarehouseService erpWarehouseService;
    private final ErpProductService productService;
    private final ErpNoRedisDAO noRedisDAO;
    private final DeptApi deptApi;
    private final AdminUserApi adminUserApi;
    @Lazy
    @Autowired
    private ErpPurchaseRequestServiceImpl erpPurchaseRequestService;
    @Resource(name = PURCHASE_REQUEST_STATE_MACHINE_NAME)
    StateMachine<ErpAuditStatus, ErpEventEnum, ErpPurchaseRequestDO> auditMachine;
    @Resource(name = PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestDO> offMachine;
    @Resource(name = PURCHASE_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, ErpEventEnum, ErpPurchaseRequestDO> orderMachine;

    @Resource(name = PURCHASE_ORDER_ITEM_STATE_MACHINE_NAME)
    StateMachine<ErpStorageStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> orderItemMachine;
    @Resource(name = PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, ErpEventEnum, ErpPurchaseRequestItemsDO> offItemMachine;

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
        deptApi.validateDeptList(Collections.singleton(createReqVO.getApplicationDeptId()));
        //1.4 校验申请人合法
        adminUserApi.validateUser(createReqVO.getApplicantId());
        //初始化
        ErpPurchaseRequestDO purchaseRequest = BeanUtils.toBean(createReqVO, ErpPurchaseRequestDO.class);
        purchaseRequest.setNo(no);
        //2. 插入主表的申请单数据
        ThrowUtil.ifThrow(erpPurchaseRequestMapper.insert(purchaseRequest) <= 0, PURCHASE_REQUEST_ADD_FAIL_APPROVE);
        Long id = purchaseRequest.getId();
        itemsDOList.forEach(i -> i.setRequestId(id));
        //3. 批量插入子表数据
        ThrowUtil.ifThrow(!erpPurchaseRequestItemsMapper.insertBatch(itemsDOList), PURCHASE_REQUEST_ADD_FAIL_PRODUCT);
        //4.初始化-审核状态-开关-订购
        auditMachine.fireEvent(ErpAuditStatus.DRAFT, ErpEventEnum.INIT, purchaseRequest);
        offMachine.fireEvent(ErpOffStatus.OPEN, ErpEventEnum.OFF_INIT, purchaseRequest);
        orderMachine.fireEvent(ErpOrderStatus.OT_ORDERED, ErpEventEnum.ORDER_INIT, purchaseRequest);
        //子表初始化
        itemsDOList.forEach(i -> {
                orderItemMachine.fireEvent(ErpStorageStatus.NONE_IN_STORAGE, ErpEventEnum.ORDER_INIT, i);
                offItemMachine.fireEvent(ErpOffStatus.OPEN, ErpEventEnum.OFF_INIT, i);
            }
        );
        return id;
    }

    /**
     * 校验采购订单的子项目是否合法
     *
     * @param items 采购订单子项目集合
     *              1、校验产品有效性 2、校验仓库有效性
     */
    @Override
    public List<ErpPurchaseRequestItemsDO> validatePurchaseRequestItems(List<ErpPurchaseRequestItemsSaveReqVO> items) {
        // 1. 校验产品有效性
        productService.validProductList(convertSet(items, ErpPurchaseRequestItemsSaveReqVO::getProductId));
        // 1.1 校验仓库有效性
        erpWarehouseService.validWarehouseList(convertSet(items, ErpPurchaseRequestItemsSaveReqVO::getWarehouseId));
        // 2. 转化为 ErpPurchaseRequestItemsDO 列表
        return convertList(items, o -> BeanUtils.toBean(o, ErpPurchaseRequestItemsDO.class));
    }

    /**
     * 校验采购订单的子项表id否关联主表
     *
     * @param masterId 主表id-申请单
     * @param ids      itemIds 子表id集合-申请项
     */
    @Override
    public void validatePurchaseRequestItemsMasterId(Long masterId, List<Long> ids) {
        // 验证ids在数据库中是否存在
        ThrowUtil.ifThrow(erpPurchaseRequestItemsMapper.selectListByIds(ids).stream().noneMatch(i -> ids.contains(i.getId())), PURCHASE_REQUEST_ITEM_NOT_EXISTS, ids);
        // 校验子单requestId是否关联主单的id
        ThrowUtil.ifThrow(erpPurchaseRequestItemsMapper.selectListByRequestId(masterId).stream().noneMatch(i -> ids.contains(i.getId())), PURCHASE_REQUEST_UPDATE_FAIL_REQUEST_ID, masterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseRequest(ErpPurchaseRequestSaveReqVO updateReqVO) {
        //1 校验
        ErpPurchaseRequestDO erpPurchaseRequestDO = validatePurchaseRequestExists(updateReqVO.getId());
        //1.1 校验items是否存在
        List<ErpPurchaseRequestItemsDO> itemsDOList = validatePurchaseRequestItems(updateReqVO.getItems());
        //1.2 状态校验
        updateStatusCheck(erpPurchaseRequestDO, updateReqVO.getItems().stream().map(ErpPurchaseRequestItemsSaveReqVO::getId).toList());
        //1.4 校验主子表关联
        List<ErpPurchaseRequestItemsDO> requestItemsDOS = erpPurchaseRequestItemsMapper.findItemsGroupedByRequestId(List.of(updateReqVO.getId())).get(updateReqVO.getId());
        List<Long> requestItemsIds = convertList(requestItemsDOS, ErpPurchaseRequestItemsDO::getId);
        validatePurchaseRequestItemsMasterId(updateReqVO.getId(), requestItemsIds);
        // 2 更新
        // 2.2 更新主表
        ErpPurchaseRequestDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseRequestDO.class);
        erpPurchaseRequestMapper.updateById(updateObj);
        // 2.3 更新子表
        updatePurchaseRequestItemList(updateReqVO.getId(), itemsDOList);
    }

    //判断当前状态是否可以更新
    private void updateStatusCheck(ErpPurchaseRequestDO requestDO, List<Long> itemIds) {
        //1.1 判断已审核
        ThrowUtil.ifThrow(requestDO.getStatus().equals(ErpAuditStatus.APPROVED.getCode()), PURCHASE_REQUEST_UPDATE_FAIL_APPROVE, requestDO.getNo());
        //1.2 判断已关闭
        ThrowUtil.ifThrow(requestDO.getOffStatus().equals(ErpOffStatus.CLOSED.getCode()), PURCHASE_REQUEST_CLOSED, requestDO.getNo());
        //1.3 判断已手动关闭
        ThrowUtil.ifThrow(requestDO.getOffStatus().equals(ErpOffStatus.MANUAL_CLOSED.getCode()), PURCHASE_REQUEST_MANUAL_CLOSED, requestDO.getNo());
        //2 判断子表已关闭？手动关闭？
        List<ErpPurchaseRequestItemsDO> itemsDOList = erpPurchaseRequestItemsMapper.selectListByIds(itemIds);
        for (ErpPurchaseRequestItemsDO itemsDO : itemsDOList) {
            // 2.1 判断子表已关闭
            ThrowUtil.ifThrow(itemsDO.getOffStatus().equals(ErpOffStatus.CLOSED.getCode()), PURCHASE_REQUEST_ITEM_CLOSED, itemsDO.getId());
            // 2.2 判断子表已手动关闭
            ThrowUtil.ifThrow(itemsDO.getOffStatus().equals(ErpOffStatus.MANUAL_CLOSED.getCode()), PURCHASE_REQUEST_ITEM_MANUAL_CLOSED, itemsDO.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseRequestItemList(Long id, List<ErpPurchaseRequestItemsDO> itemsDOList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpPurchaseRequestItemsDO> oldList = erpPurchaseRequestItemsMapper.selectListByRequestId(id);
        // id 不同，就认为是不同的记录
        List<List<ErpPurchaseRequestItemsDO>> diffList = diffList(oldList, itemsDOList,
            (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));
        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setRequestId(id));
            erpPurchaseRequestItemsMapper.insertBatch(diffList.get(0));
            for (ErpPurchaseRequestItemsDO itemsDO : diffList.get(0)) {
                //初始化状态
                orderItemMachine.fireEvent(ErpStorageStatus.NONE_IN_STORAGE, ErpEventEnum.INIT_STORAGE, itemsDO);
                offItemMachine.fireEvent(ErpOffStatus.OPEN, ErpEventEnum.OFF_INIT, itemsDO);
            }
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpPurchaseRequestItemsMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpPurchaseRequestItemsMapper.deleteByIds(convertList(diffList.get(2), ErpPurchaseRequestItemsDO::getId));
            //触发关闭状态
            for (ErpPurchaseRequestItemsDO itemsDO : diffList.get(2)) {
                offItemMachine.fireEvent(ErpOffStatus.fromCode(itemsDO.getOffStatus()), ErpEventEnum.MANUAL_CLOSE, itemsDO);
            }
        }
    }

    @Override
    public List<ErpPurchaseRequestItemsDO> getPurchaseRequestItemListByOrderIds(Collection<Long> requestIds) {
        if (CollUtil.isEmpty(requestIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseRequestItemsMapper.selectListByRequestIds(requestIds);
    }

    /**
     * 审核/反审核采购订单
     * 该方法用于根据传入的请求参数对采购订单进行审核或反审核操作。
     */
    @Override
    public void reviewPurchaseOrder(ErpPurchaseRequestAuditStatusReq req) {
        // 查询所有相关的 items
        ErpPurchaseRequestDO requestDO = erpPurchaseRequestMapper.selectById(req.getRequestId());
        // 判断是否是审核操作
        if (Boolean.TRUE.equals(req.getReviewed())) {
            // 审核操作
            auditMachine.fireEvent( ErpAuditStatus.fromCode(requestDO.getStatus()),ErpEventEnum.AGREE,requestDO);
        } else {
            // 反审核操作
            auditMachine.fireEvent( ErpAuditStatus.fromCode(requestDO.getStatus()),ErpEventEnum.WITHDRAW_REVIEW,requestDO);
        }
    }

//    /**
//     * 审核采购订单并更新批准数量
//     */
//    private void approvePurchaseOrder(ErpPurchaseRequestAuditStatusReq req, List<ErpPurchaseRequestItemsDO> itemsDOList) {
//        // 更新采购申请单状态为审核通过
//        erpPurchaseRequestService.updatePurchaseRequestStatus(req.getRequestId(), ErpAuditStatus.APPROVED.getCode(), null, null);
//
//        // 创建一个 Map 来根据 itemId 快速查找对应的审核项
//        Map<Long, ErpPurchaseRequestAuditStatusReq.requestItems> itemMap = req.getItems().stream()
//            .collect(Collectors.toMap(ErpPurchaseRequestAuditStatusReq.requestItems::getId, item -> item));
//
//        // 设置批准数量
//        itemsDOList.forEach(itemDO -> {
//            ErpPurchaseRequestAuditStatusReq.requestItems item = itemMap.get(itemDO.getId());
//            if (item != null) {
//                itemDO.setApproveCount(item.getApproveCount());
//            }
//        });
//    }

    /**
     * 关闭/启用采购申请单子项状态
     *
     * @param requestId 采购订单id
     * @param itemIds   采购订单子项id集合
     * @param enable    是否开启（true 表示开启，false 表示关闭）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchPurchaseOrderStatus(Long requestId, List<Long> itemIds, Boolean enable) {
        // 根据 enable 参数判断是开启还是关闭状态
        List<ErpPurchaseRequestItemsDO> itemsDOList = erpPurchaseRequestItemsMapper.selectBatchIds(itemIds);
        if (Boolean.TRUE.equals(enable)) {
            itemsDOList.forEach(itemsDO ->
                offItemMachine.fireEvent(ErpOffStatus.fromCode(itemsDO.getOffStatus()),
                ErpEventEnum.ACTIVATE, itemsDO));
        } else {
            // 处理关闭状态
            itemsDOList.forEach(itemsDO ->
                offItemMachine.fireEvent(ErpOffStatus.fromCode(itemsDO.getOffStatus()),
                ErpEventEnum.MANUAL_CLOSE, itemsDO));
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseRequest(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpPurchaseRequestDO> purchaseRequestDOs = erpPurchaseRequestMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(purchaseRequestDOs)) {
            return;
        }
        // 1.1 非已审核->异常
        purchaseRequestDOs.forEach(erpPurchaseRequestDO -> {
            ThrowUtil.ifThrow(!erpPurchaseRequestDO.getStatus().equals(ErpAuditStatus.APPROVED.getCode()), PURCHASE_REQUEST_DELETE_FAIL_APPROVE, erpPurchaseRequestDO.getNo());
        });
        // 2. 遍历删除，并记录操作日志
        purchaseRequestDOs.forEach(erpPurchaseRequest -> {
            //获取主表id
            Long id = erpPurchaseRequest.getId();
            erpPurchaseRequestMapper.deleteById(id);
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