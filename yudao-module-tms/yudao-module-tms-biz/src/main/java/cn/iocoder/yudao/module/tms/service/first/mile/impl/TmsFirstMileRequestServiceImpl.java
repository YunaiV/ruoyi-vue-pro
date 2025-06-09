package cn.iocoder.yudao.module.tms.service.first.mile.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestAuditReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo.TmsFirstMileRequestSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileSaveReqVO;
import cn.iocoder.yudao.module.tms.convert.first.mile.request.TmsFirstMileRequestConvert;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item.TmsFirstMileItemDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.dal.mysql.first.mile.request.TmsFirstMileRequestMapper;
import cn.iocoder.yudao.module.tms.dal.mysql.first.mile.request.item.TmsFirstMileRequestItemMapper;
import cn.iocoder.yudao.module.tms.dal.redis.no.TmsNoRedisDAO;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.TmsLogRecordConstants;
import cn.iocoder.yudao.module.tms.enums.status.TmsAuditStatus;
import cn.iocoder.yudao.module.tms.enums.status.TmsOffStatus;
import cn.iocoder.yudao.module.tms.enums.status.TmsOrderStatus;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileRequestBO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileRequestItemItemBO;
import cn.iocoder.yudao.module.tms.service.first.mile.TmsFirstMileService;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestItemService;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestService;
import cn.iocoder.yudao.module.wms.api.warehouse.WmsWarehouseApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.dal.redis.no.TmsNoRedisDAO.FIRST_MILE_REQUEST_NO_PREFIX;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.*;
import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.*;

/**
 * 头程申请单 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TmsFirstMileRequestServiceImpl implements TmsFirstMileRequestService {

    private final TmsFirstMileRequestMapper firstMileRequestMapper;
    private final TmsFirstMileRequestItemMapper firstMileRequestItemMapper;
    private final TmsNoRedisDAO tmsNoRedisDAO;
    private final ErpProductApi erpProductApi;
    private final TmsFirstMileRequestItemService firstMileRequestItemService;
    private final AdminUserApi adminUserApi;
    private final DeptApi deptApi;
    private final WmsWarehouseApi wmsWarehouseApi;
    @Autowired
    @Lazy
    TmsFirstMileService firstMileService;

    @Resource(name = FIRST_MILE_REQUEST_AUDIT_STATE_MACHINE)
    private StateMachine<TmsAuditStatus, TmsEventEnum, TmsFirstMileRequestAuditReqVO> tmsFirstMileRequestStatusMachine;
    @Resource(name = FIRST_MILE_REQUEST_OFF_STATE_MACHINE)
    private StateMachine<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestDO> offStatusStatusMachine;
    @Resource(name = FIRST_MILE_REQUEST_PURCHASE_ORDER_STATE_MACHINE)
    private StateMachine<TmsOrderStatus, TmsEventEnum, TmsFirstMileRequestDO> orderStatusStatusMachine;
    @Resource(name = FIRST_MILE_REQUEST_ITEM_OFF_STATE_MACHINE)
    private StateMachine<TmsOffStatus, TmsEventEnum, TmsFirstMileRequestItemDO> offItemStatusMachine;

    //校验code中间日期是否是当天
    private static void validCodeDateIsToday(String code) {
        String[] parts = code.split("-");
        if (parts.length != 3) {
            throw exception(FIRST_MILE_REQUEST_CODE_FORMAT_ERROR, code);
        }
        String dateStr = parts[1];
        String today = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        if (!dateStr.equals(today)) {
            throw exception(FIRST_MILE_REQUEST_CODE_DATE_NOT_TODAY, dateStr);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_CREATE_SUB_TYPE,
            bizNo = "{{#id}}",
            extra = "{{#vo.code}}",
            success = "创建了头程申请单【{{#vo.code}}】")
    public Long createFirstMileRequest(@Validated TmsFirstMileRequestSaveReqVO vo) {
        // 插入
        TmsFirstMileRequestDO firstMileRequest = BeanUtils.toBean(vo, TmsFirstMileRequestDO.class);

        // 计算主表的总重量和总体积
        List<TmsFirstMileRequestItemDO> requestItemDOS = TmsFirstMileRequestConvert.convertItemList(vo.getItems());
        //校验产品是否存在
        erpProductApi.validProductList(requestItemDOS.stream().map(TmsFirstMileRequestItemDO::getProductId).distinct().toList());
        calculateTotalWeightAndVolume(firstMileRequest, requestItemDOS);

        //code 校验
        if (vo.getCode() != null) {
            validCodeDateIsToday(vo.getCode());
            if (validCodeDuplicate(vo.getCode())) {
                throw exception(FIRST_MILE_REQUEST_CODE_DUPLICATE, vo.getCode());
            }
            tmsNoRedisDAO.setManualSerial(FIRST_MILE_REQUEST_NO_PREFIX, vo.getCode());
        } else {
            vo.setCode(tmsNoRedisDAO.generate(FIRST_MILE_REQUEST_NO_PREFIX, FIRST_MILE_REQUEST_CREATE_FAIL));
        }
        //校验产品是否存在
        erpProductApi.validProductList(requestItemDOS.stream().map(TmsFirstMileRequestItemDO::getProductId).distinct().toList());
        // 校验人+部门+仓库是否合法
        adminUserApi.validateUser(firstMileRequest.getRequesterId());
        deptApi.validateDeptList(Collections.singleton(firstMileRequest.getRequestDeptId()));
        wmsWarehouseApi.validWarehouseList(Collections.singleton(firstMileRequest.getToWarehouseId()));

        firstMileRequestMapper.insert(firstMileRequest);
        // 插入子表
        firstMileRequestItemService.createFirstMileRequestItemList(firstMileRequest.getId(), requestItemDOS);
        //初始化主子表状态
        initMasterStatus(firstMileRequest);

        LogRecordContext.putVariable("id", firstMileRequest.getId());
        // 返回
        return firstMileRequest.getId();
    }

    private void initMasterStatus(TmsFirstMileRequestDO firstMileRequest) {
        //审核
        tmsFirstMileRequestStatusMachine.fireEvent(TmsAuditStatus.DRAFT, TmsEventEnum.AUDIT_INIT,
            TmsFirstMileRequestAuditReqVO.builder().requestId(firstMileRequest.getId()).build());
        //开关
        offStatusStatusMachine.fireEvent(TmsOffStatus.OPEN, TmsEventEnum.OFF_INIT, firstMileRequest);
        //采购
        orderStatusStatusMachine.fireEvent(TmsOrderStatus.OT_ORDERED, TmsEventEnum.ORDER_INIT, firstMileRequest);
    }

    /**
     * 数据库已存在code
     *
     * @param code code
     * @return Boolean
     */
    private Boolean validCodeDuplicate(String code) {
        return firstMileRequestMapper.selectByNo(code) != null;
    }

    //判断当前状态是否可以更新
    private void statusCheckForUpdate(TmsFirstMileRequestDO oldDo, ErrorCode errorCode) {
        //1.1 已审核->e
        ThrowUtil.ifThrow(oldDo.getAuditStatus().equals(TmsAuditStatus.APPROVED.getCode()), errorCode, oldDo.getCode(),
            TmsAuditStatus.fromCode(oldDo.getAuditStatus()).getDesc());
        //1.2 未开启->e
        ThrowUtil.ifThrow(!oldDo.getOffStatus().equals(TmsOffStatus.OPEN.getCode()), errorCode, oldDo.getCode(),
            TmsOffStatus.fromCode(oldDo.getOffStatus()).getDesc());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_DELETE_SUB_TYPE,
            bizNo = "{{#id}}",
            success = "删除了头程申请单【{{#businessName}}】")
    public void deleteFirstMileRequest(Long id) {
        // 获取头程申请单信息，用于记录日志
        TmsFirstMileRequestDO request = validateFirstMileRequestExists(id);
        // 记录操作日志上下文
        LogRecordContext.putVariable("businessName", request.getCode());
        // 校验存在+状态
        statusCheckForUpdate(request, FIRST_MILE_REQUEST_DELETED_FAIL_APPROVE);
        // 手动关闭所有行状态
        validHasFirstMileItem(id, FIRST_MILE_REQUEST_DELETE_FAIL_STATUS_ERROR);
        // 删除
        firstMileRequestMapper.deleteById(id);

        // 删除子表
        firstMileRequestItemService.deleteFirstMileRequestItemByRequestId(id);
    }

    @Override
    public TmsFirstMileRequestDO validateFirstMileRequestExists(Long id) {
        TmsFirstMileRequestDO mileRequestDO = firstMileRequestMapper.selectById(id);
        if (mileRequestDO == null) {
            throw exception(FIRST_MILE_REQUEST_NOT_EXISTS, id);
        }
        return mileRequestDO;
    }

    @Override
    public PageResult<TmsFirstMileRequestBO> getFirstMileRequestBOPage(TmsFirstMileRequestPageReqVO pageReqVO) {
        // 1. 获取子表分页数据
        PageResult<TmsFirstMileRequestItemItemBO> pageResult = firstMileRequestItemMapper.selectPageBO(pageReqVO);
        if (CollectionUtils.isEmpty(pageResult.getList())) {
            return new PageResult<>(Collections.emptyList(), 0L);
        }
        // 2. 转换为BO对象列表
        List<TmsFirstMileRequestBO> boList = bindBOList(pageResult.getList());
        // 3. 返回分页结果
        return new PageResult<>(boList, pageResult.getTotal());
    }

    @Override
    public TmsFirstMileRequestBO getFirstMileRequestBO(Long id) {
        // 查询主表
        TmsFirstMileRequestDO firstMileRequestDO = validateFirstMileRequestExists(id);
        // 查询子表
        List<TmsFirstMileRequestItemDO> firstMileRequestItemDOList = firstMileRequestItemMapper.selectListByRequestId(id);
        // 转换
        return TmsFirstMileRequestConvert.convert(firstMileRequestDO, firstMileRequestItemDOList);
    }

    @Override
    public TmsFirstMileRequestDO getFirstMileRequest(Long id) {
        return firstMileRequestMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_UPDATE_SUB_TYPE,
            bizNo = "{{#id}}",
            extra = "{{#code}}",
            success = "更新了头程申请单【{{#code}}】: {_DIFF{#requestDO}}")
    public TmsFirstMileRequestDO updateFirstMileRequestStatus(Long id, Integer offStatus, Integer orderStatus, Integer auditStatus, String auditMsg) {
        // 获取头程申请单
        TmsFirstMileRequestDO requestDO = validateFirstMileRequestExists(id);
        // 更新状态字段
        if (offStatus != null) {
            requestDO.setOffStatus(offStatus);
        }
        if (orderStatus != null) {
            requestDO.setOrderStatus(orderStatus);
        }
        if (auditStatus != null) {
            requestDO.setAuditStatus(auditStatus);
            requestDO.setAuditAdvice(auditMsg);
        }
        // 执行更新
        firstMileRequestMapper.updateById(requestDO);
        //
        LogRecordContext.putVariable("code", requestDO.getCode());
        return requestDO;
    }


    @Override
    public TmsFirstMileRequestItemDO getFirstMileRequestItem(Long id) {
        return firstMileRequestItemMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TmsFirstMileRequestItemDO updateFirstMileRequestItemStatus(Long id, Integer openStatus, Integer orderStatus) {
        return null;
    }
    // ==================== 子表（头程申请表明细） ====================

    @Override
    public List<TmsFirstMileRequestItemDO> getFirstMileRequestItemListByRequestId(Long requestId) {
        return firstMileRequestItemMapper.selectListByRequestId(requestId);
    }

    /**
     * 将List<TmsFirstMileRequestItemBO>转换为List<TmsFirstMileRequestBO> 实现主表和子表数据的绑定
     *
     * @param itemBOList 包含主表和子表数据的BO对象列表
     * @return 转换后的BO对象列表
     */
    private List<TmsFirstMileRequestBO> bindBOList(List<TmsFirstMileRequestItemItemBO> itemBOList) {
        Map<Long, List<TmsFirstMileRequestItemItemBO>> itemMap = itemBOList.stream().filter(item -> item.getTmsFirstMileRequestDO() != null)
            .collect(Collectors.groupingBy(item -> item.getTmsFirstMileRequestDO().getId()));
        List<TmsFirstMileRequestBO> boList = new ArrayList<>();
        for (TmsFirstMileRequestItemItemBO itemBO : itemBOList) {
            if (itemBO.getTmsFirstMileRequestDO() == null) {
                continue;
            }
            // 检查该主表是否已经转换过
            Long requestId = itemBO.getTmsFirstMileRequestDO().getId();
            if (boList.stream().anyMatch(bo -> bo.getId().equals(requestId))) {
                continue;
            }
            // 转换主表数据
            TmsFirstMileRequestBO bo = TmsFirstMileRequestConvert.convert(itemBO.getTmsFirstMileRequestDO(),
                itemMap.get(requestId).stream().map(TmsFirstMileRequestConvert::convertItem).collect(Collectors.toList()));
            boList.add(bo);
        }
        return boList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_SUBMIT_AUDIT_SUB_TYPE,
            bizNo = "{{#ids[0]}}",
            success = "提交了头程申请单【{{#codes}}】审核")
    public void submitAudit(List<Long> ids) {
        // 获取头程申请单信息，用于记录日志
        List<TmsFirstMileRequestDO> requests = ids.stream()
                .map(this::validateFirstMileRequestExists)
                .toList();
        // 记录操作日志上下文
        String codes = requests.stream()
                .map(TmsFirstMileRequestDO::getCode)
                .collect(Collectors.joining("、"));
        LogRecordContext.putVariable("codes", codes);

        // 检查参数是否为空
        if (ids.isEmpty()) {
            throw exception(FIRST_MILE_REQUEST_NOT_EXISTS, ids);
        }

        // 查询所有记录
        List<TmsFirstMileRequestDO> requestDOList = firstMileRequestMapper.selectByIds(ids);

        // 找出不存在的记录ID
        List<Long> existingIds = requestDOList.stream().map(TmsFirstMileRequestDO::getId).toList();
        List<Long> notExistIds = ids.stream().filter(id -> !existingIds.contains(id)).collect(Collectors.toList());

        // 如果有不存在的记录，抛出异常
        if (!notExistIds.isEmpty()) {
            throw exception(FIRST_MILE_REQUEST_NOT_EXISTS, notExistIds);
        }

        //关闭状态不能提审
        requestDOList.forEach(requestDO -> {
            if (!Objects.equals(requestDO.getOffStatus(), TmsOffStatus.OPEN.getCode())) {
                throw exception(FIRST_MILE_REQUEST_OFF_STATUS_NOT_ALLOWED, requestDO.getCode(), TmsOffStatus.fromCode(requestDO.getOffStatus()).getDesc());
            }
        });
        // 批量执行状态转换
        for (TmsFirstMileRequestDO requestDO : requestDOList) {
            TmsFirstMileRequestAuditReqVO auditReqVO = TmsFirstMileRequestAuditReqVO.builder().requestId(requestDO.getId()).build();
            tmsFirstMileRequestStatusMachine.fireEvent(TmsAuditStatus.DRAFT, TmsEventEnum.SUBMIT_FOR_REVIEW, auditReqVO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_AUDIT_SUB_TYPE,
            bizNo = "{{#reqVO.id}}",
            success = "{{#reqVO.reviewed ? (#reqVO.pass ? '审核通过' : '审核不通过') : '反审核'}}了头程申请单【{{#vo.code}}】")
    public void review(TmsFirstMileRequestAuditReqVO reqVO) {
        // 获取头程申请单信息，用于记录日志
        TmsFirstMileRequestDO request = validateFirstMileRequestExists(reqVO.getRequestId());
        // 记录操作日志上下文
        LogRecordContext.putVariable("vo", request);

        // 检查参数是否为空
        if (reqVO.getRequestId() == null) {
            throw exception(FIRST_MILE_REQUEST_NOT_EXISTS, reqVO);
        }
        // 关闭状态不能审核/反审核
        if (!Objects.equals(request.getOffStatus(), TmsOffStatus.OPEN.getCode())) {
            throw exception(FIRST_MILE_REQUEST_OFF_STATUS_NOT_ALLOWED, request.getCode(), TmsOffStatus.fromCode(request.getOffStatus()).getDesc());
        }
        TmsAuditStatus currentStatus = TmsAuditStatus.fromCode(request.getAuditStatus());
        if (Boolean.TRUE.equals(reqVO.getReviewed())) {
            // 审核通过
            tmsFirstMileRequestStatusMachine.fireEvent(currentStatus, TmsEventEnum.AGREE, reqVO);
        } else {
            //如果存在对应头程明细关联,则不允许反审核
            validHasFirstMileItem(reqVO.getRequestId(), FIRST_MILE_REQUEST_ITEM_RELATION_NOT_ALLOWED);
            // 审核拒绝或反审核
            tmsFirstMileRequestStatusMachine.fireEvent(currentStatus, TmsEventEnum.REJECT, reqVO);
        }
    }

    /**
     * 验证头程单申请单ID是否存在关联头程明细
     *
     * @param requestId 头程申请单ID
     */
    private void validHasFirstMileItem(Long requestId, ErrorCode errorCode) {
        List<TmsFirstMileRequestItemDO> tmsFirstMileRequestItemDOS = firstMileRequestItemMapper.selectListByRequestId(requestId);
        tmsFirstMileRequestItemDOS.forEach(item -> {
            //  判断是否存在关联
            List<TmsFirstMileItemDO> firstMileItemDOList = firstMileService.getFirstMileItemListByRequestItemId(item.getId());
            if (firstMileItemDOList != null && !firstMileItemDOList.isEmpty()) {
                throw exception(errorCode, item.getId());
            }
        });
    }

    /**
     * 计算主表的总重量和总体积
     *
     * @param firstMileRequest 主表对象
     * @param requestItemDOS   子表对象列表
     */
    private void calculateTotalWeightAndVolume(TmsFirstMileRequestDO firstMileRequest, List<TmsFirstMileRequestItemDO> requestItemDOS) {
        if (requestItemDOS == null || requestItemDOS.isEmpty()) {
            firstMileRequest.setTotalWeight(BigDecimal.ZERO);
            firstMileRequest.setTotalVolume(BigDecimal.ZERO);
//            firstMileRequest.setNetWeight(BigDecimal.ZERO);
            return;
        }

        // 计算总重量和总体积
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;
        BigDecimal netWeight = BigDecimal.ZERO;

        for (TmsFirstMileRequestItemDO item : requestItemDOS) {
            // 获取产品信息
            ErpProductDTO product = erpProductApi.getProductDto(item.getProductId());
            if (product == null) {
                continue;
            }

            // 设置明细项的包装长宽高
            item.setPackageLength(BigDecimal.valueOf(product.getPackageLength()));
            item.setPackageWidth(BigDecimal.valueOf(product.getPackageWidth()));
            item.setPackageHeight(BigDecimal.valueOf(product.getPackageHeight()));
            item.setPackageWeight(product.getWeight());

            // 累加毛重和净重
            if (product.getWeight() != null && item.getQty() != null) {
                totalWeight = totalWeight.add(product.getWeight().multiply(BigDecimal.valueOf(item.getQty())));
                // 累加净重 = 包装重量 * 数量
                netWeight = netWeight.add(product.getPackageWeight().multiply(BigDecimal.valueOf(item.getQty())));
            }

            // 计算单个物品的体积（长*宽*高）并乘以数量
            if (product.getPackageLength() != null && product.getPackageWidth() != null && product.getPackageHeight() != null && item.getQty() != null) {
                BigDecimal itemVolume = BigDecimal.valueOf(product.getPackageLength())
                    .multiply(BigDecimal.valueOf(product.getPackageWidth()))
                    .multiply(BigDecimal.valueOf(product.getPackageHeight()))
                    .multiply(BigDecimal.valueOf(item.getQty()));
                // 设置明细项的体积
                item.setVolume(itemVolume);
                totalVolume = totalVolume.add(itemVolume);
            }
        }

        firstMileRequest.setTotalWeight(totalWeight);
        firstMileRequest.setTotalVolume(totalVolume);
//        firstMileRequest.setNetWeight(netWeight);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchTmsFirstMileOpenStatus(List<Long> itemIds, Boolean enable) {
        // 检查参数是否为空
        if (itemIds == null || itemIds.isEmpty()) {
            throw exception(FIRST_MILE_REQUEST_ITEM_NOT_EXISTS);
        }

        // 查询所有记录
        List<TmsFirstMileRequestItemDO> requestItemDOList = firstMileRequestItemMapper.selectByIds(itemIds);

        // 找出不存在的记录ID
        List<Long> existingIds = requestItemDOList.stream().map(TmsFirstMileRequestItemDO::getId).toList();
        List<Long> notExistIds = itemIds.stream().filter(id -> !existingIds.contains(id)).toList();

        // 如果有不存在的记录，抛出异常
        if (!notExistIds.isEmpty()) {
            throw exception(FIRST_MILE_REQUEST_ITEM_NOT_EXISTS, notExistIds);
        }

        requestItemDOList.forEach(item -> {
            if (enable) {
                offItemStatusMachine.fireEvent(TmsOffStatus.fromCode(item.getOffStatus()), TmsEventEnum.ACTIVATE, item);
            } else {
                offItemStatusMachine.fireEvent(TmsOffStatus.fromCode(item.getOffStatus()), TmsEventEnum.MANUAL_CLOSE, item);
            }
        });

    }

    @Override
    public String getLatestCode() {
        return tmsNoRedisDAO.getMaxSerial(FIRST_MILE_REQUEST_NO_PREFIX, FIRST_MILE_REQUEST_CODE_GENERATE_FAIL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_REQUEST_UPDATE_SUB_TYPE,
            bizNo = "{{#vo.id}}",
            success = "更新了头程申请单【{{#vo.code}}】: {_DIFF{#vo}}")
    public void updateFirstMileRequest(@Validated TmsFirstMileRequestSaveReqVO vo) {
        // 校验存在
        TmsFirstMileRequestDO oldDo = validateFirstMileRequestExists(vo.getId());
        TmsFirstMileRequestDO updateObj = BeanUtils.toBean(vo, TmsFirstMileRequestDO.class);

        if (!Objects.equals(oldDo.getCode(), vo.getCode())) {
            validCodeDateIsToday(vo.getCode());
            if (validCodeDuplicate(vo.getCode())) {
                throw exception(FIRST_MILE_CODE_DUPLICATE, vo.getCode());
            }
            tmsNoRedisDAO.setManualSerial(FIRST_MILE_REQUEST_NO_PREFIX, vo.getCode());
        } else {
            vo.setCode(tmsNoRedisDAO.generate(FIRST_MILE_REQUEST_NO_PREFIX, FIRST_MILE_REQUEST_CREATE_FAIL));
        }

        if (!Objects.equals(vo.getRequesterId(), oldDo.getRequesterId())) {
            adminUserApi.validateUser(vo.getRequesterId());
        }
        if (!Objects.equals(vo.getRequestDeptId(), oldDo.getRequestDeptId())) {
            deptApi.validateDeptList(Collections.singleton(vo.getRequestDeptId()));
        }
        if (!Objects.equals(vo.getToWarehouseId(), oldDo.getToWarehouseId())) {
            wmsWarehouseApi.validWarehouseList(Collections.singleton(vo.getToWarehouseId()));
        }
        statusCheckForUpdate(oldDo, FIRST_MILE_REQUEST_UPDATE_FAIL_APPROVE);

        // 计算主表的总重量和总体积
        List<TmsFirstMileRequestItemDO> requestItemDOS = TmsFirstMileRequestConvert.convertItemList(vo.getItems());
        //校验产品是否存在
        erpProductApi.validProductList(requestItemDOS.stream().map(TmsFirstMileRequestItemDO::getProductId).distinct().toList());
        calculateTotalWeightAndVolume(updateObj, requestItemDOS);

        firstMileRequestMapper.updateById(updateObj);

        // 更新子表
        firstMileRequestItemService.updateFirstMileRequestItemList(vo.getId(), requestItemDOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long mergeFirstMileRequest(TmsFirstMileSaveReqVO createReqVO) {
        return firstMileService.createFirstMile(createReqVO);
    }

    /**
     * 获取头程申请表明细列表MAP
     *
     * @param requestIds ids
     * @return map
     */
    @Override
    public Map<Long, TmsFirstMileRequestItemDO> getFirstMileRequestItemListMap(List<Long> requestIds) {
        List<TmsFirstMileRequestItemDO> requestItemDOList = firstMileRequestItemMapper.selectListByRequestIds(requestIds);
        return requestItemDOList.stream().collect(Collectors.toMap(TmsFirstMileRequestItemDO::getId, Function.identity()));
    }
}