package cn.iocoder.yudao.module.mes.service.md.vendor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorImportExcelVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorImportRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.vendor.vo.MesMdVendorSaveReqVO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor.MesMdVendorDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.vendor.MesMdVendorMapper;
import cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.qc.iqc.MesQcIqcService;
import cn.iocoder.yudao.module.mes.service.wm.arrivalnotice.MesWmArrivalNoticeService;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import cn.iocoder.yudao.module.mes.service.wm.itemreceipt.MesWmItemReceiptService;
import cn.iocoder.yudao.module.mes.service.wm.outsourceissue.MesWmOutsourceIssueService;
import cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt.MesWmOutsourceReceiptService;
import cn.iocoder.yudao.module.mes.service.wm.returnvendor.MesWmReturnVendorService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 供应商 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdVendorServiceImpl implements MesMdVendorService {

    @Resource
    private MesMdVendorMapper vendorMapper;

    @Resource
    private MesWmBarcodeService barcodeService;
    @Resource
    @Lazy
    private MesWmItemReceiptService itemReceiptService;
    @Resource
    @Lazy
    private MesWmArrivalNoticeService arrivalNoticeService;
    @Resource
    @Lazy
    private MesWmReturnVendorService returnVendorService;
    @Resource
    @Lazy
    private MesQcIqcService iqcService;
    @Resource
    @Lazy
    private MesProWorkOrderService workOrderService;
    @Resource
    @Lazy
    private MesWmOutsourceIssueService outsourceIssueService;
    @Resource
    @Lazy
    private MesWmOutsourceReceiptService outsourceReceiptService;

    @Override
    public Long createVendor(MesMdVendorSaveReqVO createReqVO) {
        // 校验数据
        validateVendorSaveData(createReqVO);

        // 插入
        MesMdVendorDO vendor = BeanUtils.toBean(createReqVO, MesMdVendorDO.class);
        vendorMapper.insert(vendor);

        // 自动生成条码
        barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.VENDOR.getValue(),
                vendor.getId(), vendor.getCode(), vendor.getName());
        return vendor.getId();
    }

    @Override
    public void updateVendor(MesMdVendorSaveReqVO updateReqVO) {
        // 校验存在
        validateVendorExists(updateReqVO.getId());
        // 校验数据
        validateVendorSaveData(updateReqVO);

        // 更新
        MesMdVendorDO updateObj = BeanUtils.toBean(updateReqVO, MesMdVendorDO.class);
        vendorMapper.updateById(updateObj);
    }

    @Override
    public void deleteVendor(Long id) {
        // 校验存在
        validateVendorExists(id);
        // 校验是否被其他业务引用
        validateVendorNotReferenced(id);
        // 删除
        vendorMapper.deleteById(id);
    }

    @Override
    public MesMdVendorDO validateVendorExists(Long id) {
        MesMdVendorDO vendor = vendorMapper.selectById(id);
        if (vendor == null) {
            throw exception(MD_VENDOR_NOT_EXISTS);
        }
        return vendor;
    }

    @Override
    public MesMdVendorDO validateVendorExistsAndEnable(Long id) {
        MesMdVendorDO vendor = validateVendorExists(id);
        if (ObjUtil.notEqual(CommonStatusEnum.ENABLE.getStatus(), vendor.getStatus())) {
            throw exception(MD_VENDOR_IS_DISABLE);
        }
        return vendor;
    }

    private void validateVendorSaveData(MesMdVendorSaveReqVO reqVO) {
        // 校验编码唯一
        validateVendorCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验名称唯一
        validateVendorNameUnique(reqVO.getId(), reqVO.getName());
        // 校验简称唯一
        validateVendorNicknameUnique(reqVO.getId(), reqVO.getNickname());
    }

    private void validateVendorCodeUnique(Long id, String code) {
        MesMdVendorDO vendor = vendorMapper.selectByCode(code);
        if (vendor == null) {
            return;
        }
        if (ObjUtil.notEqual(vendor.getId(), id)) {
            throw exception(MD_VENDOR_CODE_DUPLICATE);
        }
    }

    private void validateVendorNameUnique(Long id, String name) {
        MesMdVendorDO vendor = vendorMapper.selectByName(name);
        if (vendor == null) {
            return;
        }
        if (ObjUtil.notEqual(vendor.getId(), id)) {
            throw exception(MD_VENDOR_NAME_DUPLICATE);
        }
    }

    private void validateVendorNicknameUnique(Long id, String nickname) {
        if (StrUtil.isEmpty(nickname)) {
            return;
        }
        MesMdVendorDO vendor = vendorMapper.selectByNickname(nickname);
        if (vendor == null) {
            return;
        }
        if (ObjUtil.notEqual(vendor.getId(), id)) {
            throw exception(MD_VENDOR_NICKNAME_DUPLICATE);
        }
    }

    private void validateVendorNotReferenced(Long id) {
        if (itemReceiptService.getItemReceiptCountByVendorId(id) > 0
                || arrivalNoticeService.getArrivalNoticeCountByVendorId(id) > 0
                || returnVendorService.getReturnVendorCountByVendorId(id) > 0
                || iqcService.getIqcCountByVendorId(id) > 0
                || workOrderService.getWorkOrderCountByVendorId(id) > 0
                || outsourceIssueService.getOutsourceIssueCountByVendorId(id) > 0
                || outsourceReceiptService.getOutsourceReceiptCountByVendorId(id) > 0) {
            throw exception(MD_VENDOR_HAS_REFERENCE);
        }
    }

    @Override
    public MesMdVendorDO getVendor(Long id) {
        return vendorMapper.selectById(id);
    }

    @Override
    public PageResult<MesMdVendorDO> getVendorPage(MesMdVendorPageReqVO pageReqVO) {
        return vendorMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesMdVendorDO> getVendorList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return vendorMapper.selectByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MesMdVendorImportRespVO importVendorList(List<MesMdVendorImportExcelVO> importVendors, boolean updateSupport) {
        // 1. 参数校验
        if (CollUtil.isEmpty(importVendors)) {
            throw exception(MD_VENDOR_IMPORT_LIST_IS_EMPTY);
        }

        // 2. 遍历，逐个创建 or 更新
        MesMdVendorImportRespVO respVO = MesMdVendorImportRespVO.builder()
                .createCodes(new ArrayList<>()).updateCodes(new ArrayList<>())
                .failureCodes(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importVendors.forEach(importVendor -> {
            int currentIndex = index.getAndIncrement();
            // 2.1 校验字段
            String key = StrUtil.blankToDefault(importVendor.getCode(), "第 " + currentIndex + " 行");
            if (StrUtil.isBlank(importVendor.getCode())) {
                respVO.getFailureCodes().put(key, "供应商编码不能为空");
                return;
            }
            if (StrUtil.isBlank(importVendor.getName())) {
                respVO.getFailureCodes().put(key, "供应商名称不能为空");
                return;
            }

            // 2.2 判断：创建 or 更新
            MesMdVendorDO existVendor = vendorMapper.selectByCode(importVendor.getCode());
            if (existVendor == null) {
                // 2.2.1 创建
                try {
                    validateVendorNameUnique(null, importVendor.getName());
                    validateVendorNicknameUnique(null, importVendor.getNickname());
                } catch (ServiceException ex) {
                    respVO.getFailureCodes().put(key, ex.getMessage());
                    return;
                }
                MesMdVendorDO vendor = BeanUtils.toBean(importVendor, MesMdVendorDO.class);
                vendorMapper.insert(vendor);
                // 自动生成条码
                barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.VENDOR.getValue(),
                        vendor.getId(), vendor.getCode(), vendor.getName());
                respVO.getCreateCodes().add(importVendor.getCode());
            } else if (updateSupport) {
                // 2.2.2 更新
                try {
                    validateVendorNameUnique(existVendor.getId(), importVendor.getName());
                    validateVendorNicknameUnique(existVendor.getId(), importVendor.getNickname());
                } catch (ServiceException ex) {
                    respVO.getFailureCodes().put(key, ex.getMessage());
                    return;
                }
                MesMdVendorDO updateObj = BeanUtils.toBean(importVendor, MesMdVendorDO.class);
                updateObj.setId(existVendor.getId());
                vendorMapper.updateById(updateObj);
                respVO.getUpdateCodes().add(importVendor.getCode());
            } else {
                // 不支持更新
                respVO.getFailureCodes().put(key, "供应商编码已存在");
            }
        });
        return respVO;
    }

}
