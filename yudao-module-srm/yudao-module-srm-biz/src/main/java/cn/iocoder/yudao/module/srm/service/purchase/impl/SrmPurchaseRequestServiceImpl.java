package cn.iocoder.yudao.module.srm.service.purchase.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.ErpProductUnitApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.config.machine.SrmQuantityOrderedCountContext;
import cn.iocoder.yudao.module.srm.config.machine.request.SrmRequestInMachineContext;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.order.req.SrmPurchaseOrderSaveReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.request.req.*;
import cn.iocoder.yudao.module.srm.convert.purchase.SrmOrderConvert;
import cn.iocoder.yudao.module.srm.convert.purchase.SrmPurchaseRequestItemsConvert;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseOrderItemDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseOrderItemMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmPurchaseRequestMapper;
import cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO;
import cn.iocoder.yudao.module.srm.enums.LogRecordConstants;
import cn.iocoder.yudao.module.srm.enums.SrmEventEnum;
import cn.iocoder.yudao.module.srm.enums.status.SrmAuditStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOffStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmOrderStatus;
import cn.iocoder.yudao.module.srm.enums.status.SrmStorageStatus;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseOrderService;
import cn.iocoder.yudao.module.srm.service.purchase.SrmPurchaseRequestService;
import cn.iocoder.yudao.module.srm.service.purchase.bo.request.SrmPurchaseRequestBO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.request.SrmPurchaseRequestItemsBO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.api.warehouse.WmsWarehouseApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.srm.dal.redis.no.SrmNoRedisDAO.PURCHASE_REQUEST_NO_PREFIX;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.*;
import static cn.iocoder.yudao.module.srm.enums.SrmStateMachines.*;
import static jodd.util.StringUtil.truncate;

/**
 * ERP采购申请单 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SrmPurchaseRequestServiceImpl implements SrmPurchaseRequestService {

    private final SrmPurchaseRequestMapper srmPurchaseRequestMapper;
    private final SrmPurchaseRequestItemsMapper erpPurchaseRequestItemsMapper;
    private final SrmPurchaseOrderItemMapper srmPurchaseOrderItemMapper;
    private final SrmNoRedisDAO noRedisDAO;
    private final WmsWarehouseApi wmsWarehouseApi;
    private final ErpProductApi erpProductApi;
    private final DeptApi deptApi;
    private final AdminUserApi adminUserApi;
    private final ErpProductUnitApi erpProductUnitApi;
    @Autowired
    @Lazy
    SrmPurchaseOrderService srmPurchaseOrderService;
    @Resource(name = PURCHASE_REQUEST_AUDIT_STATE_MACHINE)
    StateMachine<SrmAuditStatus, SrmEventEnum, SrmPurchaseRequestAuditReqVO> auditMachine;
    @Resource(name = PURCHASE_REQUEST_OFF_STATE_MACHINE)
    StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestDO> offMachine;
    @Resource(name = PURCHASE_REQUEST_ORDER_STATE_MACHINE)
    StateMachine<SrmOrderStatus, SrmEventEnum, SrmPurchaseRequestDO> orderMachine;
    @Resource(name = PURCHASE_REQUEST_STORAGE_STATE_MACHINE)
    StateMachine<SrmStorageStatus, SrmEventEnum, SrmPurchaseRequestDO> storageMachine;
    //
    @Resource(name = PURCHASE_REQUEST_ITEM_OFF_STATE_MACHINE_NAME)
    StateMachine<SrmOffStatus, SrmEventEnum, SrmPurchaseRequestItemsDO> requestItemsDOStateMachine;
    @Resource(name = PURCHASE_REQUEST_ITEM_ORDER_STATE_MACHINE_NAME)
    StateMachine<SrmOrderStatus, SrmEventEnum, SrmQuantityOrderedCountContext> orderItemMachine;
    @Resource(name = PURCHASE_REQUEST_ITEM_STORAGE_STATE_MACHINE_NAME)
    StateMachine<SrmStorageStatus, SrmEventEnum, SrmRequestInMachineContext> storageItemMachine;

    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_REQUEST_TYPE,
            subType = LogRecordConstants.SRM_PURCHASE_REQUEST_CREATE_SUB_TYPE,
            bizNo = "{{#id}}",
            extra = "{{#vo.code}}",
            success = "创建了采购申请单【{{#vo.code}}】")
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseRequest(SrmPurchaseRequestSaveReqVO vo) {
        //获取单据日期，不为空就拿，为空就当前时间
        vo.setBillTime(vo.getBillTime() == null ? LocalDateTime.now() : vo.getBillTime());
        //1.校验
        voSetCode(vo);
        //1.2 校验子表合法
        List<SrmPurchaseRequestItemsDO> itemsDOList = validatePurchaseRequestItems(vo.getItems());
        //1.3 校验部门合法
        deptApi.validateDeptList(Collections.singleton(vo.getApplicationDeptId()));
        //1.4 校验申请人合法
        adminUserApi.validateUser(vo.getApplicantId());
        //初始化
        SrmPurchaseRequestDO purchaseRequest = BeanUtils.toBean(vo, SrmPurchaseRequestDO.class);
        purchaseRequest.setCode(vo.getCode());
        //2. 插入主表的申请单数据
        ThrowUtil.ifSqlThrow(srmPurchaseRequestMapper.insert(purchaseRequest), PURCHASE_REQUEST_ADD_FAIL_APPROVE);
        Long id = purchaseRequest.getId();
        itemsDOList.forEach(i -> i.setRequestId(id));
        //3. 批量插入子表数据
        ThrowUtil.ifThrow(!erpPurchaseRequestItemsMapper.insertBatch(itemsDOList), PURCHASE_REQUEST_ADD_FAIL_PRODUCT);
        //5.初始化状态-主子表
        initMasterStatus(purchaseRequest);
        List<SrmPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectListByRequestId(purchaseRequest.getId());
        initSlaveStatus(itemsDOS);
        //
        return id;
    }

    private void voSetCode(SrmPurchaseRequestSaveReqVO vo) {
        //生成单据编号
        if (vo.getCode() != null) {
            ThrowUtil.ifThrow(srmPurchaseRequestMapper.selectByNo(vo.getCode()) != null, PURCHASE_REQUEST_NO_EXISTS_BY_NO, vo.getCode());
            noRedisDAO.setManualSerial(PURCHASE_REQUEST_NO_PREFIX, vo.getCode());
        } else {
            vo.setCode(noRedisDAO.generate(PURCHASE_REQUEST_NO_PREFIX, PURCHASE_REQUEST_NO_OUT_OF_BOUNDS));
            //1.1 校验编号no是否在数据库中重复
            ThrowUtil.ifThrow(srmPurchaseRequestMapper.selectByNo(vo.getCode()) != null, PURCHASE_REQUEST_NO_EXISTS);
        }
    }

    private void initSlaveStatus(List<SrmPurchaseRequestItemsDO> itemsDOS) {
        itemsDOS.forEach(i -> {
            orderItemMachine.fireEvent(SrmOrderStatus.OT_ORDERED, SrmEventEnum.ORDER_INIT, SrmQuantityOrderedCountContext.builder().purchaseRequestItemId(i.getId()).build());
            requestItemsDOStateMachine.fireEvent(SrmOffStatus.OPEN, SrmEventEnum.OFF_INIT, i);
            //入库状态初始化
            storageItemMachine.fireEvent(SrmStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, SrmRequestInMachineContext.builder().applyItemId(i.getId()).build());
        });
    }

    private void initMasterStatus(SrmPurchaseRequestDO purchaseRequest) {

        auditMachine.fireEvent(SrmAuditStatus.DRAFT, SrmEventEnum.AUDIT_INIT,
            SrmPurchaseRequestAuditReqVO.builder().requestId(purchaseRequest.getId()).build());
        offMachine.fireEvent(SrmOffStatus.OPEN, SrmEventEnum.OFF_INIT, purchaseRequest);
        orderMachine.fireEvent(SrmOrderStatus.OT_ORDERED, SrmEventEnum.ORDER_INIT, purchaseRequest);
        //入库
        storageMachine.fireEvent(SrmStorageStatus.NONE_IN_STORAGE, SrmEventEnum.STORAGE_INIT, purchaseRequest);
    }

    /**
     * 校验采购订单的子项目是否合法
     *
     * @param items 采购订单子项目集合 1、校验产品有效性 2、校验仓库有效性
     */
    @Override
    public List<SrmPurchaseRequestItemsDO> validatePurchaseRequestItems(List<SrmPurchaseRequestItemsSaveReqVO> items) {
        // 1. 校验产品有效性
        List<ErpProductDTO> dtoList = erpProductApi.validProductList(convertSet(items, SrmPurchaseRequestItemsSaveReqVO::getProductId));
        Map<Long, ErpProductDTO> dtoMap = erpProductApi.getProductMap(convertSet(dtoList, ErpProductDTO::getId));
        // 1.1 校验仓库有效性
        wmsWarehouseApi.validWarehouseList(convertSet(items, SrmPurchaseRequestItemsSaveReqVO::getWarehouseId));
        // 1.2 把产品转化为sku+报关品名
        // 2. 转化为 SrmPurchaseRequestItemsDO 列表
        return convertList(items, o -> BeanUtils.toBean(o, SrmPurchaseRequestItemsDO.class, item -> {
            //产品名称
            item.setProductName(dtoMap.get(item.getProductId()).getName());
            item.setProductCode(dtoMap.get(item.getProductId()).getCode());
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
        ThrowUtil.ifThrow(erpPurchaseRequestItemsMapper.selectListByIds(ids).stream().noneMatch(i -> ids.contains(i.getId())), PURCHASE_REQUEST_ITEM_NOT_EXISTS,
            ids);
        // 校验子单requestId是否关联主单的id
        ThrowUtil.ifThrow(erpPurchaseRequestItemsMapper.selectListByRequestId(masterId).stream().noneMatch(i -> ids.contains(i.getId())),
            PURCHASE_REQUEST_UPDATE_FAIL_REQUEST_ID, masterId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long merge(SrmPurchaseRequestMergeReqVO reqVO) {
        // 获取所有 itemIds
        List<Long> itemIds = reqVO.getItems().stream().map(SrmPurchaseRequestMergeReqVO.requestItems::getId).collect(Collectors.toList());

        // 查询 itemDOS 并校验
        List<SrmPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectListByIds(itemIds);
        ThrowUtil.ifThrow(itemsDOS.isEmpty(), PURCHASE_REQUEST_ITEM_NOT_EXISTS, itemIds);

        //申请单已审核+未关闭才可以合并
        validMerge(itemsDOS);
        // 创建 itemsMap 和 requestItemDOMap
        Map<Long, SrmPurchaseRequestMergeReqVO.requestItems> itemsMap = reqVO.getItems().stream().collect(Collectors.toMap(SrmPurchaseRequestMergeReqVO.requestItems::getId, Function.identity()));
        Map<Long, SrmPurchaseRequestItemsDO> requestItemDOMap = itemsDOS.stream().collect(Collectors.toMap(SrmPurchaseRequestItemsDO::getId, Function.identity()));

        // SrmPurchaseRequestItemsDO item -> 订单vo item
        List<SrmPurchaseOrderSaveReqVO.Item> voItemList = SrmOrderConvert.INSTANCE.convertToErpPurchaseOrderSaveReqVOItemList(itemsDOS, itemsMap, requestItemDOMap);
        // 获取申请单编号映射
        Set<Long> requestIds = itemsDOS.stream().map(SrmPurchaseRequestItemsDO::getRequestId).collect(Collectors.toSet());
        Map<Long, SrmPurchaseRequestDO> rDOMap =
            srmPurchaseRequestMapper.selectByIds(requestIds).stream().collect(Collectors.toMap(SrmPurchaseRequestDO::getId, Function.identity()));

        //后置渲染，申请单的no编号
        voItemList.forEach(item -> {
            Long itemId = item.getPurchaseApplyItemId();
            item.setId(null);
            item.setPurchaseApplyItemId(itemId);//采购申请项id
            //获得主表DO
            SrmPurchaseRequestDO aDo = rDOMap.get(requestItemDOMap.get(itemId).getRequestId());
            item.setApplicantId(aDo.getApplicantId());//申请人
            item.setApplicationDeptId(aDo.getApplicationDeptId());//申请部门
            //设置来源
            item.setSource("采购合并");
        });

        //2.0 构造VO入参类
        SrmPurchaseOrderSaveReqVO saveReqVO = BeanUtils.toBean(reqVO, SrmPurchaseOrderSaveReqVO.class, vo -> {
            vo.setId(null);
            vo.setItems(voItemList);
            //            vo.setRemark("申请人期望采购日期: " + DateUtil.format(reqVO.getOrderTime(), DatePattern.CHINESE_DATE_PATTERN));
        });
        Long purchaseOrder = null;
        try {
            purchaseOrder = srmPurchaseOrderService.createPurchaseOrder(saveReqVO);
        } catch (Exception e) {
            log.error("[merge][合并采购申请单失败，申请单编号({})，原因({})]", saveReqVO.getId(), e.getMessage());
            throw exception(PURCHASE_REQUEST_MERGE_FAIL_REASON, truncate(e.getMessage(), 200));
        }
        return purchaseOrder;
    }

    private void validMerge(List<SrmPurchaseRequestItemsDO> itemsDOS) {
        //1. 校验申请单需已审核
        List<SrmPurchaseRequestDO> requestDOS = srmPurchaseRequestMapper.selectByIds(convertList(itemsDOS, SrmPurchaseRequestItemsDO::getRequestId));
        for (SrmPurchaseRequestDO requestDO : requestDOS) {
            ThrowUtil.ifThrow(!requestDO.getAuditStatus().equals(SrmAuditStatus.APPROVED.getCode()), PURCHASE_REQUEST_MERGE_FAIL, requestDO.getCode());
        }
        //2. 校验申请项需处于开启状态
        for (SrmPurchaseRequestItemsDO itemsDO : itemsDOS) {
            ThrowUtil.ifThrow(!itemsDO.getOffStatus().equals(SrmOffStatus.OPEN.getCode()), PURCHASE_REQUEST_ITEM_NOT_EXISTS_BY_OPEN, itemsDO.getId());
        }
    }

    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_REQUEST_TYPE,
            subType = LogRecordConstants.SRM_PURCHASE_REQUEST_UPDATE_SUB_TYPE,
            bizNo = "{{#vo.id}}",
            extra = "{{#vo.code}}",
            success = "更新了采购申请单【{{#vo.code}}】: {_DIFF{#vo}}")
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseRequest(SrmPurchaseRequestSaveReqVO vo) {
        //1 校验
        SrmPurchaseRequestDO srmPurchaseRequestDO = validateIdExists(vo.getId());
        //1.1 校验items是否存在
        List<SrmPurchaseRequestItemsDO> itemsDOList = validatePurchaseRequestItems(vo.getItems());
        //1.2 状态校验
        updateStatusCheck(srmPurchaseRequestDO, vo.getItems().stream().map(SrmPurchaseRequestItemsSaveReqVO::getId).toList());
        //1.4 校验主子表关联
        List<SrmPurchaseRequestItemsDO> requestItemsDOS = erpPurchaseRequestItemsMapper.findItemsGroupedByRequestId(List.of(vo.getId())).get(vo.getId());
        List<Long> requestItemsIds = convertList(requestItemsDOS, SrmPurchaseRequestItemsDO::getId);
        validatePurchaseRequestItemsMasterId(vo.getId(), requestItemsIds);
        //1.5 设置no
        String oldNo = srmPurchaseRequestDO.getCode();
        if (!oldNo.equals(vo.getCode())) {
            voSetCode(vo);
        }
        // 2 更新
        // 2.2 更新主表
        SrmPurchaseRequestDO updateObj = BeanUtils.toBean(vo, SrmPurchaseRequestDO.class);
        srmPurchaseRequestMapper.updateById(updateObj);
        // 2.3 更新子表
        updatePurchaseRequestItemList(vo.getId(), itemsDOList);
    }

    //判断当前状态是否可以更新
    private void updateStatusCheck(SrmPurchaseRequestDO requestDO, List<Long> itemIds) {
        //1.1 不处于草稿、审核不通过、审核撤销 状态->e
        ThrowUtil.ifThrow(
            !requestDO.getAuditStatus().equals(SrmAuditStatus.DRAFT.getCode()) && !requestDO.getAuditStatus()
                .equals(SrmAuditStatus.REJECTED.getCode()) && !requestDO.getAuditStatus()
                .equals(SrmAuditStatus.REVOKED.getCode()), PURCHASE_REQUEST_UPDATE_FAIL_APPROVE,
            SrmAuditStatus.fromCode(requestDO.getAuditStatus()).getDesc(), requestDO.getCode());
        //1.2 判断已关闭
        ThrowUtil.ifThrow(requestDO.getOffStatus().equals(SrmOffStatus.CLOSED.getCode()), PURCHASE_REQUEST_CLOSED, requestDO.getCode());
        //1.3 判断已手动关闭
        ThrowUtil.ifThrow(requestDO.getOffStatus().equals(SrmOffStatus.MANUAL_CLOSED.getCode()), PURCHASE_REQUEST_MANUAL_CLOSED, requestDO.getCode());
        //2 判断子表已关闭？手动关闭？
        List<SrmPurchaseRequestItemsDO> itemsDOList = erpPurchaseRequestItemsMapper.selectListByIds(itemIds);
        for (SrmPurchaseRequestItemsDO itemsDO : itemsDOList) {
            // 2.1 判断子表已关闭
            ThrowUtil.ifThrow(itemsDO.getOffStatus().equals(SrmOffStatus.CLOSED.getCode()), PURCHASE_REQUEST_ITEM_CLOSED, itemsDO.getId());
            // 2.2 判断子表已手动关闭
            ThrowUtil.ifThrow(itemsDO.getOffStatus().equals(SrmOffStatus.MANUAL_CLOSED.getCode()), PURCHASE_REQUEST_ITEM_MANUAL_CLOSED, itemsDO.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseRequestItemList(Long id, List<SrmPurchaseRequestItemsDO> newList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<SrmPurchaseRequestItemsDO> oldList = erpPurchaseRequestItemsMapper.selectListByRequestId(id);
        // id 不同，就认为是不同的记录
        List<List<SrmPurchaseRequestItemsDO>> diffList = diffList(oldList, newList, (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));
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
            for (SrmPurchaseRequestItemsDO itemsDO : diffList.get(2)) {
                requestItemsDOStateMachine.fireEvent(SrmOffStatus.fromCode(itemsDO.getOffStatus()), SrmEventEnum.MANUAL_CLOSE, itemsDO);
            }
            erpPurchaseRequestItemsMapper.deleteByIds(convertList(diffList.get(2), SrmPurchaseRequestItemsDO::getId));
            //TODO 触发其他状态改变
        }
    }

    @Override
    public List<SrmPurchaseRequestItemsDO> getPurchaseRequestItemListByOrderIds(Collection<Long> requestIds) {
        if (CollUtil.isEmpty(requestIds)) {
            return Collections.emptyList();
        }
        return erpPurchaseRequestItemsMapper.selectListByRequestIds(requestIds);
    }

    /**
     * 审核/反审核采购订单 该方法用于根据传入的请求参数对采购订单进行审核或反审核操作。
     */
    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_REQUEST_TYPE,
            subType = LogRecordConstants.SRM_PURCHASE_REQUEST_AUDIT_SUB_TYPE,
            bizNo = "{{#vo.requestId}}",
            extra = "{{#code}}",
            success = "{{#vo.reviewed ? (#vo.pass ? '审核通过' : '审核不通过') : '反审核'}}了采购申请单【{{#code}}】")
    public void reviewPurchaseOrder(SrmPurchaseRequestAuditReqVO vo) {
        // 查询采购申请单信息用于日志记录
        SrmPurchaseRequestDO requestDO = srmPurchaseRequestMapper.selectById(vo.getRequestId());
        LogRecordContext.putVariable("vo", requestDO);
        LogRecordContext.putVariable("reqVO", vo);

        if (requestDO == null) {
            log.error("采购申请单不存在，ID: {}", vo.getRequestId());
            throw ServiceExceptionUtil.exception(PURCHASE_REQUEST_NOT_EXISTS, vo.getRequestId());
        }
        //log
        LogRecordContext.putVariable("code", requestDO.getCode());
        // 获取当前申请单状态
        SrmAuditStatus currentStatus = SrmAuditStatus.fromCode(requestDO.getAuditStatus());
        if (Boolean.TRUE.equals(vo.getReviewed())) {
            // 审核操作
            if (vo.getPass()) {
                log.debug("采购申请单通过审核，ID: {}", vo.getRequestId());
                auditMachine.fireEvent(currentStatus, SrmEventEnum.AGREE, vo);
            } else {
                log.debug("采购申请单拒绝审核，ID: {}", vo.getRequestId());
                auditMachine.fireEvent(currentStatus, SrmEventEnum.REJECT, vo);
            }
        } else {
            //反审核
            //存在对应的采购订单项->异常
            List<SrmPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectListByRequestId(vo.getRequestId());
            for (SrmPurchaseRequestItemsDO itemsDO : itemsDOS) {
                ThrowUtil.ifThrow(srmPurchaseOrderItemMapper.selectCountByPurchaseApplyItemId(itemsDO.getId()) > 0, PURCHASE_REQUEST_ITEM_ORDERED,
                    itemsDO.getId());
            }
            log.debug("采购申请单撤回审核，ID: {}", vo.getRequestId());
            auditMachine.fireEvent(currentStatus, SrmEventEnum.WITHDRAW_REVIEW, vo);
        }
    }

    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_REQUEST_TYPE,
            subType = LogRecordConstants.SRM_PURCHASE_REQUEST_SUBMIT_AUDIT_SUB_TYPE,
            bizNo = "{{#ids[0]}}",
            extra = "{{#codes}}",
            success = "提交了采购申请单【{{#codes}}】审核")
    public void submitAudit(Collection<Long> ids) {
        // 获取单据编号用于日志记录
        List<SrmPurchaseRequestDO> requests = srmPurchaseRequestMapper.selectByIds(ids);
        String codes = CollUtil.join(requests.stream().map(SrmPurchaseRequestDO::getCode).collect(Collectors.toList()), ",");
        LogRecordContext.putVariable("codes", codes);
        
        if (!CollUtil.isEmpty(ids)) {
            List<SrmPurchaseRequestDO> dos = srmPurchaseRequestMapper.selectByIds(ids);
            for (SrmPurchaseRequestDO aDo : dos) {
                auditMachine.fireEvent(SrmAuditStatus.fromCode(aDo.getAuditStatus()), SrmEventEnum.SUBMIT_FOR_REVIEW,
                    SrmPurchaseRequestAuditReqVO.builder().requestId(aDo.getId()).build());
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
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_REQUEST_TYPE,
            subType = LogRecordConstants.SUB_TYPE_SWITCH_EXPRESSION,
            bizNo = LogRecordConstants.BIZ_NO_SWITCH_EXPRESSION,
            extra = "{{#codes}}",
            success = "{{#enable ? '开启了采购申请单【' + #codes + '】' : '关闭了采购申请单【' + #codes + '】'}}")
    @Transactional(rollbackFor = Exception.class)
    public void switchPurchaseOrderStatus(Long requestId, List<Long> itemIds, Boolean enable) {
        SrmEventEnum event = Boolean.TRUE.equals(enable) ? SrmEventEnum.ACTIVATE : SrmEventEnum.MANUAL_CLOSE;
        if (requestId != null) {
            // 处理采购申请主表状态
            SrmPurchaseRequestDO requestDO = validateIdExists(requestId);
            if (requestDO != null) {
                // 获取申请单编号用于日志记录
                LogRecordContext.putVariable("codes", requestDO.getCode());
                offMachine.fireEvent(SrmOffStatus.fromCode(requestDO.getOffStatus()), event, requestDO);
            }
        } else {
            if (itemIds != null && !itemIds.isEmpty()) {
                // 批量处理采购申请子项状态
                List<SrmPurchaseRequestItemsDO> itemsDOList = validItemIdsExist(itemIds);
                if (!itemsDOList.isEmpty()) {
                    // 获取申请单编号用于日志记录
                    List<SrmPurchaseRequestDO> requests = srmPurchaseRequestMapper.selectByIds(
                            itemsDOList.stream().map(SrmPurchaseRequestItemsDO::getRequestId).collect(Collectors.toSet()));
                    String codes = CollUtil.join(requests.stream().map(SrmPurchaseRequestDO::getCode).collect(Collectors.toList()), ",");
                    LogRecordContext.putVariable("codes", codes);
                    itemsDOList.forEach(itemsDO -> requestItemsDOStateMachine.fireEvent(SrmOffStatus.fromCode(itemsDO.getOffStatus()), event, itemsDO));
                }
            }
        }
    }

    @Override
    @LogRecord(type = LogRecordConstants.SRM_PURCHASE_REQUEST_TYPE,
            subType = LogRecordConstants.SRM_PURCHASE_REQUEST_DELETE_SUB_TYPE,
            bizNo = "{{#ids[0]}}",
            extra = "{{#businessName}}",
            success = "删除了采购申请单【{{#businessName}}】")
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseRequest(List<Long> ids) {
        // 获取业务名称用于日志记录
        List<SrmPurchaseRequestDO> requests = srmPurchaseRequestMapper.selectByIds(ids);
        String businessName = CollUtil.join(requests.stream().map(SrmPurchaseRequestDO::getCode).collect(Collectors.toList()), ",");
        LogRecordContext.putVariable("businessName", businessName);
        
        // 1. 校验不处于已审批
        if (CollUtil.isEmpty(requests)) {
            return;
        }
        // 1.1 已审核->异常
        requests.forEach(erpPurchaseRequestDO -> {
            ThrowUtil.ifThrow(erpPurchaseRequestDO.getAuditStatus().equals(SrmAuditStatus.APPROVED.getCode()), PURCHASE_REQUEST_DELETE_FAIL_APPROVE,
                erpPurchaseRequestDO.getCode());
            //已关闭->异常
            ThrowUtil.ifThrow(erpPurchaseRequestDO.getOffStatus().equals(SrmOffStatus.CLOSED.getCode()), PURCHASE_REQUEST_DELETE_FAIL_CLOSE,
                erpPurchaseRequestDO.getCode());
        });
        //1.2 校验存在关联的采购订单
        //收集ids
        List<Long> requestDoIds = requests.stream().map(SrmPurchaseRequestDO::getId).collect(Collectors.toList());
        ThrowUtil.ifThrow(!validHasApplyItemId(requestDoIds), PURCHASE_REQUEST_DELETE_FAIL);

        //2.0 手动关闭所有行状态
        for (SrmPurchaseRequestDO requestDO : requests) {
            List<SrmPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectListByRequestId(requestDO.getId());
            itemsDOS.forEach(
                itemsDO -> requestItemsDOStateMachine.fireEvent(SrmOffStatus.fromCode(itemsDO.getOffStatus()), SrmEventEnum.MANUAL_CLOSE, itemsDO));
        }
        //2.1 遍历删除，并记录操作日志
        requests.forEach(erpPurchaseRequest -> {
            //获取主表id
            Long id = erpPurchaseRequest.getId();
            srmPurchaseRequestMapper.deleteById(id);
            erpPurchaseRequestItemsMapper.deleteByRequestId(id);
        });
    }

    //校验是否存在关联的采购订单
    private Boolean validHasApplyItemId(Collection<Long> requestDoIds) {
        requestDoIds = requestDoIds.stream().distinct().toList();
        //收集purchaseRequestDOs的id
        List<SrmPurchaseRequestItemsDO> requestItemsDOS = erpPurchaseRequestItemsMapper.selectListByRequestIds(requestDoIds);
        //收集item的id,去重
        Set<Long> itemIds = requestItemsDOS.stream().map(SrmPurchaseRequestItemsDO::getId).collect(Collectors.toSet());
        List<SrmPurchaseOrderItemDO> dos = srmPurchaseOrderItemMapper.selectListByPurchaseApplyItemIds(itemIds);
        //如果dos非空，返回true
        return CollUtil.isEmpty(dos);
    }

    @Override
    public SrmPurchaseRequestDO validateIdExists(Long id) {
        SrmPurchaseRequestDO srmPurchaseRequestDO = srmPurchaseRequestMapper.selectById(id);
        ThrowUtil.ifThrow(srmPurchaseRequestDO == null, PURCHASE_REQUEST_NOT_EXISTS);
        return srmPurchaseRequestDO;
    }

    //校验子项是否合法by itemId
    @Override
    public SrmPurchaseRequestItemsDO validItemIdExist(Long itemId) {
        SrmPurchaseRequestItemsDO requestItemsDO = erpPurchaseRequestItemsMapper.selectById(itemId);
        ThrowUtil.ifThrow(requestItemsDO == null, PURCHASE_REQUEST_ITEM_NOT_EXISTS);
        return requestItemsDO;
    }

    //校验子项是否合法、批量ids
    @Override
    public List<SrmPurchaseRequestItemsDO> validItemIdsExist(Collection<Long> itemIds) {
        if (CollUtil.isEmpty(itemIds)) {
            return Collections.emptyList();
        }
        //批量查询 比较
        List<SrmPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectByIds(itemIds);
        if (itemsDOS.size() != itemIds.size()) {
            throw exception(PURCHASE_REQUEST_ITEM_NOT_EXISTS,
                CollUtil.subtract(itemIds, CollUtil.newArrayList(itemsDOS.stream().map(SrmPurchaseRequestItemsDO::getId).collect(Collectors.toSet()))));
        }
        return itemsDOS;
    }

    @Override
    public SrmPurchaseRequestDO getPurchaseRequest(Long id) {
        return srmPurchaseRequestMapper.selectById(id);
    }

    @Override
    public SrmPurchaseRequestBO getPurchaseRequestBO(Long id) {
        //查主表
        SrmPurchaseRequestDO srmPurchaseRequestDO = srmPurchaseRequestMapper.selectById(id);
        //查子表
        List<SrmPurchaseRequestItemsDO> itemsDOS = erpPurchaseRequestItemsMapper.selectListByRequestId(id);
        //转换 SrmPurchaseRequestBO
        return BeanUtils.toBean(srmPurchaseRequestDO, SrmPurchaseRequestBO.class, bo -> bo.setItems(itemsDOS));
    }

    @Override
    public PageResult<SrmPurchaseRequestBO> getPurchaseRequestItemBOPage(SrmPurchaseRequestPageReqVO pageReqVO) {
        // 1. 查询分页数据
        PageResult<SrmPurchaseRequestItemsBO> requestItemsBOPageResult = erpPurchaseRequestItemsMapper.selectPageBO(pageReqVO);

        // 2. 转换为目标BO
        List<SrmPurchaseRequestBO> purchaseRequestBOList = SrmPurchaseRequestItemsConvert.INSTANCE.convertList(requestItemsBOPageResult.getList());
        
        // 3. 返回分页结果
        return new PageResult<>(purchaseRequestBOList, requestItemsBOPageResult.getTotal());
    }

    @Override
    public List<SrmPurchaseRequestItemsDO> getPurchaseRequestItemListByOrderId(Long requestId) {
        return erpPurchaseRequestItemsMapper.selectListByRequestId(requestId);
    }

    @Override
    public String getMaxSerialNumber() {
        return noRedisDAO.getMaxSerial(PURCHASE_REQUEST_NO_PREFIX, PURCHASE_REQUEST_NO_OUT_OF_BOUNDS);
    }
}