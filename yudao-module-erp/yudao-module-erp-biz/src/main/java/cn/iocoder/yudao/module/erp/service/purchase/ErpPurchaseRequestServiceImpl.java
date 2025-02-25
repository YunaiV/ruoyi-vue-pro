package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.erp.config.PurchaseRequestStateMachine;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestAuditStatusReq;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestItemsSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.req.ErpPurchaseRequestSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.ErpOffStatus;
import cn.iocoder.yudao.module.erp.enums.ErpOrderStatus;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.service.stock.ErpWarehouseService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import java.util.Map;
import java.util.stream.Collectors;

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
        //初始状态设置，审核状态、关闭状态、订购状态
        purchaseRequest.setStatus(ErpAuditStatus.PROCESS.getCode())
            .setOffStatus(ErpOffStatus.OPEN.getCode())
            .setOrderStatus(ErpOrderStatus.OT_ORDERED.getCode());

        //2. 插入主表的申请单数据
        ThrowUtil.ifThrow(erpPurchaseRequestMapper.insert(purchaseRequest) <= 0, PURCHASE_REQUEST_ADD_FAIL_APPROVE);
        Long id = purchaseRequest.getId();
        itemsDOList.forEach(i -> i.setRequestId(id));
        //设置子表初始状态
        itemsDOList.forEach(i -> i.setOffStatus(ErpOffStatus.OPEN.getCode())
            .setOrderStatus(ErpAuditStatus.PROCESS.getCode()));
        //3. 批量插入子表数据
        ThrowUtil.ifThrow(!erpPurchaseRequestItemsMapper.insertBatch(itemsDOList), PURCHASE_REQUEST_ADD_FAIL_PRODUCT);
        // 返回单据id
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
        ThrowUtil.ifThrow(requestDO.getStatus().equals(ErpAuditStatus.APPROVE.getCode()), PURCHASE_REQUEST_UPDATE_FAIL_APPROVE, requestDO.getNo());
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
    public void updatePurchaseRequestStatus(Long id, Integer newAuditStatus, Integer newOrderStatus, Integer newOffStatus) {
        // 1. 校验采购申请单是否存在
        ErpPurchaseRequestDO requestDO = validatePurchaseRequestExists(id);

        // 2. 校验审核状态是否合法
        if (newAuditStatus != null) {
            boolean isAuditStatusValid = PurchaseRequestStateMachine.canTransitionAudit(
                ErpAuditStatus.fromCode(requestDO.getStatus()),
                ErpAuditStatus.fromCode(newAuditStatus)
            );
            ThrowUtil.ifThrow(!isAuditStatusValid, PURCHASE_REQUEST_UPDATE_FAIL_AUDIT_STATUS,
                ErpAuditStatus.getDescriptionByCode(requestDO.getStatus()),
                ErpAuditStatus.getDescriptionByCode(newAuditStatus));
        }
        // 3. 校验采购状态是否合法
        if (newOrderStatus != null) {
            boolean isOrderStatusValid = PurchaseRequestStateMachine.canTransitionOrder(
                ErpOrderStatus.fromCode(requestDO.getOrderStatus()),
                ErpOrderStatus.fromCode(newOrderStatus)
            );
            ThrowUtil.ifThrow(!isOrderStatusValid, PURCHASE_REQUEST_UPDATE_FAIL_ORDER_STATUS,
                ErpOrderStatus.getDescriptionByCode(requestDO.getOrderStatus()),
                ErpOrderStatus.getDescriptionByCode(newOrderStatus));
        }
        // 4. 校验关闭状态是否合法
        if (newOffStatus != null) {
            boolean isOffStatusValid = PurchaseRequestStateMachine.canTransitionOff(
                ErpOffStatus.fromCode(requestDO.getOffStatus()),
                ErpOffStatus.fromCode(newOffStatus)
            );
            ThrowUtil.ifThrow(!isOffStatusValid, PURCHASE_REQUEST_UPDATE_FAIL_OFF_STATUS,
                ErpOffStatus.getDescriptionByCode(requestDO.getOffStatus()),
                ErpOffStatus.getDescriptionByCode(newOffStatus));
        }
        // 7. 更新采购申请单状态
        ErpPurchaseRequestDO updateRequest = new ErpPurchaseRequestDO();
        if (newAuditStatus != null) {
            updateRequest.setStatus(newAuditStatus);
            updateRequest.setAuditorId(SecurityFrameworkUtils.getLoginUserId());
            updateRequest.setAuditTime(LocalDateTime.now());
        }
        if (newOrderStatus != null) {
            updateRequest.setOrderStatus(newOrderStatus);
        }
        if (newOffStatus != null) {
            updateRequest.setOffStatus(newOffStatus);
        }
        // 8. 更新数据库
        int i = erpPurchaseRequestMapper.update(updateRequest, new LambdaUpdateWrapper<ErpPurchaseRequestDO>().eq(ErpPurchaseRequestDO::getId, id));
        ThrowUtil.ifThrow(i == 0, PURCHASE_REQUEST_UPDATE_FAIL, requestDO.getNo());
    }

    /**
     * 更新采购申请单子项状态(审核状态+关闭状态)
     *
     * @param itemIds     子表id
     * @param orderStatus 采购状态
     * @param offStatus   关闭状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItemStatus(List<Long> itemIds, Integer orderStatus, Integer offStatus) {
        // 1. 校验子项是否存在
        List<ErpPurchaseRequestItemsDO> items = erpPurchaseRequestItemsMapper.selectBatchIds(itemIds);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("采购申请单子项不存在");
        }
        // 2. 校验采购状态是否合法
        if (orderStatus != null) {
            for (ErpPurchaseRequestItemsDO item : items) {
                boolean isAuditStatusValid = PurchaseRequestStateMachine.canTransitionOrder(
                    ErpOrderStatus.fromCode(item.getOrderStatus()),
                    ErpOrderStatus.fromCode(orderStatus)
                );
                ThrowUtil.ifThrow(!isAuditStatusValid, PURCHASE_REQUEST_UPDATE_FAIL_AUDIT_STATUS,
                    ErpOrderStatus.getDescriptionByCode(item.getOrderStatus()),
                    ErpOrderStatus.getDescriptionByCode(orderStatus));
            }
        }

        // 3. 校验关闭状态是否合法
        if (offStatus != null) {
            for (ErpPurchaseRequestItemsDO item : items) {
                boolean isOffStatusValid = PurchaseRequestStateMachine.canTransitionOff(
                    ErpOffStatus.fromCode(item.getOffStatus()),
                    ErpOffStatus.fromCode(offStatus)
                );
                ThrowUtil.ifThrow(!isOffStatusValid, PURCHASE_REQUEST_UPDATE_FAIL_OFF_STATUS,
                    ErpOffStatus.getDescriptionByCode(item.getOffStatus()),
                    ErpOffStatus.getDescriptionByCode(offStatus));
            }
        }
        // 4. 更新子项状态
        for (ErpPurchaseRequestItemsDO item : items) {
            // 更新审核状态
//            if (auditStatus != null) {
//                item.setStatus(auditStatus);
//            }
            // 更新关闭状态
            if (offStatus != null) {
                item.setOffStatus(offStatus);
            }
        }
        // 5. 批量更新子项状态
        Boolean b = erpPurchaseRequestItemsMapper.updateBatch(items);
        ThrowUtil.ifThrow(!b, PURCHASE_REQUEST_UPDATE_FAIL, itemIds.toString());
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
     *
     */
    @Override
    public void reviewPurchaseOrder(ErpPurchaseRequestAuditStatusReq req) {
        // 查询所有相关的 items
        ErpPurchaseRequestDO requestDO = erpPurchaseRequestMapper.selectById(req.getRequestId());
        List<ErpPurchaseRequestItemsDO> itemsDOList = erpPurchaseRequestItemsMapper.selectListByRequestIds(Collections.singleton(req.getRequestId()));

        // 判断是否是审核操作
        if (Boolean.TRUE.equals(req.getReviewed())) {
            //排除已审核
            ThrowUtil.ifThrow(ErpAuditStatus.PROCESS.getCode().equals(requestDO.getStatus()), PURCHASE_REQUEST_APPROVE_FAIL);
            // 审核操作
            approvePurchaseOrder(req, itemsDOList);
        } else {
            //非审核->异常
            ThrowUtil.ifThrow(!ErpAuditStatus.APPROVE.getCode().equals(requestDO.getStatus()), PURCHASE_REQUEST_PROCESS_FAIL);
            //非关闭->异常
            ThrowUtil.ifThrow(!ErpOffStatus.CLOSED.getCode().equals(requestDO.getOffStatus()), PURCHASE_REQUEST_PROCESS_FAIL_CLOSE);
            //已订购+部分订购->异常
            ThrowUtil.ifThrow(ErpOrderStatus.PARTIALLY_ORDERED.getCode().equals(requestDO.getOrderStatus()) ||
                !ErpOrderStatus.ORDERED.getCode().equals(requestDO.getOrderStatus()
                ), PURCHASE_REQUEST_PROCESS_FAIL_ORDERED);
            // 反审核操作
            reverseApprovePurchaseOrder(itemsDOList, req);
        }
    }

    /**
     * 审核采购订单并更新批准数量
     */
    private void approvePurchaseOrder(ErpPurchaseRequestAuditStatusReq req, List<ErpPurchaseRequestItemsDO> itemsDOList) {
        // 更新采购申请单状态为审核通过
        erpPurchaseRequestService.updatePurchaseRequestStatus(req.getRequestId(), ErpAuditStatus.APPROVE.getCode(), null, null);

        // 创建一个 Map 来根据 itemId 快速查找对应的审核项
        Map<Long, ErpPurchaseRequestAuditStatusReq.requestItems> itemMap = req.getItems().stream()
            .collect(Collectors.toMap(ErpPurchaseRequestAuditStatusReq.requestItems::getId, item -> item));

        // 设置批准数量
        itemsDOList.forEach(itemDO -> {
            ErpPurchaseRequestAuditStatusReq.requestItems item = itemMap.get(itemDO.getId());
            if (item != null) {
                itemDO.setApproveCount(item.getApproveCount());
            }
        });
    }

    /**
     * 反审核操作，将批准数量置为 null
     */
    private void reverseApprovePurchaseOrder(List<ErpPurchaseRequestItemsDO> itemsDOList, ErpPurchaseRequestAuditStatusReq req) {
        // 更新采购申请单状态为反审核
        erpPurchaseRequestService.updatePurchaseRequestStatus(req.getRequestId(), ErpAuditStatus.REVERSED.getCode(), null, null);

        // 设置所有申请单的批准数量为 null
        itemsDOList.forEach(itemDO -> itemDO.setApproveCount(null));
    }


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
        if (Boolean.TRUE.equals(enable)) {
            // 处理启用状态
            //更新子表
            erpPurchaseRequestService.updateItemStatus(itemIds, null, ErpOffStatus.OPEN.getCode());
            //一个子表开->主表开
            if (erpPurchaseRequestMapper.selectCount(new LambdaQueryWrapper<ErpPurchaseRequestDO>()
                .eq(ErpPurchaseRequestDO::getId, requestId)
                .eq(ErpPurchaseRequestDO::getOffStatus, ErpOffStatus.OPEN.getCode())) == 0) {//主表不处于开启状态
                // 主表状态开启
                log.debug("主表状态开启");
                erpPurchaseRequestService.updatePurchaseRequestStatus(requestId, null, null, ErpOffStatus.OPEN.getCode());
                }
        } else {
            // 处理关闭状态
            //未审核->异常
            ThrowUtil.ifThrow(!ErpAuditStatus.APPROVE.getCode().equals(erpPurchaseRequestMapper.selectById(requestId).getStatus()), PURCHASE_REQUEST_CLOSE_FAIL);
            //处理子表状态
            erpPurchaseRequestService.updateItemStatus(itemIds, null, ErpOffStatus.MANUAL_CLOSED.getCode());
            //子表都关->主表也关
            if (erpPurchaseRequestItemsMapper.selectCount(new LambdaQueryWrapper<ErpPurchaseRequestItemsDO>()
                .eq(ErpPurchaseRequestItemsDO::getRequestId, requestId)
                .eq(ErpPurchaseRequestItemsDO::getOffStatus, ErpOffStatus.OPEN.getCode())) == 0) {
                // 主表状态关闭
                erpPurchaseRequestService.updatePurchaseRequestStatus(requestId, null, null, ErpOffStatus.MANUAL_CLOSED.getCode());
            }
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
            ThrowUtil.ifThrow(!erpPurchaseRequestDO.getStatus().equals(ErpAuditStatus.APPROVE.getCode()), PURCHASE_REQUEST_DELETE_FAIL_APPROVE, erpPurchaseRequestDO.getNo());
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