package cn.iocoder.yudao.module.srm.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.ErpProductUnitApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.api.stock.WmsWarehouseApi;
import cn.iocoder.yudao.module.srm.api.purchase.SrmInCountDTO;
import cn.iocoder.yudao.module.srm.api.purchase.TmsOrderCountDTO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.ErpPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.*;
import cn.iocoder.yudao.module.srm.convert.purchase.ErpOrderConvert;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.ErpOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.ErpOrderStatus;
import cn.iocoder.yudao.module.srm.enums.status.ErpStorageStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.alibaba.cola.statemachine.StateMachine;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.*;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;

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
    private final ErpPurchaseOrderItemMapper erpPurchaseOrderItemMapper;
    private final ErpPurchaseOrderService erpPurchaseOrderService;
    private final SrmNoRedisDAO noRedisDAO;
    private final WmsWarehouseApi wmsWarehouseApi;
    private final ErpProductApi erpProductApi;
    private final DeptApi deptApi;
    private final AdminUserApi adminUserApi;

    @Resource(name = PURCHASE_REQUEST_AUDIT_STATE_MACHINE_NAME)
    StateMachine<SrmAuditStatus, SrmEventEnum, ErpPurchaseRequestAuditReqVO> auditMachine;
    @Resource(name = PURCHASE_REQUEST_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestDO> offMachine;
    @Resource(name = PURCHASE_REQUEST_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, SrmEventEnum, ErpPurchaseRequestDO> orderMachine;

    @Resource(name = PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME)
    StateMachine<ErpOffStatus, SrmEventEnum, ErpPurchaseRequestItemsDO> offItemMachine;
    @Resource(name = PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME)
    StateMachine<ErpOrderStatus, SrmEventEnum, TmsOrderCountDTO> orderItemMachine;
    @Resource(name = PURCHASE_REQUEST_STORAGE_STATE_MACHINE_NAME)
    StateMachine<ErpStorageStatus, SrmEventEnum, ErpPurchaseRequestDO> storageMachine;
    @Resource(name = PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME)
    StateMachine<ErpStorageStatus, SrmEventEnum, SrmInCountDTO> storageItemMachine;
    @Autowired
    private ErpProductUnitApi erpProductUnitApi;

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
        String no = noRedisDAO.generate(SrmNoRedisDAO.PURCHASE_REQUEST_NO_PREFIX, PURCHASE_REQUEST_NO_OUT_OF_BOUNDS);
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
        ThrowUtil.ifSqlThrow(erpPurchaseRequestMapper.insert(purchaseRequest), PURCHASE_REQUEST_ADD_FAIL_APPROVE);
        Long id = purchaseRequest.getId();
        itemsDOList.forEach(i -> i.setRequestId(id));
        //3. 批量插入子表数据
        ThrowUtil.ifThrow(!erpPurchaseRequestItemsMapper.insertBatch(itemsDOList), PURCHASE_REQUEST_ADD_FAIL_PRODUCT);
        //5.初始化状态-主子表
        initMasterStatus(purchaseRequest);
        List<ErpPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectListByRequestId(purchaseRequest.getId());
        initSlaveStatus(itemsDOS);
        return id;
    }

    private void initSlaveStatus(List<ErpPurchaseRequestItemsDO> itemsDOS) {
        itemsDOS.forEach(i -> {
            orderItemMachine.fireEvent(ErpOrderStatus.OT_ORDERED, SrmEventEnum.ORDER_INIT, TmsOrderCountDTO.builder().purchaseOrderItemId(i.getId()).build());
            offItemMachine.fireEvent(ErpOffStatus.OPEN, SrmEventEnum.OFF_INIT, i);
            //入库
            storageItemMachine.fireEvent(ErpStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, SrmInCountDTO.builder().applyItemId(i.getId()).build());
        });
    }

    private void initMasterStatus(ErpPurchaseRequestDO purchaseRequest) {

        auditMachine.fireEvent(SrmAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT, ErpPurchaseRequestAuditReqVO.builder().requestId(purchaseRequest.getId()).build());
        offMachine.fireEvent(ErpOffStatus.OPEN, SrmEventEnum.OFF_INIT, purchaseRequest);
        orderMachine.fireEvent(ErpOrderStatus.OT_ORDERED, SrmEventEnum.ORDER_INIT, purchaseRequest);
        //入库
        storageMachine.fireEvent(ErpStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, purchaseRequest);
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
        List<ErpProductDTO> dtoList = erpProductApi.validProductList(convertSet(items, ErpPurchaseRequestItemsSaveReqVO::getProductId));
        Map<Long, ErpProductDTO> dtoMap = erpProductApi.getProductMap(convertSet(dtoList, ErpProductDTO::getId));
        // 1.1 校验仓库有效性
        wmsWarehouseApi.validWarehouseList(convertSet(items, ErpPurchaseRequestItemsSaveReqVO::getWarehouseId));
        // 1.2 把产品转化为sku+报关品名
        // 2. 转化为 ErpPurchaseRequestItemsDO 列表
        return convertList(items, o -> BeanUtils.toBean(o, ErpPurchaseRequestItemsDO.class, item -> {
            //产品名称
            item.setProductName(dtoMap.get(item.getProductId()).getName());
            item.setBarCode(dtoMap.get(item.getProductId()).getBarCode());
            //产品单位名称(产品必有单位)
            item.setProductUnitName(erpProductUnitApi.getProductUnitList(Collections.singleton(dtoMap.get(item.getProductId()).getUnitId())).get(0).getName());
        }));
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
    public Long merge(ErpPurchaseRequestMergeReqVO reqVO) {
        // 获取所有 itemIds
        List<Long> itemIds = reqVO.getItems().stream().map(ErpPurchaseRequestMergeReqVO.requestItems::getId).collect(Collectors.toList());

        // 查询 itemDOS 并校验
        List<ErpPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectListByIds(itemIds);
        ThrowUtil.ifThrow(itemsDOS.isEmpty(), PURCHASE_REQUEST_ITEM_NOT_EXISTS, itemIds);

        //申请单已审核+未关闭才可以合并
        validMerge(itemsDOS);
        // 创建 itemsMap 和 requestItemDOMap
        Map<Long, ErpPurchaseRequestMergeReqVO.requestItems> itemsMap = reqVO.getItems().stream().collect(Collectors.toMap(ErpPurchaseRequestMergeReqVO.requestItems::getId, Function.identity()));
        Map<Long, ErpPurchaseRequestItemsDO> requestItemDOMap = itemsDOS.stream().collect(Collectors.toMap(ErpPurchaseRequestItemsDO::getId, Function.identity()));

        // ErpPurchaseRequestItemsDO item -> 订单vo item
        List<ErpPurchaseOrderSaveReqVO.Item> voItemList = ErpOrderConvert.INSTANCE.convertToErpPurchaseOrderSaveReqVOItemList(itemsDOS, itemsMap, requestItemDOMap);
        // 获取申请单编号映射
        Set<Long> requestIds = itemsDOS.stream().map(ErpPurchaseRequestItemsDO::getRequestId).collect(Collectors.toSet());
        Map<Long, ErpPurchaseRequestDO> rDOMap = erpPurchaseRequestMapper.selectByIds(requestIds).stream().collect(Collectors.toMap(ErpPurchaseRequestDO::getId, Function.identity()));

        //后置渲染，申请单的no编号
        voItemList.forEach(item -> {
            Long itemId = item.getPurchaseApplyItemId();
            ErpPurchaseRequestItemsDO itemDO = requestItemDOMap.get(itemId);
            item.setId(null);
            item.setPurchaseApplyItemId(itemId);//采购申请项id
            //获得requestDO
            ErpPurchaseRequestDO aDo = rDOMap.get(itemDO.getRequestId());
            item.setErpPurchaseRequestItemNo(aDo.getNo());
            item.setApplicantId(aDo.getApplicantId());//申请人
            item.setApplicationDeptId(aDo.getApplicationDeptId());//申请部门
            item.setCurrencyName(reqVO.getCurrencyName());//订单币别名称
        });

        //2.0 构造VO入参类
        ErpPurchaseOrderSaveReqVO saveReqVO = BeanUtils.toBean(reqVO, ErpPurchaseOrderSaveReqVO.class, vo -> {
            vo.setId(null);
            vo.setNoTime(LocalDateTime.now());
            vo.setItems(voItemList);
//            vo.setRemark("申请人期望采购日期: " + DateUtil.format(reqVO.getOrderTime(), DatePattern.CHINESE_DATE_PATTERN));
        });
        //3.0 持久化
        Long orderId = erpPurchaseOrderService.createPurchaseOrder(saveReqVO);

        // 更新订单->设置来源"采购合并"
        saveReqVO.setId(orderId);
        erpPurchaseOrderService.updatePurchaseOrder(saveReqVO);
        List<ErpPurchaseOrderItemDO> itemDOS = erpPurchaseOrderItemMapper.selectListByOrderId(orderId);
        itemDOS.forEach(itemDO -> itemDO.setSource("采购合并"));
        erpPurchaseOrderItemMapper.updateBatch(itemDOS);
        //修改子单据的来源
        return orderId;
    }

    private void validMerge(List<ErpPurchaseRequestItemsDO> itemsDOS) {
        //1. 校验申请单需已审核
        List<ErpPurchaseRequestDO> requestDOS = erpPurchaseRequestMapper.selectByIds(convertList(itemsDOS, ErpPurchaseRequestItemsDO::getRequestId));
        for (ErpPurchaseRequestDO requestDO : requestDOS) {
            ThrowUtil.ifThrow(!requestDO.getStatus().equals(SrmAuditStatus.APPROVED.getCode()), PURCHASE_REQUEST_MERGE_FAIL, requestDO.getNo());
        }
        //2. 校验申请项需处于开启状态
        for (ErpPurchaseRequestItemsDO itemsDO : itemsDOS) {
            ThrowUtil.ifThrow(!itemsDO.getOffStatus().equals(ErpOffStatus.OPEN.getCode()), PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_OPEN, itemsDO.getId());
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseRequest(ErpPurchaseRequestSaveReqVO updateReqVO) {
        //1 校验
        ErpPurchaseRequestDO erpPurchaseRequestDO = validateIdExists(updateReqVO.getId());
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
        ThrowUtil.ifThrow(requestDO.getStatus().equals(SrmAuditStatus.APPROVED.getCode()), PURCHASE_REQUEST_UPDATE_FAIL_APPROVE, requestDO.getNo());
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
        List<List<ErpPurchaseRequestItemsDO>> diffList = diffList(oldList, itemsDOList, (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));
        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setRequestId(id));
            erpPurchaseRequestItemsMapper.insertBatch(diffList.get(0));
            //初始化状态
            initSlaveStatus(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpPurchaseRequestItemsMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            //触发关闭状态
            for (ErpPurchaseRequestItemsDO itemsDO : diffList.get(2)) {
                offItemMachine.fireEvent(ErpOffStatus.fromCode(itemsDO.getOffStatus()), SrmEventEnum.MANUAL_CLOSE, itemsDO);
            }
            erpPurchaseRequestItemsMapper.deleteByIds(convertList(diffList.get(2), ErpPurchaseRequestItemsDO::getId));
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
    public void reviewPurchaseOrder(ErpPurchaseRequestAuditReqVO req) {
        // 查询采购申请单信息
        ErpPurchaseRequestDO requestDO = erpPurchaseRequestMapper.selectById(req.getRequestId());

        if (requestDO == null) {
            log.error("采购申请单不存在，ID: {}", req.getRequestId());
            throw ServiceExceptionUtil.exception(PURCHASE_REQUEST_NOT_EXISTS, req.getRequestId());
        }
        // 获取当前申请单状态
        SrmAuditStatus currentStatus = SrmAuditStatus.fromCode(requestDO.getStatus());
        if (Boolean.TRUE.equals(req.getReviewed())) {
            // 审核操作
            if (req.getPass()) {
                log.debug("采购申请单通过审核，ID: {}", req.getRequestId());
                auditMachine.fireEvent(currentStatus, SrmEventEnum.AGREE, req);
            } else {
                log.debug("采购申请单拒绝审核，ID: {}", req.getRequestId());
                auditMachine.fireEvent(currentStatus, SrmEventEnum.REJECT, req);
            }
        } else {
            //反审核
            //存在对应的采购订单项->异常
            List<ErpPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectListByRequestId(req.getRequestId());
            for (ErpPurchaseRequestItemsDO itemsDO : itemsDOS) {
                ThrowUtil.ifThrow(erpPurchaseOrderItemMapper.selectCountByPurchaseApplyItemId(itemsDO.getId()) > 0, PURCHASE_REQUEST_ITEM_ORDERED, itemsDO.getId());
            }
            log.debug("采购申请单撤回审核，ID: {}", req.getRequestId());
            auditMachine.fireEvent(currentStatus, SrmEventEnum.WITHDRAW_REVIEW, req);
        }
    }


    @Override
    public void submitAudit(Collection<Long> ids) {
        if (!CollUtil.isEmpty(ids)) {
            List<ErpPurchaseRequestDO> dos = erpPurchaseRequestMapper.selectByIds(ids);
            for (ErpPurchaseRequestDO aDo : dos) {
                auditMachine.fireEvent(SrmAuditStatus.fromCode(aDo.getStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW, ErpPurchaseRequestAuditReqVO.builder().requestId(aDo.getId()).build());
            }
        }
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
        SrmEventEnum event = Boolean.TRUE.equals(enable) ? SrmEventEnum.ACTIVATE : SrmEventEnum.MANUAL_CLOSE;
        if (requestId != null) {
            // 处理采购订单状态
            ErpPurchaseRequestDO aDo = erpPurchaseRequestMapper.selectById(requestId);
            if (aDo != null) {
                offMachine.fireEvent(ErpOffStatus.fromCode(aDo.getOffStatus()), event, aDo);
            }
        } else if (itemIds != null && !itemIds.isEmpty()) {
            // 批量处理采购订单子项状态
            List<ErpPurchaseRequestItemsDO> itemsDOList = erpPurchaseRequestItemsMapper.selectByIds(itemIds);
            if (itemsDOList != null && !itemsDOList.isEmpty()) {
                itemsDOList.forEach(itemsDO -> offItemMachine.fireEvent(ErpOffStatus.fromCode(itemsDO.getOffStatus()), event, itemsDO));
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseRequest(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpPurchaseRequestDO> purchaseRequestDOs = erpPurchaseRequestMapper.selectByIds(ids);
        if (CollUtil.isEmpty(purchaseRequestDOs)) {
            return;
        }
        // 1.1 已审核->异常
        purchaseRequestDOs.forEach(erpPurchaseRequestDO -> {
            ThrowUtil.ifThrow(erpPurchaseRequestDO.getStatus().equals(SrmAuditStatus.APPROVED.getCode()), PURCHASE_REQUEST_DELETE_FAIL_APPROVE, erpPurchaseRequestDO.getNo());
            //已关闭->异常
            ThrowUtil.ifThrow(erpPurchaseRequestDO.getOffStatus().equals(ErpOffStatus.CLOSED.getCode()), PURCHASE_REQUEST_DELETE_FAIL_CLOSE, erpPurchaseRequestDO.getNo());
        });
        //1.2 校验存在关联的采购订单
        ThrowUtil.ifThrow(!validHasApplyItemId(purchaseRequestDOs), PURCHASE_REQUEST_DELETE_FAIL);

        //2.0 手动关闭所有行状态
        for (ErpPurchaseRequestDO requestDO : purchaseRequestDOs) {
            List<ErpPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectListByRequestId(requestDO.getId());
            itemsDOS.forEach(itemsDO -> offItemMachine.fireEvent(ErpOffStatus.fromCode(itemsDO.getOffStatus()), SrmEventEnum.MANUAL_CLOSE, itemsDO));
        }
        //2.1 遍历删除，并记录操作日志
        purchaseRequestDOs.forEach(erpPurchaseRequest -> {
            //获取主表id
            Long id = erpPurchaseRequest.getId();
            erpPurchaseRequestMapper.deleteById(id);
            erpPurchaseRequestItemsMapper.deleteByRequestId(id);
        });
    }

    //校验是否存在关联的采购订单
    private Boolean validHasApplyItemId(List<ErpPurchaseRequestDO> purchaseRequestDOs) {
        //收集purchaseRequestDOs的id
        List<Long> ids = purchaseRequestDOs.stream().map(ErpPurchaseRequestDO::getId).collect(Collectors.toList());
        List<ErpPurchaseRequestItemsDO> requestItemsDOS = erpPurchaseRequestItemsMapper.selectListByRequestIds(ids);
        //收集item的id,去重
        Set<Long> itemIds = requestItemsDOS.stream().map(ErpPurchaseRequestItemsDO::getId).collect(Collectors.toSet());
        List<ErpPurchaseOrderItemDO> dos = erpPurchaseOrderItemMapper.selectListByPurchaseApplyItemIds(itemIds);
        //如果dos非空，返回true
        return CollUtil.isEmpty(dos);
    }

    @Override
    public ErpPurchaseRequestDO validateIdExists(Long id) {
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