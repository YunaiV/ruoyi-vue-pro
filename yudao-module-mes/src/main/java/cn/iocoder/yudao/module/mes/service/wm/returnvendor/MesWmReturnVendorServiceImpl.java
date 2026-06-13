package cn.iocoder.yudao.module.mes.service.wm.returnvendor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.MesWmReturnVendorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.MesWmReturnVendorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.returnvendor.MesWmReturnVendorMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmReturnVendorStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.vendor.MesMdVendorService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 供应商退货单 Service 实现类
 */
@Service
@Validated
public class MesWmReturnVendorServiceImpl implements MesWmReturnVendorService {

    @Resource
    private MesWmReturnVendorMapper returnVendorMapper;

    @Resource
    private MesWmReturnVendorLineService returnVendorLineService;
    @Resource
    private MesWmReturnVendorDetailService returnVendorDetailService;
    @Resource
    private MesMdVendorService vendorService;
    @Resource
    private MesWmTransactionService wmTransactionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReturnVendor(MesWmReturnVendorSaveReqVO createReqVO) {
        // 1. 校验数据
        validateReturnVendorSaveData(createReqVO);

        // 2. 插入主表
        MesWmReturnVendorDO returnVendor = BeanUtils.toBean(createReqVO, MesWmReturnVendorDO.class);
        returnVendor.setStatus(MesWmReturnVendorStatusEnum.PREPARE.getStatus());
        returnVendorMapper.insert(returnVendor);
        return returnVendor.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReturnVendor(MesWmReturnVendorSaveReqVO updateReqVO) {
        // 1. 校验数据
        validateReturnVendorSaveData(updateReqVO);

        // 2. 更新主表
        MesWmReturnVendorDO updateObj = BeanUtils.toBean(updateReqVO, MesWmReturnVendorDO.class);
        returnVendorMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReturnVendor(Long id) {
        // 1. 校验存在 + 准备中状态
        validateReturnVendorExistsAndPrepare(id);

        // 2.1 级联删除明细
        returnVendorDetailService.deleteReturnVendorDetailByReturnId(id);
        // 2.2 级联删除行
        returnVendorLineService.deleteReturnVendorLineByReturnId(id);
        // 2.3 删除主表
        returnVendorMapper.deleteById(id);
    }

    @Override
    public MesWmReturnVendorDO getReturnVendor(Long id) {
        return returnVendorMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmReturnVendorDO> getReturnVendorPage(MesWmReturnVendorPageReqVO pageReqVO) {
        return returnVendorMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReturnVendor(Long id) {
        // 校验存在 + 草稿状态
        validateReturnVendorExistsAndPrepare(id);
        // 校验至少有一条行
        List<MesWmReturnVendorLineDO> lines = returnVendorLineService.getReturnVendorLineListByReturnId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_RETURN_VENDOR_NO_LINE);
        }

        // 提交（草稿 -> 待拣货）
        returnVendorMapper.updateById(new MesWmReturnVendorDO()
                .setId(id).setStatus(MesWmReturnVendorStatusEnum.APPROVING.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockReturnVendor(Long id) {
        // 校验存在
        MesWmReturnVendorDO returnVendor = validateReturnVendorExists(id);
        if (ObjUtil.notEqual(MesWmReturnVendorStatusEnum.APPROVING.getStatus(), returnVendor.getStatus())) {
            throw exception(WM_RETURN_VENDOR_STATUS_INVALID);
        }
        // 执行拣货（待拣货 -> 待执行退货）
        returnVendorMapper.updateById(new MesWmReturnVendorDO()
                .setId(id).setStatus(MesWmReturnVendorStatusEnum.APPROVED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishReturnVendor(Long id) {
        // 1.1 校验存在
        MesWmReturnVendorDO returnVendor = validateReturnVendorExists(id);
        if (ObjUtil.notEqual(MesWmReturnVendorStatusEnum.APPROVED.getStatus(), returnVendor.getStatus())) {
            throw exception(WM_RETURN_VENDOR_STATUS_INVALID);
        }
        // 1.2 校验明细不为空
        List<MesWmReturnVendorDetailDO> details = returnVendorDetailService.getReturnVendorDetailListByReturnId(id);
        if (CollUtil.isEmpty(details)) {
            throw exception(WM_RETURN_VENDOR_NO_DETAIL);
        }

        // 2. 遍历所有明细，创建库存事务（扣减库存 + 记录流水）
        createTransactionList(returnVendor, details);

        // 3. 更新退货单状态
        returnVendorMapper.updateById(new MesWmReturnVendorDO()
                .setId(id).setStatus(MesWmReturnVendorStatusEnum.FINISHED.getStatus()));
    }

    private void createTransactionList(MesWmReturnVendorDO returnVendor, List<MesWmReturnVendorDetailDO> details) {
        wmTransactionService.createTransactionList(convertList(details, detail -> new MesWmTransactionSaveReqDTO()
                .setType(MesWmTransactionTypeEnum.OUT.getType()).setItemId(detail.getItemId())
                .setQuantity(detail.getQuantity().negate()) // 出库数量为负数
                .setBatchId(detail.getBatchId()).setBatchCode(detail.getBatchCode())
                .setWarehouseId(detail.getWarehouseId()).setLocationId(detail.getLocationId()).setAreaId(detail.getAreaId())
                .setVendorId(returnVendor.getVendorId())
                .setBizType(MesBizTypeConstants.WM_RETURN_VENDOR).setBizId(returnVendor.getId())
                .setBizCode(returnVendor.getCode()).setBizLineId(detail.getLineId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReturnVendor(Long id) {
        // 校验存在
        MesWmReturnVendorDO returnVendor = validateReturnVendorExists(id);
        // 已完成和已取消不允许取消
        if (ObjectUtils.equalsAny(returnVendor.getStatus(),
                MesWmReturnVendorStatusEnum.FINISHED.getStatus(),
                MesWmReturnVendorStatusEnum.CANCELED.getStatus())) {
            throw exception(WM_RETURN_VENDOR_CANCEL_NOT_ALLOWED);
        }

        // 取消
        returnVendorMapper.updateById(new MesWmReturnVendorDO()
                .setId(id).setStatus(MesWmReturnVendorStatusEnum.CANCELED.getStatus()));
    }

    @Override
    public Boolean checkReturnVendorQuantity(Long id) {
        List<MesWmReturnVendorLineDO> lines = returnVendorLineService.getReturnVendorLineListByReturnId(id);
        for (MesWmReturnVendorLineDO line : lines) {
            List<MesWmReturnVendorDetailDO> details = returnVendorDetailService.getReturnVendorDetailListByLineId(line.getId());
            BigDecimal totalDetailQty = CollectionUtils.getSumValue(details,
                    MesWmReturnVendorDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
            if (line.getQuantity() != null && totalDetailQty.compareTo(line.getQuantity()) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public MesWmReturnVendorDO validateReturnVendorExists(Long id) {
        MesWmReturnVendorDO returnVendor = returnVendorMapper.selectById(id);
        if (returnVendor == null) {
            throw exception(WM_RETURN_VENDOR_NOT_EXISTS);
        }
        return returnVendor;
    }

    @Override
    public MesWmReturnVendorDO validateReturnVendorExistsAndPrepare(Long id) {
        MesWmReturnVendorDO returnVendor = validateReturnVendorExists(id);
        if (ObjUtil.notEqual(MesWmReturnVendorStatusEnum.PREPARE.getStatus(), returnVendor.getStatus())) {
            throw exception(WM_RETURN_VENDOR_STATUS_INVALID);
        }
        return returnVendor;
    }

    /**
     * 校验退货单创建/修改的公共数据
     */
    private void validateReturnVendorSaveData(MesWmReturnVendorSaveReqVO reqVO) {
        // 校验存在 + 草稿状态（仅修改时）
        if (reqVO.getId() != null) {
            validateReturnVendorExistsAndPrepare(reqVO.getId());
        }
        // 校验供应商存在
        vendorService.validateVendorExistsAndEnable(reqVO.getVendorId());
        // 校验编码唯一
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
    }

    /**
     * 校验退货单编号唯一
     */
    private void validateCodeUnique(Long id, String code) {
        MesWmReturnVendorDO existing = returnVendorMapper.selectByCode(code);
        if (existing == null) {
            return;
        }
        if (ObjUtil.notEqual(id, existing.getId())) {
            throw exception(WM_RETURN_VENDOR_CODE_DUPLICATE);
        }
    }

    @Override
    public Long getReturnVendorCountByVendorId(Long vendorId) {
        return returnVendorMapper.selectCountByVendorId(vendorId);
    }

}
