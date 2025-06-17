package cn.iocoder.yudao.module.tms.service.first.mile.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.fms.api.finance.FmsCompanyApi;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.tms.api.first.mile.dto.TmsFistMileItemUpdateDTO;
import cn.iocoder.yudao.module.tms.api.first.mile.dto.TmsFistMileUpdateDTO;
import cn.iocoder.yudao.module.tms.api.first.mile.request.TmsFistMileRequestItemDTO;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo.TmsFirstMileItemRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.item.vo.TmsFirstMileItemSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileAuditReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMilePageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.req.TmsFirstMileSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp.TmsFirstMileRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.first.mile.vo.resp.TmsFirstMileStockRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.vessel.tracking.vo.TmsVesselTrackingSaveReqVO;
import cn.iocoder.yudao.module.tms.convert.first.mile.TmsFirstMileConvert;
import cn.iocoder.yudao.module.tms.dal.dataobject.fee.TmsFeeDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.TmsFirstMileDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item.TmsFirstMileItemDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import cn.iocoder.yudao.module.tms.dal.mysql.first.mile.TmsFirstMileMapper;
import cn.iocoder.yudao.module.tms.dal.mysql.first.mile.item.TmsFirstMileItemMapper;
import cn.iocoder.yudao.module.tms.dal.mysql.first.mile.request.TmsFirstMileRequestMapper;
import cn.iocoder.yudao.module.tms.dal.mysql.first.mile.request.item.TmsFirstMileRequestItemMapper;
import cn.iocoder.yudao.module.tms.dal.redis.no.TmsNoRedisDAO;
import cn.iocoder.yudao.module.tms.enums.TmsDictTypeConstants;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.TmsLogRecordConstants;
import cn.iocoder.yudao.module.tms.enums.status.TmsAuditStatus;
import cn.iocoder.yudao.module.tms.enums.status.TmsOrderStatus;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileBO;
import cn.iocoder.yudao.module.tms.service.bo.TmsFirstMileItemBO;
import cn.iocoder.yudao.module.tms.service.fee.TmsFeeService;
import cn.iocoder.yudao.module.tms.service.first.mile.TmsFirstMileService;
import cn.iocoder.yudao.module.tms.service.first.mile.request.TmsFirstMileRequestService;
import cn.iocoder.yudao.module.tms.service.port.info.TmsPortInfoService;
import cn.iocoder.yudao.module.tms.service.vessel.tracking.TmsVesselTrackingService;
import cn.iocoder.yudao.module.wms.api.inbound.item.WmsInboundItemApi;
import cn.iocoder.yudao.module.wms.api.inbound.item.dto.WmsInboundItemBinDTO;
import cn.iocoder.yudao.module.wms.api.outbound.WmsOutboundApi;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundDTO;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundImportReqDTO;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundItemSaveReqDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.WmsWarehouseApi;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.*;
import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.FIRST_MILE_AUDIT_STATE_MACHINE;
import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.FIRST_MILE_REQUEST_ITEM_ORDER_STATE_MACHINE;
import static cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundType.FIRST_MILE;
import static jodd.util.StringUtil.truncate;

/**
 * 头程单 Service 实现类
 *
 * @author wdy
 */
@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TmsFirstMileServiceImpl implements TmsFirstMileService {

    public static final Integer First_MILE_SOURCE_TYPE = BillType.TMS_FIRST_MILE.getValue();
    private final TmsFirstMileRequestMapper firstMileRequestMapper;
    private final TmsFirstMileRequestItemMapper firstMileRequestItemMapper;
    private final TmsFirstMileMapper firstMileMapper;
    private final TmsFirstMileItemMapper firstMileItemMapper;
    private final TmsFeeService feeService;
    private final TmsVesselTrackingService tmsVesselTrackingService;
    private final TmsNoRedisDAO noRedisDAO;
    private final WmsWarehouseApi warehouseApi;
    private final WmsOutboundApi wmsOutboundApi;
    private final ErpProductApi erpProductApi;
    private final WmsInboundItemApi wmsInboundItemApi;
    private final TmsPortInfoService tmsPortInfoService;
    private final DictDataApi dictDataApi;
    @Autowired
    @Lazy
    TmsFirstMileRequestService tmsFirstMileRequestService;

    @Resource(name = FIRST_MILE_AUDIT_STATE_MACHINE)
    StateMachine<TmsAuditStatus, TmsEventEnum, TmsFirstMileAuditReqVO> auditStateMachine;
    @Resource(name = FIRST_MILE_REQUEST_ITEM_ORDER_STATE_MACHINE)
    StateMachine<TmsOrderStatus, TmsEventEnum, TmsFistMileRequestItemDTO> requestItemOrderStateMachine;
    @Autowired
    private FmsCompanyApi fmsCompanyApi;

    //校验code中间日期是否是当天
    private static void validCodeDateIsToday(TmsFirstMileSaveReqVO vo) {
        String[] parts = vo.getCode().split("-");
        if (parts.length != 3) {
            throw exception(FIRST_MILE_CODE_FORMAT_ERROR, vo.getCode());
        }
        String dateStr = parts[1];
        String today = DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
        if (!dateStr.equals(today)) {
            throw exception(FIRST_MILE_CODE_DATE_NOT_TODAY, dateStr);
        }
    }

    /**
     * 校验头程单编号
     *
     * @param code      编号
     * @param excludeId 排除的ID（更新时使用）
     */
    private void validateFirstMileCode(String code, Long excludeId) {
        // 1. 校验编号是否重复
        boolean isDuplicate = excludeId != null ? validCodeDuplicate(code, excludeId) : validCodeDuplicate(code);
        if (isDuplicate) {
            throw exception(FIRST_MILE_CODE_DUPLICATE, code);
        }

        // 2. 校验编号格式
        String pattern = "^" + TmsNoRedisDAO.FIRST_MILE_NO_PREFIX + "-\\d{8}-[0-8]\\d{5}$";
        if (code.matches(pattern)) {
            // 如果符合格式，设置最大序号
            noRedisDAO.setManualSerial(TmsNoRedisDAO.FIRST_MILE_NO_PREFIX, code);
        }
    }

    @Override
    @Idempotent
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_CREATE_SUB_TYPE,
        bizNo = "{{#id}}",
        success = "创建了头程单【{{#vo.code}}】")
    public Long createFirstMile(TmsFirstMileSaveReqVO vo) {
        //1.0 校验
        validateFirstMile(vo, null);

        if (vo.getCode() != null) {
            validateFirstMileCode(vo.getCode(), null);
        } else {
            vo.setCode(noRedisDAO.generate(TmsNoRedisDAO.FIRST_MILE_NO_PREFIX, FIRST_MILE_CODE_GENERATE_FAIL));
        }

        TmsFirstMileDO firstMile = BeanUtils.toBean(vo, TmsFirstMileDO.class);
        firstMileMapper.insert(firstMile);
        Long firstMileId = firstMile.getId();

        //2.0 保存头程明细
        createFirstMileItemList(firstMileId, vo.getFirstMileItems());

        //3.0 保存费用项
        if (vo.getFees() != null) {
            createFeeList(firstMileId, BeanUtils.toBean(vo.getFees(), TmsFeeSaveReqVO.class));
        }

        //4.0 保存船期信息
        if (vo.getVesselTracking() != null) {
            try {
                tmsVesselTrackingService.createVesselTracking(BeanUtils.toBean(vo.getVesselTracking(), TmsVesselTrackingSaveReqVO.class, peek -> {
                    peek.setUpstreamId(firstMileId);
                    peek.setUpstreamType(First_MILE_SOURCE_TYPE);
                }));
            } catch (Exception e) {
                throw exception(FIRST_MILE_CREATE_FAIL, truncate(e.getMessage(), 200));
            }
        }

        auditStateMachine.fireEvent(TmsAuditStatus.DRAFT, TmsEventEnum.AUDIT_INIT, TmsFirstMileAuditReqVO.builder().id(firstMileId).build());
        //
        LogRecordContext.putVariable("id", firstMileId);
        return firstMileId;
    }

    /**
     * 校验头程单参数
     *
     * @param vo           头程单参数
     * @param oldFirstMile 原头程单（更新时使用）
     */
    private void validateFirstMile(TmsFirstMileSaveReqVO vo, TmsFirstMileDO oldFirstMile) {
        // 1. 校验仓库
        Set<Long> warehouseIds = vo.getFirstMileItems().stream().flatMap(item -> Stream.of(item.getFromWarehouseId())).collect(Collectors.toSet());
        warehouseIds.addAll(Stream.of(vo.getToWarehouseId()).collect(Collectors.toSet()));
        warehouseApi.validWarehouseList(warehouseIds);
        // 2. 校验头程单明细
        validateFirstMileItems(vo.getFirstMileItems());
        // 3. 校验港口信息/admin-api/tms/first-mile/create
        validateVesselTrackingPorts(vo.getVesselTracking());
        // 4. 校验货柜类型
        Optional.ofNullable(vo.getCabinetType()).ifPresent(i -> dictDataApi.validateDictDataList(TmsDictTypeConstants.TMS_LOGISTIC_TYPE, Collections.singleton(String.valueOf(i))));
        // 5. 校验公司
        Set<Long> mainCompanyIds = Stream.of(vo.getSalesCompanyId(), vo.getExportCompanyId(), vo.getTransitCompanyId()).collect(Collectors.toSet());
        Set<Long> companyIds = vo.getFirstMileItems().stream().flatMap(item -> Stream.of(item.getSalesCompanyId(), item.getCompanyId())).collect(Collectors.toSet());
        mainCompanyIds.addAll(companyIds);
//        fmsCompanyApi.validateCompany(companyIds.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    /**
     * 校验目标仓库
     *
     * @param toWarehouseId 目标仓库ID
     */
    private void validateToWarehouse(Long toWarehouseId) {
        warehouseApi.validWarehouseList(Collections.singleton(toWarehouseId));
    }

    /**
     * 校验头程单明细
     *
     * @param items 明细列表
     */
    private void validateFirstMileItems(List<TmsFirstMileItemSaveReqVO> items) {
        // 1. 校验明细列表非空
        if (CollUtil.isEmpty(items)) {
            throw exception(FIRST_MILE_ITEM_NOT_EXISTS);
        }

        // 2. 校验明细中的仓库ID和销售公司ID
        Set<Long> warehouseIds = new HashSet<>();
        Set<Long> companyIds = new HashSet<>();
        items.forEach(item -> {
            if (item.getFromWarehouseId() != null) {
                warehouseIds.add(item.getFromWarehouseId());
            }
            if (item.getCompanyId() != null) {
                companyIds.add(item.getCompanyId());
            }
        });

        // 校验仓库列表
        if (!warehouseIds.isEmpty()) {
            warehouseApi.validWarehouseList(warehouseIds);
        }

        // 校验销售公司列表
        if (!companyIds.isEmpty()) {
            fmsCompanyApi.validateCompany(companyIds);
        }
    }

    /**
     * 校验船运信息中的港口
     *
     * @param vesselTracking 船运信息
     */
    private void validateVesselTrackingPorts(TmsVesselTrackingSaveReqVO vesselTracking) {
        if (vesselTracking != null) {
            Set<Long> portIds = Stream.of(
                    vesselTracking.getFromPort(),
                    vesselTracking.getToPort(),
                    vesselTracking.getTransitPort()
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            tmsPortInfoService.validatePortInfoExistsList(portIds);
        }
    }

    //草稿+审核不通过才能修改
    private void statusCheckForEdit(TmsFirstMileDO tmsFirstMileDO, ErrorCode errorCode) {
        // 只有草稿状态或审核不通过状态才能修改
        if (tmsFirstMileDO.getAuditStatus() != null) {
            // 如果不是草稿状态或审核不通过状态，则抛出异常
            if (!TmsAuditStatus.DRAFT.getCode().equals(tmsFirstMileDO.getAuditStatus()) && !TmsAuditStatus.REJECTED.getCode().equals(tmsFirstMileDO.getAuditStatus())) {
                throw exception(errorCode, tmsFirstMileDO.getCode(), TmsAuditStatus.fromCode(tmsFirstMileDO.getAuditStatus()).getDesc());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_UPDATE_SUB_TYPE,
        bizNo = "{{#vo.id}}",
        success = "更新了头程单【{{#vo.code}}】: {_DIFF{#vo}}")
    public void updateFirstMile(TmsFirstMileSaveReqVO vo) {
        vo.initId(); //初始化上游ID
        TmsFirstMileDO tmsFirstMileDO = validateFirstMileExists(vo.getId());

        //校验code
        if (!Objects.equals(vo.getCode(), tmsFirstMileDO.getCode())) {
            validateFirstMileCode(vo.getCode(), vo.getId());
        }
        //判断头程单主子表是否改变
        if (this.checkTmsFirstMileChanged(vo, tmsFirstMileDO)) {
            statusCheckForEdit(tmsFirstMileDO, FIRST_MILE_UPDATE_FAIL_APPROVE);
        }
        //校验更新参数
        validateFirstMile(vo, tmsFirstMileDO);

        //1.0 更新头程单
        TmsFirstMileDO updateObj = BeanUtils.toBean(vo, TmsFirstMileDO.class);

        // 计算主表的总数量、总重量和总体积
        List<TmsFirstMileItemDO> items = BeanUtils.toBean(vo.getFirstMileItems(), TmsFirstMileItemDO.class);

        firstMileMapper.updateById(updateObj);

        //2.0 更新头程单子表
        updateFirstMileItemList(vo.getId(), vo.getFirstMileItems());

        //3.0 更新头程单费用子表
        if (vo.getFees() != null) {
            feeService.updateFeeList(vo.getId(), First_MILE_SOURCE_TYPE, vo.getFees());
        } else {
            // 如果费用列表为null，删除所有相关费用记录
            feeService.deleteFeeListBySourceIdAndSourceType(vo.getId(), First_MILE_SOURCE_TYPE);
        }

        //4.0 更新船运信息
        if (vo.getVesselTracking() != null) {
            tmsVesselTrackingService.updateVesselTracking(BeanUtils.toBean(vo.getVesselTracking(), TmsVesselTrackingSaveReqVO.class, peek -> peek.setUpstreamType(First_MILE_SOURCE_TYPE)));
        } else {
            // 如果船运信息为null，删除相关船运记录
            tmsVesselTrackingService.deleteVesselTracking(vo.getId(), First_MILE_SOURCE_TYPE);
        }
    }


    private boolean checkTmsFirstMileChanged(TmsFirstMileSaveReqVO vo, TmsFirstMileDO tmsFirstMileDO) {
        TmsFirstMileService tmsFirstMileService = SpringUtils.getBean(TmsFirstMileService.class);
        TmsFirstMileBO firstMileBO = tmsFirstMileService.getFirstMileBO(tmsFirstMileDO.getId());

        // 主表比对
        TmsFirstMileSaveReqVO oldVO = BeanUtils.toBean(firstMileBO, TmsFirstMileSaveReqVO.class);
        TmsFirstMileSaveReqVO newVO = BeanUtils.toBean(vo, TmsFirstMileSaveReqVO.class);
        if (!isSameMainData(oldVO, newVO)) {
            return true;
        }

        // 子表比对
        List<TmsFirstMileItemDO> oldItems = firstMileBO.getItems();
        List<TmsFirstMileItemSaveReqVO> newItems = vo.getFirstMileItems();

        // 数量不一致
        if (oldItems.size() != newItems.size()) {
            return true;
        }

        Map<Long, TmsFirstMileItemSaveReqVO> newItemMap = newItems.stream()
            .filter(item -> item.getId() != null)
            .collect(Collectors.toMap(TmsFirstMileItemSaveReqVO::getId, Function.identity()));

        for (TmsFirstMileItemDO oldItem : oldItems) {
            TmsFirstMileItemSaveReqVO newItem = newItemMap.get(oldItem.getId());
            if (newItem == null || !isSameItemData(oldItem, newItem)) {
                return true;
            }
        }

        // 新增的子项（ID 为 null）→ 新增
        return newItems.stream().anyMatch(item -> item.getId() == null);
    }

    private boolean isSameMainData(TmsFirstMileSaveReqVO oldVO, TmsFirstMileSaveReqVO newVO) {
        return Objects.equals(oldVO.getCode(), newVO.getCode()) &&
            Objects.equals(oldVO.getBillTime(), newVO.getBillTime()) &&
            Objects.equals(oldVO.getCarrierId(), newVO.getCarrierId()) &&
            Objects.equals(oldVO.getSettlementDate(), newVO.getSettlementDate()) &&
            Objects.equals(oldVO.getBalance(), newVO.getBalance()) &&
            Objects.equals(oldVO.getToWarehouseId(), newVO.getToWarehouseId()) &&
            Objects.equals(oldVO.getCabinetType(), newVO.getCabinetType()) &&
            Objects.equals(oldVO.getPackTime(), newVO.getPackTime()) &&
            Objects.equals(oldVO.getSalesCompanyId(), newVO.getSalesCompanyId()) &&
            Objects.equals(oldVO.getArrivePlanTime(), newVO.getArrivePlanTime()) &&
            Objects.equals(oldVO.getRemark(), newVO.getRemark()) &&
            Objects.equals(oldVO.getExportCompanyId(), newVO.getExportCompanyId()) &&
            Objects.equals(oldVO.getTransitCompanyId(), newVO.getTransitCompanyId()) &&
            Objects.equals(oldVO.getRevision(), newVO.getRevision());
    }

    private boolean isSameItemData(TmsFirstMileItemDO oldItem, TmsFirstMileItemSaveReqVO newItem) {
        return Objects.equals(oldItem.getRequestItemId(), newItem.getRequestItemId()) &&
            Objects.equals(oldItem.getProductId(), newItem.getProductId()) &&
            Objects.equals(oldItem.getFbaBarCode(), newItem.getFbaBarCode()) &&
            Objects.equals(oldItem.getQty(), newItem.getQty()) &&
            Objects.equals(oldItem.getBoxQty(), newItem.getBoxQty()) &&
            Objects.equals(oldItem.getCompanyId(), newItem.getCompanyId()) &&
            Objects.equals(oldItem.getDeptId(), newItem.getDeptId()) &&
            Objects.equals(oldItem.getRemark(), newItem.getRemark()) &&
            Objects.equals(oldItem.getOutboundPlanQty(), newItem.getOutboundPlanQty()) &&
            Objects.equals(oldItem.getFromWarehouseId(), newItem.getFromWarehouseId()) &&
            Objects.equals(oldItem.getSalesCompanyId(), newItem.getSalesCompanyId()) &&
            Objects.equals(oldItem.getRevision(), newItem.getRevision());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_DELETE_SUB_TYPE,
        bizNo = "{{#id}}",
        success = "删除了头程单【{{#code}}】")
    public void deleteFirstMile(Long id) {
        TmsFirstMileDO tmsFirstMileDO = validateFirstMileExists(id);
        statusCheckForEdit(tmsFirstMileDO, FIRST_MILE_DELETE_FAIL_APPROVE);
        firstMileMapper.deleteById(id);
        //删明细
        deleteFirstMileItemByFirstMileId(id);
        //删费用
        feeService.deleteFee(id, First_MILE_SOURCE_TYPE);
        //删除船运？(是否需要？)
        //log
        LogRecordContext.putVariable("code", tmsFirstMileDO.getCode());
    }

    /**
     * 校验是否存在
     *
     * @param id id
     * @return tmsFirstMileDO
     */
    @Override
    public TmsFirstMileDO validateFirstMileExists(Long id) {
        TmsFirstMileDO tmsFirstMileDO = firstMileMapper.selectById(id);
        if (tmsFirstMileDO == null) {
            throw exception(FIRST_MILE_NOT_EXISTS, id);
        }
        return tmsFirstMileDO;
    }

    @Override
    public TmsFirstMileDO getFirstMile(Long id) {
        return firstMileMapper.selectById(id);
    }

    /**
     * 获得头程单BO
     *
     * @param id 头程单id
     */
    @Override
    public TmsFirstMileBO getFirstMileBO(Long id) {
        TmsFirstMileDO tmsFirstMileDO = firstMileMapper.selectById(id);
        TmsFirstMileBO bo = BeanUtils.toBean(tmsFirstMileDO, TmsFirstMileBO.class);
        bo.setItems(firstMileItemMapper.selectListByFirstMileId(id));
        //跟踪信息
        bo.setTracking(tmsVesselTrackingService.getVesselTrackingByUpstreamIdAndUpstreamType(id, First_MILE_SOURCE_TYPE));
        //fees
        bo.setFees(feeService.getFee(id, First_MILE_SOURCE_TYPE));
        return bo;
    }

    @Override
    public PageResult<TmsFirstMileBO> getFirstMileBOPage(TmsFirstMilePageReqVO pageReqVO) {
        PageResult<TmsFirstMileItemBO> itemPageResult = firstMileItemMapper.selectPageBO(pageReqVO);
        if (itemPageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), itemPageResult.getTotal());
        }
        List<TmsFirstMileBO> firstMileBOList = TmsFirstMileConvert.convertBOList(itemPageResult.getList());
        // 获取费用(费用不分页展示，详情取)
        return new PageResult<>(firstMileBOList, itemPageResult.getTotal());
    }

    @Override
    public String getLatestCode() {
        return noRedisDAO.getMaxSerial(TmsNoRedisDAO.FIRST_MILE_NO_PREFIX, FIRST_MILE_CODE_GENERATE_FAIL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_SUBMIT_AUDIT_SUB_TYPE,
        bizNo = "{{#ids[0]}}",
        success = "提交了头程单【{{#codes}}】审核")
    public void submitAudit(List<Long> ids) {
        // 1. 获取头程单信息，用于记录日志
        List<TmsFirstMileDO> firstMiles = ids.stream()
            .map(this::validateFirstMileExists)
            .toList();
        // 记录操作日志上下文
        String codes = firstMiles.stream()
            .map(TmsFirstMileDO::getCode)
            .collect(Collectors.joining("、"));
        LogRecordContext.putVariable("codes", codes);

        // 2. 更新状态
        // 检查参数是否为空
        if (ids.isEmpty()) {
            throw exception(FIRST_MILE_ID_NOT_EXISTS, ids);
        }

        // 查询所有记录
        List<TmsFirstMileDO> requestDOList = firstMileMapper.selectByIds(ids);
        // 找出不存在的记录ID
        List<Long> existingIds = requestDOList.stream().map(TmsFirstMileDO::getId).toList();
        List<Long> notExistIds = ids.stream().filter(id -> !existingIds.contains(id)).toList();

        // 如果有不存在的记录，抛出异常
        if (!notExistIds.isEmpty()) {
            throw exception(FIRST_MILE_ID_NOT_EXISTS, notExistIds);
        }

        // 批量执行状态转换
        for (TmsFirstMileDO firstMileDO : requestDOList) {
            TmsFirstMileAuditReqVO auditReqVO = TmsFirstMileAuditReqVO.builder().id(firstMileDO.getId()).build();
            auditStateMachine.fireEvent(TmsAuditStatus.DRAFT, TmsEventEnum.SUBMIT_FOR_REVIEW, auditReqVO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = TmsLogRecordConstants.TMS_FIRST_MILE_TYPE,
        subType = TmsLogRecordConstants.TMS_FIRST_MILE_AUDIT_SUB_TYPE,
        bizNo = "{{#reqVO.id}}",
        success = "{{#reqVO.reviewed ? (#reqVO.pass ? '审核通过' : '审核不通过') : '反审核'}}了头程单【{{#code}}】")
    public void review(TmsFirstMileAuditReqVO reqVO) {
        TmsFirstMileDO tmsFirstMileDO = validateFirstMileExists(reqVO.getId());
        if (Boolean.TRUE.equals(reqVO.getReviewed())) {
            if (reqVO.getPass()) {
                //1. 通过
                auditStateMachine.fireEvent(TmsAuditStatus.fromCode(tmsFirstMileDO.getAuditStatus()), TmsEventEnum.AGREE, reqVO);
                //2. 生成对应出库单
                TmsFirstMileBO tmsFirstMileBO = getFirstMileBO(reqVO.getId());
                //3.0 创建出库单,为每个仓库创建不同得出库单。
                this.createWmsOutbound(tmsFirstMileBO);
            } else {
                //不通过
                auditStateMachine.fireEvent(TmsAuditStatus.fromCode(tmsFirstMileDO.getAuditStatus()), TmsEventEnum.REJECT, reqVO);
            }
        } else {
            //反审核
            auditStateMachine.fireEvent(TmsAuditStatus.fromCode(tmsFirstMileDO.getAuditStatus()), TmsEventEnum.WITHDRAW_REVIEW, reqVO);
            //作废WMS出库单
            this.abandonWmsOutbound(tmsFirstMileDO.getId());
        }
        //
        LogRecordContext.putVariable("code", tmsFirstMileDO.getCode());
    }

    /**
     * 创建 WMS 出库单
     *
     * @param firstMileBO 头程单
     */
    private void createWmsOutbound(TmsFirstMileBO firstMileBO) {
        // 1. 按仓库分组头程项
        Map<Long, List<TmsFirstMileItemDO>> warehouseItemsMap = firstMileBO.getItems().stream()
            .collect(Collectors.groupingBy(item -> {
                if (item.getFromWarehouseId() == null) {
                    throw exception(FIRST_MILE_PROCESS_FAIL_WAREHOUSE_ID_DONT_EXISTS);
                }
                return item.getFromWarehouseId();
            }));

        // 2. 为每个仓库创建出库单
        try {
            warehouseItemsMap.forEach((warehouseId, items) -> {
                // 构建出库单基本信息
                WmsOutboundImportReqDTO importReqDTO = buildOutboundBaseInfo(firstMileBO);
                importReqDTO.setWarehouseId(warehouseId); // 设置仓库ID
                // 构建出库单明细信息，将相同仓库的头程项合并到一个出库单中
                importReqDTO.setItemList(buildOutboundItems(items));
                // 生成出库单
                log.debug("头程单生成出库单：{}", JsonUtils.toJsonString(importReqDTO));
                wmsOutboundApi.generateOutbound(importReqDTO);
            });
        } catch (Exception e) {
            throw exception(FIRST_MILE_PROCESS_FAIL_WMS_OUTBOUND_EXISTS, truncate(e.getMessage(), 200));
        }
    }

    /**
     * 构建出库单基本信息
     *
     * @param firstMileBO 头程单
     * @return 出库单基本信息
     */
    private WmsOutboundImportReqDTO buildOutboundBaseInfo(TmsFirstMileBO firstMileBO) {
        WmsOutboundImportReqDTO importReqDTO = new WmsOutboundImportReqDTO();
        importReqDTO.setType(FIRST_MILE.getValue()); // 订单出库
        importReqDTO.setUpstreamId(firstMileBO.getId()); // 来源单据ID
        importReqDTO.setUpstreamCode(firstMileBO.getCode()); // 来源单据编码
        importReqDTO.setUpstreamType(First_MILE_SOURCE_TYPE); // 来源单据类型
        importReqDTO.setRemark(firstMileBO.getRemark()); // 备注
        importReqDTO.setOutboundTime(firstMileBO.getOutboundTime()); // 出库时间
        importReqDTO.setCompanyId(firstMileBO.getExportCompanyId()); //  出口公司ID
        return importReqDTO;
    }

    /**
     * 构建出库单明细信息
     *
     * @param items 头程单明细
     * @return 出库单明细列表
     */
    private List<WmsOutboundItemSaveReqDTO> buildOutboundItems(List<TmsFirstMileItemDO> items) {
        return items.stream().map(item -> {
            WmsOutboundItemSaveReqDTO itemDTO = new WmsOutboundItemSaveReqDTO();
            itemDTO.setProductId(item.getProductId()); // 产品ID
            itemDTO.setPlanQty(item.getQty()); // 计划出库量
            itemDTO.setActualQty(item.getQty()); // 实际出库量
            itemDTO.setRemark(item.getRemark()); // 备注
            itemDTO.setUpstreamId(item.getId()); // 来源明细行ID
            itemDTO.setCompanyId(item.getCompanyId()); // 设置库存公司ID
            itemDTO.setDeptId(item.getDeptId()); //库存归属部门ID(哪个部门出库SKU)
            return itemDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 作废头程单关联的WMS出库单
     *
     * @param firstMileId 头程单ID
     */
    private void abandonWmsOutbound(Long firstMileId) {
        List<WmsOutboundDTO> dtoList = Optional.ofNullable(wmsOutboundApi.getOutboundList(First_MILE_SOURCE_TYPE, firstMileId))
            .orElse(Collections.emptyList());
        if (CollUtil.isEmpty(dtoList)) {
            return;
        }

        // 收集所有非草稿
        List<String> nonDraftCodes = dtoList.stream()
            .filter(dto -> !Objects.equals(dto.getAuditStatus(), WmsOutboundAuditStatus.DRAFT.getValue()))
            .map(WmsOutboundDTO::getCode)
            .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(nonDraftCodes)) {
            throw exception(FIRST_MILE_WMS_OUTBOUND_NOT_CAN_ABANDON, String.join(",", nonDraftCodes));
        }

        // 作废
        try {
            dtoList.forEach(dto -> wmsOutboundApi.abandonOutbound(dto.getId(), "头程单反审核"));
        } catch (Exception e) {
            throw exception(FEE_WMS_OUTBOUND_NOT_CAN_ABANDON, truncate(e.getMessage(), 200));
        }
    }

    // ==================== 子表（头程单明细） ====================
    @Override
    public List<TmsFirstMileItemDO> getFirstMileItemListByFirstMileId(Long firstMileId) {
        return firstMileItemMapper.selectListByFirstMileId(firstMileId);
    }

    /**
     * 通过申请项ID获得 头程明细列表
     *
     * @param requestItemId 申请项ID
     */
    @Override
    public List<TmsFirstMileItemDO> getFirstMileItemListByRequestItemId(Long requestItemId) {
        return firstMileItemMapper.selectListByRequestItemId(requestItemId);
    }

    @Override
    public void assembleTmsFirstMileStockRespVO(TmsFirstMileRespVO tmsFirstMileRespVO) {
        Long toWarehouseId = tmsFirstMileRespVO.getToWarehouseId();
        Set<Long> productIds = tmsFirstMileRespVO.getFirstMileItems().stream().map(TmsFirstMileItemRespVO::getProductId).collect(Collectors.toSet());
        Map<Long, List<WmsInboundItemBinDTO>> inboundItemBinMap = wmsInboundItemApi.getInboundItemBinMap(toWarehouseId, productIds, true);
        //companyId inboundItemBinMap
//        Set<Long> companyIds = inboundItemBinMap.values().stream().flatMap(List::stream).flatMap(item -> Stream.of(item.getCompanyId(), item.getInboundCompanyId())).collect(Collectors.toSet());
//        Map<Long, FmsCompanyDTO> companyMap = fmsCompanyApi.getCompanyMap(companyIds);
//        //
//        tmsFirstMileRespVO.getFirstMileItems().forEach(item ->
//            item.setStock(BeanUtils.toBean(inboundItemBinMap.get(item.getProductId()), TmsFirstMileStockRespVO.class, stock -> {
//                stock.setCompanyName(companyMap.get(stock.getCompanyId()).getName());
//                stock.setInboundCompanyName(companyMap.get(stock.getInboundCompanyId()).getName());
//            })));
        Set<Long> companyIds = inboundItemBinMap.values().stream()
            .flatMap(List::stream)
            .flatMap(item -> Stream.of(item.getCompanyId(), item.getInboundCompanyId()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Long, FmsCompanyDTO> companyMap = fmsCompanyApi.getCompanyMap(companyIds);

        tmsFirstMileRespVO.getFirstMileItems().forEach(item -> {
            TmsFirstMileStockRespVO stockVO = Optional.ofNullable(inboundItemBinMap.get(item.getProductId()))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0)) // 取第一个元素
                .map(source -> BeanUtils.toBean(source, TmsFirstMileStockRespVO.class))
                .orElse(new TmsFirstMileStockRespVO());

            stockVO.setCompanyName(
                Optional.ofNullable(stockVO.getCompanyId())
                    .map(companyId -> Optional.ofNullable(companyMap.get(companyId))
                        .map(FmsCompanyDTO::getName)
                        .orElse("未知公司"))
                    .orElse(null));

            stockVO.setInboundCompanyName(
                Optional.ofNullable(stockVO.getInboundCompanyId())
                    .map(companyId -> Optional.ofNullable(companyMap.get(companyId))
                        .map(FmsCompanyDTO::getName)
                        .orElse("未知公司"))
                    .orElse(null));

            item.setStock(Collections.singletonList(stockVO));
        });
    }

    /**
     * 设置产品快照信息
     *
     * @param itemList 头程单明细列表
     */
    private void setProductSnapshotInfo(List<TmsFirstMileItemDO> itemList) {
        if (CollUtil.isEmpty(itemList)) {
            return;
        }
        // 1. 获取产品信息
        final Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(itemList.stream()
            .map(TmsFirstMileItemDO::getProductId)
            .collect(Collectors.toSet()));
        // 2. 设置产品快照信息
        itemList.forEach(item -> {
            // 否则从产品信息中获取
            ErpProductDTO product = productMap.get(item.getProductId());
            if (product != null) {
                // 将Integer转换为BigDecimal
                item.setPackageLength(BigDecimal.valueOf(product.getPackageLength()));
                item.setPackageWidth(BigDecimal.valueOf(product.getPackageWidth()));
                item.setPackageHeight(BigDecimal.valueOf(product.getPackageHeight()));
                item.setPackageWeight(product.getPackageWeight());
                item.setWeight(product.getWeight());
            }
        });
    }

    private void createFirstMileItemList(Long firstMileId, List<TmsFirstMileItemSaveReqVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<TmsFirstMileItemDO> itemList = BeanUtils.toBean(list, TmsFirstMileItemDO.class);
        itemList.forEach(item -> item.setFirstMileId(firstMileId));

        // 设置产品快照信息
        setProductSnapshotInfo(itemList);
        firstMileItemMapper.insertBatch(itemList);
        //item如果存在关联，则联动
        syncClosedQty(itemList, true);
    }

    /**
     * 同步订购数量 -> 头程申请项
     *
     * @param itemList TmsFirstMileItemDO
     */
    private void syncClosedQty(List<TmsFirstMileItemDO> itemList, Boolean isAdd) {
        itemList.stream().filter(item -> item.getRequestItemId() != null).forEach(item -> {
            //拿到申请itemDO
            TmsFirstMileRequestItemDO firstMileRequestItemDO = tmsFirstMileRequestService.getFirstMileRequestItem(item.getRequestItemId());
            TmsFistMileRequestItemDTO dto = TmsFistMileRequestItemDTO.builder().itemId(item.getRequestItemId()).qty(item.getQty()).build();
            if (!isAdd) {
                dto.setQty(-dto.getQty());//取反
            }
            requestItemOrderStateMachine.fireEvent(TmsOrderStatus.fromCode(firstMileRequestItemDO.getOrderStatus())
                , TmsEventEnum.ORDER_ADJUSTMENT
                , dto);
        });
    }

    /**
     * @param firstMileId 上游ID
     * @param list        list
     */
    private void updateFirstMileItemList(Long firstMileId, List<TmsFirstMileItemSaveReqVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<TmsFirstMileItemDO> oldList = firstMileItemMapper.selectListByFirstMileId(firstMileId);
        List<TmsFirstMileItemDO> newList = BeanUtils.toBean(list, TmsFirstMileItemDO.class);

        List<List<TmsFirstMileItemDO>> diffedList = CollectionUtils.diffList(oldList, newList, (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

        if (CollUtil.isNotEmpty(diffedList.get(0))) {
            diffedList.get(0).forEach(item -> item.setFirstMileId(firstMileId));
            // 设置新增明细的产品快照信息
            setProductSnapshotInfo(diffedList.get(0));
            firstMileItemMapper.insertBatch(diffedList.get(0));
            syncClosedQty(diffedList.get(0), true);
        }
        if (CollUtil.isNotEmpty(diffedList.get(1))) {
            //批量查询MAP
            Map<Long, TmsFirstMileRequestItemDO> requestItemMap = tmsFirstMileRequestService.getFirstMileRequestItemListMap(
                diffedList.get(1).stream().filter(item -> item.getRequestItemId() != null).distinct().map(TmsFirstMileItemDO::getRequestItemId).toList());
            diffedList.get(1).stream().filter(item -> item.getRequestItemId() != null).forEach(item -> {
                TmsFirstMileRequestItemDO oldItemDO = requestItemMap.get(item.getRequestItemId());
                //变化的数量差
                Integer changeQty = item.getQty() - oldItemDO.getQty();
                requestItemOrderStateMachine.fireEvent(TmsOrderStatus.fromCode(oldItemDO.getOrderStatus())
                    , TmsEventEnum.ORDER_ADJUSTMENT
                    , TmsFistMileRequestItemDTO.builder().itemId(item.getRequestItemId()).qty(changeQty).build());
            });
            // 设置更新明细的产品快照信息
            setProductSnapshotInfo(diffedList.get(1));
            firstMileItemMapper.updateBatch(diffedList.get(1));
        }
        if (CollUtil.isNotEmpty(diffedList.get(2))) {
            List<Long> deleteIds = CollectionUtils.convertList(diffedList.get(2), TmsFirstMileItemDO::getId);
            syncClosedQty(diffedList.get(2), false);
            firstMileItemMapper.deleteByIds(deleteIds);
        }
    }

    private void deleteFirstMileItemByFirstMileId(Long firstMileId) {
        firstMileItemMapper.deleteByFirstMileId(firstMileId);
    }

    // ==================== 子表（出运订单费用明细） ====================

    @Override
    public List<TmsFeeRespVO> getFeeListBySourceId(Long sourceId) {
        List<TmsFeeDO> feeList = feeService.getFee(sourceId, First_MILE_SOURCE_TYPE);
        return BeanUtils.toBean(feeList, TmsFeeRespVO.class);
    }

    private boolean validCodeDuplicate(String code) {
        return firstMileMapper.selectByCode(code);
    }

    private boolean validCodeDuplicate(String code, Long excludeId) {
        TmsFirstMileDO exist = firstMileMapper.selectByCodeRaw(code);
        return exist != null && !exist.getId().equals(excludeId);
    }

    private void createFeeList(Long sourceId, List<TmsFeeSaveReqVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<TmsFeeDO> feeList = BeanUtils.toBean(list, TmsFeeDO.class, peek -> {
            peek.setUpstreamType(First_MILE_SOURCE_TYPE);
            peek.setUpstreamId(sourceId);
        });
        feeService.createFeeList(feeList, First_MILE_SOURCE_TYPE);
    }

    /**
     * 更新头程单状态
     *
     * @param dto dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFirstMileStatus(TmsFistMileUpdateDTO dto) {
        // 1. 校验头程单是否存在
        TmsFirstMileDO firstMile = validateFirstMileExists(dto.getId());

        // 2. 更新头程单状态
        TmsFirstMileDO updateObj = new TmsFirstMileDO();
        updateObj.setId(dto.getId());

        if (dto.getAuditStatus() != null) {
            updateObj.setAuditStatus(dto.getAuditStatus());
        }
        if (dto.getAuditAdvice() != null) {
            updateObj.setAuditAdvice(dto.getAuditAdvice());
        }

        // 2.2 设置出库信息
        if (dto.getOutboundStatus() != null) {
            updateObj.setOutboundStatus(dto.getOutboundStatus());
        }
        if (dto.getOutboundTime() != null) {
            updateObj.setOutboundTime(dto.getOutboundTime());
        }

        // 2.3 设置入库信息
        if (dto.getInboundStatus() != null) {
            updateObj.setInboundStatus(dto.getInboundStatus());
        }
        if (dto.getInboundTime() != null) {
            updateObj.setInboundTime(dto.getInboundTime());
        }

        // 3. 执行更新
        firstMileMapper.updateById(updateObj);
    }

    /**
     * 批量查询申请单MAP
     *
     * @param requestItemIds 申请项IDS
     * @return 申请单MAP
     */
    @Override
    public Map<Long, TmsFirstMileRequestDO> getRequestMap(Set<Long> requestItemIds) {
        if (CollectionUtils.isEmpty(requestItemIds)) {
            return Collections.emptyMap();
        }

        // 1. 查询子项信息，获取主单ID列表
        List<TmsFirstMileRequestItemDO> items = firstMileRequestItemMapper.selectByIds(requestItemIds);
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyMap();
        }

        // 2. 获取主单ID列表
        Set<Long> requestIds = items.stream()
            .map(TmsFirstMileRequestItemDO::getRequestId)
            .collect(Collectors.toSet());


        // 3. 查询主单信息
        List<TmsFirstMileRequestDO> requests = firstMileRequestMapper.selectByIds(requestIds);
        if (CollectionUtils.isEmpty(requests)) {
            return Collections.emptyMap();
        }

        // 4. 构建主单Map
        Map<Long, TmsFirstMileRequestDO> requestMap = requests.stream()
            .collect(Collectors.toMap(TmsFirstMileRequestDO::getId, request -> request));

        // 5. 构建子项ID到主单的映射
        return items.stream()
            .collect(Collectors.toMap(
                TmsFirstMileRequestItemDO::getId,
                item -> requestMap.get(item.getRequestId())
            ));
    }

    @Override
    public TmsFirstMileItemDO validateFirstMileItemExists(Long id) {
        TmsFirstMileItemDO item = firstMileItemMapper.selectById(id);
        if (item == null) {
            throw exception(FIRST_MILE_ITEM_NOT_EXISTS, id);
        }
        return item;
    }

    @Override
    public List<TmsFirstMileItemDO> validateFirstMileItemExists(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<TmsFirstMileItemDO> items = firstMileItemMapper.selectByIds(new HashSet<>(ids));
        if (items.size() != ids.size()) {
            // 找出不存在的ID
            Set<Long> existIds = items.stream().map(TmsFirstMileItemDO::getId).collect(Collectors.toSet());
            List<Long> notExistIds = ids.stream().filter(id -> !existIds.contains(id)).collect(Collectors.toList());
            throw exception(FIRST_MILE_ITEM_NOT_EXISTS, notExistIds);
        }
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFirstMileItemOutbound(TmsFistMileItemUpdateDTO dto) {
        // 1. 校验明细行是否存在
        TmsFirstMileItemDO item = validateFirstMileItemExists(dto.getId());

        // 2. 更新出库和入库数量
        TmsFirstMileItemDO updateObj = new TmsFirstMileItemDO();
        if (dto.getInOutType() == true) {
            updateObj.setId(dto.getId());
            updateObj.setInboundClosedQty(dto.getInboundQty());
        } else {
            updateObj.setId(dto.getId());
            updateObj.setOutboundClosedQty(dto.getOutboundQty());
        }

        // 3. 执行更新
        firstMileItemMapper.updateById(updateObj);
    }

}