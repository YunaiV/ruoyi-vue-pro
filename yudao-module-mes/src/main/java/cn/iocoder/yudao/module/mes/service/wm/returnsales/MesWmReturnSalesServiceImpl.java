package cn.iocoder.yudao.module.mes.service.wm.returnsales;

import cn.hutool.core.collection.CollUtil;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.MesWmReturnSalesPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.MesWmReturnSalesSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.returnsales.MesWmReturnSalesMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmReturnSalesStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 销售退货单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmReturnSalesServiceImpl implements MesWmReturnSalesService {

    @Resource
    private MesWmReturnSalesMapper returnSalesMapper;

    @Resource
    private MesWmReturnSalesLineService returnSalesLineService;
    @Resource
    private MesWmReturnSalesDetailService returnSalesDetailService;
    @Resource
    private MesMdClientService clientService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmTransactionService wmTransactionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReturnSales(MesWmReturnSalesSaveReqVO createReqVO) {
        // 1. 校验关联数据
        validateSaveData(createReqVO);

        // 2. 插入主表
        MesWmReturnSalesDO returnSales = BeanUtils.toBean(createReqVO, MesWmReturnSalesDO.class);
        returnSales.setStatus(MesWmReturnSalesStatusEnum.PREPARE.getStatus());
        returnSalesMapper.insert(returnSales);
        return returnSales.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReturnSales(MesWmReturnSalesSaveReqVO updateReqVO) {
        // 1.1 校验存在 + 准备中状态
        validateReturnSalesExistsAndPrepare(updateReqVO.getId());
        // 1.2 校验关联数据
        validateSaveData(updateReqVO);

        // 2. 更新主表
        MesWmReturnSalesDO updateObj = BeanUtils.toBean(updateReqVO, MesWmReturnSalesDO.class);
        returnSalesMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReturnSales(Long id) {
        // 1. 校验存在 + 准备中状态
        validateReturnSalesExistsAndPrepare(id);

        // 2.1 级联删除明细
        returnSalesDetailService.deleteReturnSalesDetailByReturnId(id);
        // 2.2 级联删除行
        returnSalesLineService.deleteReturnSalesLineByReturnId(id);
        // 2.3 删除主表
        returnSalesMapper.deleteById(id);
    }

    @Override
    public MesWmReturnSalesDO getReturnSales(Long id) {
        return returnSalesMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmReturnSalesDO> getReturnSalesPage(MesWmReturnSalesPageReqVO pageReqVO) {
        return returnSalesMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReturnSales(Long id) {
        // 1.1 校验存在 + 草稿状态
        validateReturnSalesExistsAndPrepare(id);
        // 1.2 校验至少有一条行
        List<MesWmReturnSalesLineDO> lines = returnSalesLineService.getReturnSalesLineListByReturnId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_RETURN_SALES_NO_LINE);
        }

        // 2.1 确定目标状态：1）有待检验物料 → 待检验状态；2）无待检验物料 → 待执行状态
        boolean hasPendingQc = CollUtil.contains(lines,
                line -> MesWmQualityStatusEnum.PENDING.getStatus().equals(line.getQualityStatus()));
        Integer targetStatus = hasPendingQc ? MesWmReturnSalesStatusEnum.CONFIRMED.getStatus()
                : MesWmReturnSalesStatusEnum.APPROVING.getStatus();
        // 2.2 提交（草稿 -> 待检验/待执行）
        returnSalesMapper.updateById(new MesWmReturnSalesDO()
                .setId(id).setStatus(targetStatus));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishReturnSales(Long id) {
        // 1.1 校验存在 + 待执行状态
        MesWmReturnSalesDO returnSales = validateReturnSalesExists(id);
        if (ObjUtil.notEqual(MesWmReturnSalesStatusEnum.APPROVING.getStatus(), returnSales.getStatus())) {
            throw exception(WM_RETURN_SALES_STATUS_NOT_APPROVING);
        }
        // 1.2 校验至少有一条行
        List<MesWmReturnSalesLineDO> lines = returnSalesLineService.getReturnSalesLineListByReturnId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_RETURN_SALES_NO_LINE);
        }
        // 2. 执行退货（待执行 -> 待上架）
        returnSalesMapper.updateById(new MesWmReturnSalesDO()
                .setId(id).setStatus(MesWmReturnSalesStatusEnum.APPROVED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockReturnSales(Long id) {
        // 1.1 校验存在 + 待上架状态
        MesWmReturnSalesDO returnSales = validateReturnSalesExists(id);
        if (ObjUtil.notEqual(MesWmReturnSalesStatusEnum.APPROVED.getStatus(), returnSales.getStatus())) {
            throw exception(WM_RETURN_SALES_STATUS_NOT_APPROVED);
        }
        // 1.2 检查每个行的明细数量是否完成上架
        List<MesWmReturnSalesLineDO> lines = returnSalesLineService.getReturnSalesLineListByReturnId(id);
        if (CollUtil.isNotEmpty(lines)) {
            // 批量查询所有明细
            List<MesWmReturnSalesDetailDO> allDetails = returnSalesDetailService.getReturnSalesDetailListByReturnId(id);
            Map<Long, List<MesWmReturnSalesDetailDO>> detailMap = CollectionUtils.convertMultiMap(
                    allDetails, MesWmReturnSalesDetailDO::getLineId);
            // 检查每行的明细数量
            for (MesWmReturnSalesLineDO line : lines) {
                List<MesWmReturnSalesDetailDO> details = detailMap.getOrDefault(line.getId(), Collections.emptyList());
                BigDecimal totalDetailQuantity = CollectionUtils.getSumValue(details,
                        MesWmReturnSalesDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
                // 对比行数量与明细总数量，不满足直接抛出
                if (line.getQuantity().compareTo(totalDetailQuantity) > 0) {
                    MesMdItemDO item = itemService.validateItemExistsAndEnable(line.getItemId());
                    throw exception(WM_RETURN_SALES_DETAIL_QUANTITY_MISMATCH,
                            item.getCode() + " " + item.getName() + " 未完成上架");
                }
            }
        }

        // 2. 遍历所有明细，创建库存事务（增加库存 + 记录流水）
        createTransactionList(returnSales);

        // 3. 更新退货单状态
        returnSalesMapper.updateById(new MesWmReturnSalesDO()
                .setId(id).setStatus(MesWmReturnSalesStatusEnum.FINISHED.getStatus()));
    }

    private void createTransactionList(MesWmReturnSalesDO returnSales) {
        List<MesWmReturnSalesDetailDO> details = returnSalesDetailService.getReturnSalesDetailListByReturnId(returnSales.getId());
        wmTransactionService.createTransactionList(convertList(details, detail -> new MesWmTransactionSaveReqDTO()
                .setType(MesWmTransactionTypeEnum.IN.getType()).setItemId(detail.getItemId())
                .setQuantity(detail.getQuantity()) // 入库数量为正数
                .setBatchId(detail.getBatchId()).setBatchCode(detail.getBatchCode())
                .setWarehouseId(detail.getWarehouseId()).setLocationId(detail.getLocationId()).setAreaId(detail.getAreaId())
                .setBizType(MesBizTypeConstants.WM_RETURN_SALES).setBizId(returnSales.getId())
                .setBizCode(returnSales.getCode()).setBizLineId(detail.getLineId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReturnSales(Long id) {
        // 校验存在
        MesWmReturnSalesDO returnSales = validateReturnSalesExists(id);
        // 已完成和已取消不允许取消
        if (ObjectUtils.equalsAny(returnSales.getStatus(),
                MesWmReturnSalesStatusEnum.FINISHED.getStatus(),
                MesWmReturnSalesStatusEnum.CANCELED.getStatus())) {
            throw exception(WM_RETURN_SALES_CANCEL_NOT_ALLOWED);
        }

        // 取消
        returnSalesMapper.updateById(new MesWmReturnSalesDO()
                .setId(id).setStatus(MesWmReturnSalesStatusEnum.CANCELED.getStatus()));
    }

    @Override
    public Boolean checkReturnSalesQuantity(Long id) {
        List<MesWmReturnSalesLineDO> lines = returnSalesLineService.getReturnSalesLineListByReturnId(id);
        for (MesWmReturnSalesLineDO line : lines) {
            List<MesWmReturnSalesDetailDO> details = returnSalesDetailService.getReturnSalesDetailListByLineId(line.getId());
            BigDecimal totalDetailQty = CollectionUtils.getSumValue(details,
                    MesWmReturnSalesDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
            if (line.getQuantity() != null && totalDetailQty.compareTo(line.getQuantity()) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MesWmReturnSalesDO validateReturnSalesExists(Long id) {
        MesWmReturnSalesDO returnSales = returnSalesMapper.selectById(id);
        if (returnSales == null) {
            throw exception(WM_RETURN_SALES_NOT_EXISTS);
        }
        return returnSales;
    }

    /**
     * 校验销售退货单存在且为准备中状态
     */
    @Override
    public MesWmReturnSalesDO validateReturnSalesExistsAndPrepare(Long id) {
        MesWmReturnSalesDO returnSales = validateReturnSalesExists(id);
        if (ObjUtil.notEqual(MesWmReturnSalesStatusEnum.PREPARE.getStatus(), returnSales.getStatus())) {
            throw exception(WM_RETURN_SALES_STATUS_NOT_PREPARE);
        }
        return returnSales;
    }

    @Override
    public void updateReturnSalesStatus(Long id, Integer status) {
        validateReturnSalesExists(id);
        returnSalesMapper.updateById(new MesWmReturnSalesDO().setId(id).setStatus(status));
    }

    /**
     * 校验编码唯一性
     */
    private void validateCodeUnique(Long id, String code) {
        MesWmReturnSalesDO returnSales = returnSalesMapper.selectByCode(code);
        if (returnSales == null) {
            return;
        }
        if (ObjUtil.notEqual(id, returnSales.getId())) {
            throw exception(WM_RETURN_SALES_CODE_DUPLICATE);
        }
    }

    /**
     * 校验保存时的关联数据
     */
    private void validateSaveData(MesWmReturnSalesSaveReqVO reqVO) {
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        clientService.validateClientExistsAndEnable(reqVO.getClientId());
    }

}
