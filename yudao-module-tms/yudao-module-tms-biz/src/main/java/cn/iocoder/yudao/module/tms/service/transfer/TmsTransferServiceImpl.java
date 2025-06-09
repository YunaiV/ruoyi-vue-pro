package cn.iocoder.yudao.module.tms.service.transfer;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.fms.api.finance.FmsCompanyApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsTransferStatusUpdateDTO;
import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsCompanyRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsDeptRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsProductRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.common.vo.TmsWarehourseRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.vo.*;
import cn.iocoder.yudao.module.tms.convert.transfer.TmsTransferConvert;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.TmsTransferDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.item.TmsTransferItemDO;
import cn.iocoder.yudao.module.tms.dal.mysql.transfer.TmsTransferMapper;
import cn.iocoder.yudao.module.tms.dal.mysql.transfer.item.TmsTransferItemMapper;
import cn.iocoder.yudao.module.tms.enums.TmsEventEnum;
import cn.iocoder.yudao.module.tms.enums.status.TmsAuditStatus;
import cn.iocoder.yudao.module.tms.service.bo.transfer.TmsTransferBO;
import cn.iocoder.yudao.module.tms.service.bo.transfer.TmsTransferItemBO;
import cn.iocoder.yudao.module.tms.service.transfer.item.TmsTransferItemService;
import cn.iocoder.yudao.module.wms.api.inbound.dto.WmsStockWarehouseSimpleDTO;
import cn.iocoder.yudao.module.wms.api.outbound.WmsOutboundApi;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundDTO;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundImportReqDTO;
import cn.iocoder.yudao.module.wms.api.outbound.dto.WmsOutboundItemSaveReqDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.WmsWarehouseApi;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseQueryDTO;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.WmsWarehouseSimpleDTO;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundType;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.*;
import static cn.iocoder.yudao.module.tms.enums.TmsStateMachines.TRANSFER_AUDIT_STATE_MACHINE;
import static cn.iocoder.yudao.module.tms.tool.TmsStreamXTool.assemble;
import static jodd.util.StringUtil.truncate;

/**
 * 调拨单 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TmsTransferServiceImpl implements TmsTransferService {

    private final TmsTransferMapper transferMapper;
    private final TmsTransferItemMapper tmsTransferItemMapper;
    private final TmsTransferItemService transferItemService;
    private final ErpProductApi erpProductApi;
    private final FmsCompanyApi fmsCompanyApi;
    private final WmsWarehouseApi wmsWarehouseApi;
    private final WmsOutboundApi wmsOutboundApi;
    private final AdminUserApi adminUserApi;

    @Resource(name = TRANSFER_AUDIT_STATE_MACHINE)
    private StateMachine<TmsAuditStatus, TmsEventEnum, TmsTransferAuditReqVO> transferAuditStateMachine;
    @Autowired
    private DeptApi deptApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransfer(TmsTransferSaveReqVO createReqVO) {
        // 1. 校验调拨单编码是否重复
        validateTransferCodeUnique(createReqVO.getCode(), null);

        // 2. 插入主表
        TmsTransferDO transfer = BeanUtils.toBean(createReqVO, TmsTransferDO.class);
        // 计算总数量、总重量等信息
        calculateTotalInfo(transfer, createReqVO.getItems());
        transferMapper.insert(transfer);

        // 2. 插入明细表
        createReqVO.getItems().forEach(item -> {
            item.setTransferId(transfer.getId());
            transferItemService.createTransferItem(item);
        });

        // 返回主表ID
        //设置初始状态
        transferAuditStateMachine.fireEvent(TmsAuditStatus.DRAFT, TmsEventEnum.AUDIT_INIT, new TmsTransferAuditReqVO().setId(transfer.getId()));
        return transfer.getId();
    }

    /**
     * 计算调拨单的总数量、总重量等信息
     *
     * @param transfer 调拨单
     * @param items    明细列表
     */
    private void calculateTotalInfo(TmsTransferDO transfer, List<TmsTransferItemSaveReqVO> items) {
        // 初始化总数量
        transfer.setTotalQty(0);
        // 初始化总重量
        transfer.setTotalWeight(BigDecimal.ZERO);
        // 初始化总体积
        transfer.setTotalVolume(BigDecimal.ZERO);

        if (CollUtil.isEmpty(items)) {
            return;
        }

        // 1. 获取所有产品ID
        List<Long> productIds = convertList(items, TmsTransferItemSaveReqVO::getProductId);
        if (CollUtil.isEmpty(productIds)) {
            return;
        }

        // 2. 批量获取产品信息
        Map<Long, ErpProductDTO> productMap = convertMap(erpProductApi.listProductDTOs(productIds), ErpProductDTO::getId);

        // 3. 计算总重量和总体积
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;

        for (TmsTransferItemSaveReqVO item : items) {
            ErpProductDTO product = productMap.get(item.getProductId());
            if (product != null) {
                // 累加总数量
                transfer.setTotalQty(transfer.getTotalQty() + item.getQty());

                // 计算单个产品的总重量 = 产品重量 * 数量
                if (product.getWeight() != null) {
                    totalWeight = totalWeight.add(
                        MoneyUtils.priceMultiply(product.getWeight(), new BigDecimal(item.getQty()))
                    );
                }

                // 计算单个产品的总体积 = (长 * 宽 * 高) * 数量（单位：立方毫米）
                if (product.getLength() != null && product.getWidth() != null && product.getHeight() != null) {
                    BigDecimal itemVolume = new BigDecimal(product.getLength())
                        .multiply(new BigDecimal(product.getWidth()))
                        .multiply(new BigDecimal(product.getHeight()));
                    totalVolume = totalVolume.add(
                        MoneyUtils.priceMultiply(itemVolume, new BigDecimal(item.getQty()))
                    );
                }
            }
        }

        // 4. 设置总重量和总体积
        transfer.setTotalWeight(totalWeight);
        transfer.setTotalVolume(totalVolume);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransfer(TmsTransferSaveReqVO updateReqVO) {
        // 校验存在
        TmsTransferDO transfer = validateTransferExists(updateReqVO.getId());

        // 校验调拨单编码是否重复
        validateTransferCodeUnique(updateReqVO.getCode(), updateReqVO.getId());

        // 1. 更新主表
        TmsTransferDO updateObj = BeanUtils.toBean(updateReqVO, TmsTransferDO.class);
        // 计算总数量、总重量等信息
        calculateTotalInfo(updateObj, updateReqVO.getItems());
        transferMapper.updateById(updateObj);

        // 2. 更新明细表
        if (CollUtil.isNotEmpty(updateReqVO.getItems())) {
            List<TmsTransferItemDO> oldList = transferItemService.getTransferItemListByTransferId(updateReqVO.getId());
            List<TmsTransferItemDO> newList = BeanUtils.toBean(updateReqVO.getItems(), TmsTransferItemDO.class);

            List<List<TmsTransferItemDO>> diffList = diffList(oldList, newList, (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));

            if (CollUtil.isNotEmpty(diffList.get(0))) {
                diffList.get(0).forEach(o -> o.setTransferId(updateReqVO.getId()));
                transferItemService.createTransferItemList(diffList.get(0));
            }
            if (CollUtil.isNotEmpty(diffList.get(1))) {
                transferItemService.updateTransferItemList(diffList.get(1));
            }
            if (CollUtil.isNotEmpty(diffList.get(2))) {
                transferItemService.deleteTransferItemList(convertList(diffList.get(2), TmsTransferItemDO::getId));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransfer(Long id) {
        // 校验存在
        TmsTransferDO transfer = validateTransferExists(id);

        // 1. 删除主表
        transferMapper.deleteById(id);

        // 2. 删除明细表
        transferItemService.deleteTransferItemByTransferId(id);
    }

    @Override
    public TmsTransferDO getTransfer(Long id) {
        return transferMapper.selectById(id);
    }

    @Override
    public TmsTransferBO getTransferBO(Long id) {
        TmsTransferDO tmsTransferDO = transferMapper.selectById(id);
        List<TmsTransferItemDO> itemDOList = transferItemService.getTransferItemListByTransferId(id);
        return BeanUtils.toBean(tmsTransferDO, TmsTransferBO.class, p -> p.setTmsTransferItemDOList(itemDOList));
    }

    @Override
    public TmsTransferRespVO getTransferRespVO(Long id) {
        TmsTransferBO bo = getTransferBO(id);
        if (bo == null) {
            return null;
        }

        TmsTransferRespVO respVO = BeanUtils.toBean(bo, TmsTransferRespVO.class);
        List<TmsTransferItemRespVO> itemVOs = BeanUtils.toBean(bo.getTmsTransferItemDOList(), TmsTransferItemRespVO.class);
        respVO.setItems(itemVOs);

        assembleProducts(itemVOs);
        assembleCompany(itemVOs);
        assembleWarehouse(Collections.singletonList(respVO));
        adminUserApi.prepareFill(Collections.singletonList(respVO))
            .mapping(TmsTransferRespVO::getCreator, TmsTransferRespVO::setCreatorName)
            .mapping(TmsTransferRespVO::getUpdater, TmsTransferRespVO::setUpdaterName)
            .mapping(TmsTransferRespVO::getAuditorId, TmsTransferRespVO::setAuditorName)
            .fill();
        adminUserApi.prepareFill(respVO.getItems())
            .mapping(TmsTransferItemRespVO::getCreator, TmsTransferItemRespVO::setCreatorName)
            .mapping(TmsTransferItemRespVO::getUpdater, TmsTransferItemRespVO::setUpdaterName)
            .fill();
        return respVO;
    }


    private void assembleProducts(List<TmsTransferItemRespVO> itemList) {
        assemble(
            itemList,
            TmsTransferItemRespVO::getProductId,
            erpProductApi::getProductMap,
            TmsTransferItemRespVO::setProduct,
            product -> BeanUtils.toBean(product, TmsProductRespVO.class)
        );
    }

    private void assembleCompany(List<TmsTransferItemRespVO> itemList) {
        assemble(
            itemList,
            TmsTransferItemRespVO::getStockCompanyId,
            ids -> fmsCompanyApi.getCompanyMap(new HashSet<>(ids)),
            TmsTransferItemRespVO::setStockCompany,
            company -> BeanUtils.toBean(company, TmsCompanyRespVO.class)
        );
    }


    private void assembleWarehouse(List<TmsTransferRespVO> transferList) {
        // fromWarehouse
        assemble(
            transferList,
            TmsTransferRespVO::getFromWarehouseId,
            ids -> wmsWarehouseApi.getWarehouseMap(new ArrayList<>(ids)),
            TmsTransferRespVO::setFromWarehouse,
            warehouse -> BeanUtils.toBean(warehouse, TmsWarehourseRespVO.class)
        );
        // toWarehouse
        assemble(
            transferList,
            TmsTransferRespVO::getToWarehouseId,
            ids -> wmsWarehouseApi.getWarehouseMap(new ArrayList<>(ids)),
            TmsTransferRespVO::setToWarehouse,
            warehouse -> BeanUtils.toBean(warehouse, TmsWarehourseRespVO.class)
        );

    }

    private void assembleDept(List<TmsTransferItemRespVO> allItems) {
        assemble(allItems,
            TmsTransferItemRespVO::getDeptId,
            ids -> deptApi.getDeptMap(ids),
            TmsTransferItemRespVO::setDept,
            dept -> BeanUtils.toBean(dept, TmsDeptRespVO.class)
        );
    }

    /**
     * 装配调拨单VO的关联数据
     *
     * @param list 调拨单BO列表
     * @return 装配后的VO列表
     */
    private List<TmsTransferRespVO> assembleTransferVOList(List<TmsTransferBO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<TmsTransferRespVO> respVOList = list.stream().map(bo -> {
            TmsTransferRespVO respVO = BeanUtils.toBean(bo, TmsTransferRespVO.class);
            List<TmsTransferItemRespVO> itemVOs = BeanUtils.toBean(bo.getTmsTransferItemDOList(), TmsTransferItemRespVO.class);
            respVO.setItems(itemVOs);
            return respVO;
        }).toList();

        List<TmsTransferItemRespVO> allItems = respVOList.stream()
            .flatMap(vo -> vo.getItems().stream())
            .toList();

        assembleProducts(allItems);
        assembleCompany(allItems);
        assembleWarehouse(respVOList);
        assembleDept(allItems);

        return respVOList;
    }


    @Override
    public PageResult<TmsTransferBO> getTransferBOPage(TmsTransferPageReqVO pageReqVO) {
        // 1. 获取子表分页数据
        PageResult<TmsTransferItemBO> pageResult = tmsTransferItemMapper.selectBOPage(pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), pageResult.getTotal());
        }

        // 2. 转换为BO对象列表
        List<TmsTransferBO> boList = TmsTransferConvert.convertBOList(pageResult.getList());

        // 3. 返回分页结果
        return new PageResult<>(boList, pageResult.getTotal());
    }

    @Override
    public TmsTransferDO validateTransferExists(Long id) {
        TmsTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw exception(TRANSFER_NOT_EXISTS);
        }
        return transfer;
    }

    @Override
    public PageResult<TmsTransferRespVO> getTmsTransferRespVOPage(TmsTransferPageReqVO pageReqVO) {
        PageResult<TmsTransferBO> boPageResult = this.getTransferBOPage(pageReqVO);
        if (boPageResult.getList().isEmpty()) {
            return new PageResult<>(Collections.emptyList(), boPageResult.getTotal());
        }

        List<TmsTransferRespVO> respVOList = assembleTransferVOList(boPageResult.getList());
        adminUserApi.prepareFill(respVOList)
            .mapping(TmsTransferRespVO::getCreator, TmsTransferRespVO::setCreatorName)
            .mapping(TmsTransferRespVO::getUpdater, TmsTransferRespVO::setUpdaterName)
            .mapping(TmsTransferRespVO::getAuditorId, TmsTransferRespVO::setAuditorName)
            .fill();
        List<TmsTransferItemRespVO> itemRespVOList = respVOList.stream()
            .filter(vo -> vo.getItems() != null)
            .flatMap(vo -> vo.getItems().stream())
            .toList();
        adminUserApi.prepareFill(itemRespVOList)
            .mapping(TmsTransferItemRespVO::getCreator, TmsTransferItemRespVO::setCreatorName)
            .mapping(TmsTransferItemRespVO::getUpdater, TmsTransferItemRespVO::setUpdaterName)
            .fill();

        return new PageResult<>(respVOList, boPageResult.getTotal());
    }

    @Override
    public void switchOpen(TmsTransferOffStatusReqVO reqVO) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(TmsTransferAuditReqVO reqVO) {
        // 1. 校验调拨单是否存在
        TmsTransferDO tmsTransferDO = validateTransferExists(reqVO.getId());

        if (reqVO.getReviewed()) {
            //审核
            if (reqVO.getPass()) {
                //通过
                //1.0 状态
                transferAuditStateMachine.fireEvent(TmsAuditStatus.fromCode(tmsTransferDO.getAuditStatus()), TmsEventEnum.AGREE, reqVO);
                //2.0 创建出库单(待审核)
                TmsTransferBO tmsTransferBO = getTransferBO(reqVO.getId());
                try {
                    this.createWmsOutbound(tmsTransferBO);
                } catch (Exception e) {
                    throw exception(TRANSFER_CREATE_OUT_STOCK_ERROR, truncate(e.getMessage(), 200));
                }

            } else {
                //不通过
                transferAuditStateMachine.fireEvent(TmsAuditStatus.fromCode(tmsTransferDO.getAuditStatus()), TmsEventEnum.REJECT, reqVO);
            }
        } else {
            //反审核
            //1.0 作废WMS出库单
            abandonWmsOutbound(tmsTransferDO.getId());
            //2.0 更新状态
            transferAuditStateMachine.fireEvent(TmsAuditStatus.fromCode(tmsTransferDO.getAuditStatus()), TmsEventEnum.WITHDRAW_REVIEW, reqVO);
        }
    }

    /**
     * 创建 WMS 出库单
     *
     * @param transferBO 调拨单
     */
    private void createWmsOutbound(TmsTransferBO transferBO) {
        // 1. 创建出库单
        WmsOutboundImportReqDTO importReqDTO = buildOutboundBaseInfo(transferBO);
        importReqDTO.setItemList(buildOutboundItems(transferBO.getTmsTransferItemDOList()));

        // 生成出库单
        wmsOutboundApi.generateOutbound(importReqDTO);
    }

    /**
     * 构建出库单基本信息
     *
     * @param transferBO 调拨单
     * @return 出库单基本信息
     */
    private WmsOutboundImportReqDTO buildOutboundBaseInfo(TmsTransferBO transferBO) {
        WmsOutboundImportReqDTO importReqDTO = new WmsOutboundImportReqDTO();
        importReqDTO.setType(WmsOutboundType.TRANSFER.getValue()); // 调拨出库
        importReqDTO.setUpstreamId(transferBO.getId()); // 来源单据ID
        importReqDTO.setUpstreamCode(transferBO.getCode()); // 来源单据编码
        importReqDTO.setUpstreamType(BillType.TMS_TRANSFER.getValue()); // 来源单据类型
        importReqDTO.setRemark(transferBO.getRemark()); // 备注
        importReqDTO.setOutboundTime(transferBO.getOutboundTime()); // 出库时间
        importReqDTO.setWarehouseId(transferBO.getFromWarehouseId()); //起始仓
        return importReqDTO;
    }

    /**
     * 构建出库单明细信息
     *
     * @param items 调拨单明细
     * @return 出库单明细列表
     */
    private List<WmsOutboundItemSaveReqDTO> buildOutboundItems(List<TmsTransferItemDO> items) {
        return items.stream().map(item -> {
            WmsOutboundItemSaveReqDTO itemDTO = new WmsOutboundItemSaveReqDTO();
            itemDTO.setProductId(item.getProductId()); // 产品ID
            itemDTO.setPlanQty(item.getQty()); // 计划出库量
            itemDTO.setActualQty(item.getQty()); // 实际出库量
            itemDTO.setRemark(item.getRemark()); // 备注
            itemDTO.setUpstreamId(item.getId()); // 来源明细行ID
            itemDTO.setCompanyId(item.getStockCompanyId()); // 设置库存公司ID
            itemDTO.setDeptId(item.getDeptId()); //库存归属部门ID(哪个部门出库SKU)
            return itemDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 作废调拨单关联的WMS出库单
     *
     * @param transferId 调拨单ID
     */
    private void abandonWmsOutbound(Long transferId) {
        List<WmsOutboundDTO> dtoList = Optional.ofNullable(wmsOutboundApi.getOutboundList(BillType.TMS_TRANSFER.getValue(), transferId))
            .orElse(Collections.emptyList());
        if (CollUtil.isEmpty(dtoList)) {
            return;
        }

        //  收集所有非草稿
        List<String> nonDraftCodes = dtoList.stream()
            .filter(dto -> !Objects.equals(dto.getAuditStatus(), WmsOutboundAuditStatus.DRAFT.getValue()))
            .map(WmsOutboundDTO::getCode)
            .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(nonDraftCodes)) {
            throw exception(TRANSFER_ITEM_REVOKE_FAIL_OUT_STOCK_EXISTS, String.join(",", nonDraftCodes));
        }

        // 作废
        dtoList.forEach(dto -> wmsOutboundApi.abandonOutbound(dto.getId(), "调拨单反审核"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(List<Long> transferIds) {
        transferIds.forEach(transferId -> {
            TmsTransferDO tmsTransferDO = validateTransferExists(transferId);
            transferAuditStateMachine.fireEvent(TmsAuditStatus.fromCode(tmsTransferDO.getAuditStatus()), TmsEventEnum.SUBMIT_FOR_REVIEW, new TmsTransferAuditReqVO().setId(transferId));
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransferStatus(TmsTransferStatusUpdateDTO updateDTO) {
        // 1. 校验调拨单是否存在
        TmsTransferDO transfer = validateTransferExists(updateDTO.getId());

        // 2. 更新出库状态相关信息
        if (updateDTO.getOutboundTime() != null) {
            transfer.setOutboundTime(updateDTO.getOutboundTime());
        }
        if (updateDTO.getOutboundStatus() != null) {
            transfer.setOutboundStatus(updateDTO.getOutboundStatus());
        }
        if (updateDTO.getOutboundId() != null) {
            transfer.setOutboundId(updateDTO.getOutboundId());
        }
        if (updateDTO.getOutboundCode() != null) {
            transfer.setOutboundCode(updateDTO.getOutboundCode());
        }

        // 3. 更新入库状态相关信息
        if (updateDTO.getInboundTime() != null) {
            transfer.setInboundTime(updateDTO.getInboundTime());
        }
        if (updateDTO.getInboundStatus() != null) {
            transfer.setInboundStatus(updateDTO.getInboundStatus());
        }
        if (updateDTO.getInboundId() != null) {
            transfer.setInboundId(updateDTO.getInboundId());
        }
        if (updateDTO.getInboundCode() != null) {
            transfer.setInboundCode(updateDTO.getInboundCode());
        }

        // 5. 保存更新
        transferMapper.updateById(transfer);
    }

    /**
     * 校验调拨单编码是否唯一
     *
     * @param code 调拨单编码
     * @param id   调拨单编号
     */
    private void validateTransferCodeUnique(String code, Long id) {
        TmsTransferDO transfer = transferMapper.selectOne(TmsTransferDO::getCode, code);
        if (transfer == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id
        if (id == null) {
            throw exception(TRANSFER_CODE_DUPLICATE, code);
        }
        if (!transfer.getId().equals(id)) {
            throw exception(TRANSFER_CODE_DUPLICATE, code);
        }
    }

    @Override
    public TmsTransferSellableQtyRespVO getSellableQty(TmsTransferSellableQtyReqVO reqVO) {
        // 1. 构建查询参数
        WmsWarehouseQueryDTO queryDTO = new WmsWarehouseQueryDTO();
        queryDTO.setWarehouses(reqVO.getWarehouses().stream()
            .map(item -> {
                WmsWarehouseSimpleDTO dto = new WmsWarehouseSimpleDTO();
                dto.setWarehouseId(item.getWarehouseId());
                dto.setProductIds(item.getProductIds());
                return dto;
            })
            .collect(Collectors.toList()));

        // 2. 调用 WMS 接口获取可售库存
        Map<Long, List<WmsStockWarehouseSimpleDTO>> result = wmsWarehouseApi.selectSellableQty(queryDTO);

        // 3. 转换返回结果
        TmsTransferSellableQtyRespVO respVO = new TmsTransferSellableQtyRespVO();
        Map<Long, List<TmsTransferSellableQtyRespVO.ProductSellableQty>> warehouseProductMap = result.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(item -> {
                        TmsTransferSellableQtyRespVO.ProductSellableQty productQty = new TmsTransferSellableQtyRespVO.ProductSellableQty();
                        productQty.setProductId(item.getProductId());
                        productQty.setSellableQty(item.getSellableQty());
                        return productQty;
                    })
                    .collect(Collectors.toList())
            ));
        respVO.setWarehouseProductMap(warehouseProductMap);

        return respVO;
    }

}