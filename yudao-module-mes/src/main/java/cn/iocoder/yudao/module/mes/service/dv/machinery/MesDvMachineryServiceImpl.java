package cn.iocoder.yudao.module.mes.service.dv.machinery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.MesDvMachineryImportExcelVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.MesDvMachineryImportRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.MesDvMachineryPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.MesDvMachinerySaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryTypeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkshopDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.machinery.MesDvMachineryMapper;
import cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum;
import cn.iocoder.yudao.module.mes.service.dv.checkplan.MesDvCheckPlanMachineryService;
import cn.iocoder.yudao.module.mes.service.dv.checkrecord.MesDvCheckRecordService;
import cn.iocoder.yudao.module.mes.service.dv.maintenrecord.MesDvMaintenRecordService;
import cn.iocoder.yudao.module.mes.service.dv.repair.MesDvRepairService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkshopService;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 设备台账 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvMachineryServiceImpl implements MesDvMachineryService {

    @Resource
    private MesDvMachineryMapper machineryMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private MesDvMachineryTypeService machineryTypeService;
    @Resource
    @Lazy
    private MesMdWorkshopService workshopService;
    @Resource
    private MesWmBarcodeService barcodeService;
    @Resource
    @Lazy
    private MesDvCheckPlanMachineryService checkPlanMachineryService;
    @Resource
    @Lazy
    private MesDvCheckRecordService checkRecordService;
    @Resource
    @Lazy
    private MesDvMaintenRecordService maintenRecordService;
    @Resource
    @Lazy
    private MesDvRepairService repairService;

    @Override
    public Long createMachinery(MesDvMachinerySaveReqVO createReqVO) {
        // 校验设备类型存在
        machineryTypeService.getMachineryType(createReqVO.getMachineryTypeId());
        // 校验车间存在
        workshopService.getWorkshop(createReqVO.getWorkshopId());
        // 校验编码唯一
        validateMachineryCodeUnique(null, createReqVO.getCode());

        // 插入
        MesDvMachineryDO machinery = BeanUtils.toBean(createReqVO, MesDvMachineryDO.class);
        machineryMapper.insert(machinery);

        // 自动生成条码
        barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.MACHINERY.getValue(),
                machinery.getId(), machinery.getCode(), machinery.getName());
        return machinery.getId();
    }

    @Override
    public void updateMachinery(MesDvMachinerySaveReqVO updateReqVO) {
        // 校验存在
        validateMachineryExists(updateReqVO.getId());
        // 校验设备类型存在
        machineryTypeService.getMachineryType(updateReqVO.getMachineryTypeId());
        // 校验车间存在
        workshopService.getWorkshop(updateReqVO.getWorkshopId());
        // 校验编码唯一
        validateMachineryCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        MesDvMachineryDO updateObj = BeanUtils.toBean(updateReqVO, MesDvMachineryDO.class);
        machineryMapper.updateById(updateObj);
    }

    @Override
    public void deleteMachinery(Long id) {
        // 校验存在
        validateMachineryExists(id);
        // 校验关联数据
        if (checkPlanMachineryService.getCheckPlanMachineryCountByMachineryId(id) > 0) {
            throw exception(DV_MACHINERY_HAS_CHECK_PLAN);
        }
        if (checkRecordService.getCheckRecordCountByMachineryId(id) > 0) {
            throw exception(DV_MACHINERY_HAS_CHECK_RECORD);
        }
        if (maintenRecordService.getMaintenRecordCountByMachineryId(id) > 0) {
            throw exception(DV_MACHINERY_HAS_MAINTEN_RECORD);
        }
        if (repairService.getRepairCountByMachineryId(id) > 0) {
            throw exception(DV_MACHINERY_HAS_REPAIR);
        }

        // 删除
        machineryMapper.deleteById(id);
    }

    @Override
    public void validateMachineryExists(Long id) {
        if (machineryMapper.selectById(id) == null) {
            throw exception(DV_MACHINERY_NOT_EXISTS);
        }
    }

    private void validateMachineryCodeUnique(Long id, String code) {
        if (code == null) {
            return;
        }
        MesDvMachineryDO machinery = machineryMapper.selectByCode(code);
        if (machinery == null) {
            return;
        }
        if (ObjUtil.notEqual(machinery.getId(), id)) {
            throw exception(DV_MACHINERY_CODE_DUPLICATE);
        }
    }

    @Override
    public MesDvMachineryDO getMachinery(Long id) {
        return machineryMapper.selectById(id);
    }

    @Override
    public PageResult<MesDvMachineryDO> getMachineryPage(MesDvMachineryPageReqVO pageReqVO) {
        // 处理设备类型树形查询：选择父类型时，同时查询所有子类型下的设备
        if (pageReqVO.getMachineryTypeId() != null) {
            List<MesDvMachineryTypeDO> children = machineryTypeService.getMachineryTypeChildrenList(
                    pageReqVO.getMachineryTypeId());
            Set<Long> typeIds = new HashSet<>();
            typeIds.add(pageReqVO.getMachineryTypeId());
            children.forEach(child -> typeIds.add(child.getId()));
            pageReqVO.setMachineryTypeIds(typeIds);
        }
        return machineryMapper.selectPage(pageReqVO);
    }

    @Override
    public Long getMachineryCountByMachineryTypeId(Long machineryTypeId) {
        return machineryMapper.selectCountByMachineryTypeId(machineryTypeId);
    }

    @Override
    public void updateMachineryLastCheckTime(Long machineryId, LocalDateTime lastCheckTime) {
        machineryMapper.updateById(new MesDvMachineryDO().setId(machineryId).setLastCheckTime(lastCheckTime));
    }

    @Override
    public void updateMachineryLastMaintenTime(Long machineryId, LocalDateTime lastMaintenTime) {
        machineryMapper.updateById(new MesDvMachineryDO().setId(machineryId).setLastMaintenTime(lastMaintenTime));
    }

    @Override
    public List<MesDvMachineryDO> getMachineryList() {
        return machineryMapper.selectList();
    }

    @Override
    public List<MesDvMachineryDO> getMachineryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return machineryMapper.selectByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MesDvMachineryImportRespVO importMachineryList(List<MesDvMachineryImportExcelVO> importMachineryList,
                                                          boolean updateSupport) {
        // 1. 参数校验
        if (CollUtil.isEmpty(importMachineryList)) {
            throw exception(DV_MACHINERY_IMPORT_LIST_IS_EMPTY);
        }

        // 2. 批量加载设备类型和车间，构建编码到实体的映射
        List<MesDvMachineryTypeDO> allTypes = machineryTypeService.getMachineryTypeList(
                new cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.type.MesDvMachineryTypeListReqVO());
        Map<String, MesDvMachineryTypeDO> typeCodeMap = allTypes.stream()
                .collect(Collectors.toMap(MesDvMachineryTypeDO::getCode, t -> t, (a, b) -> a));
        List<MesMdWorkshopDO> allWorkshops = workshopService.getWorkshopListByStatus(
                cn.iocoder.yudao.framework.common.enums.CommonStatusEnum.ENABLE.getStatus());
        Map<String, MesMdWorkshopDO> workshopCodeMap = allWorkshops.stream()
                .collect(Collectors.toMap(MesMdWorkshopDO::getCode, w -> w, (a, b) -> a));

        // 3. 遍历，逐个创建 or 更新
        MesDvMachineryImportRespVO respVO = MesDvMachineryImportRespVO.builder()
                .createCodes(new ArrayList<>()).updateCodes(new ArrayList<>())
                .failureCodes(new LinkedHashMap<>()).build();
        AtomicInteger index = new AtomicInteger(1);
        importMachineryList.forEach(importItem -> {
            int currentIndex = index.getAndIncrement();
            // 3.1 校验必填字段
            String key = StrUtil.blankToDefault(importItem.getCode(), "第 " + currentIndex + " 行");
            if (StrUtil.isBlank(importItem.getCode())) {
                respVO.getFailureCodes().put(key, "设备编码不能为空");
                return;
            }
            if (StrUtil.isBlank(importItem.getName())) {
                respVO.getFailureCodes().put(key, "设备名称不能为空");
                return;
            }
            // 3.2 校验设备类型编码
            if (StrUtil.isBlank(importItem.getMachineryTypeCode())) {
                respVO.getFailureCodes().put(key, "设备类型编码不能为空");
                return;
            }
            MesDvMachineryTypeDO machineryType = typeCodeMap.get(importItem.getMachineryTypeCode());
            if (machineryType == null) {
                respVO.getFailureCodes().put(key, "设备类型编码[" + importItem.getMachineryTypeCode() + "]不存在");
                return;
            }
            // 3.3 校验车间编码
            if (StrUtil.isBlank(importItem.getWorkshopCode())) {
                respVO.getFailureCodes().put(key, "车间编码不能为空");
                return;
            }
            MesMdWorkshopDO workshop = workshopCodeMap.get(importItem.getWorkshopCode());
            if (workshop == null) {
                respVO.getFailureCodes().put(key, "车间编码[" + importItem.getWorkshopCode() + "]不存在");
                return;
            }

            // 3.4 判断：创建 or 更新
            MesDvMachineryDO existMachinery = machineryMapper.selectByCode(importItem.getCode());
            if (existMachinery == null) {
                // 3.4.1 创建
                MesDvMachineryDO machinery = BeanUtils.toBean(importItem, MesDvMachineryDO.class);
                machinery.setMachineryTypeId(machineryType.getId());
                machinery.setWorkshopId(workshop.getId());
                machineryMapper.insert(machinery);
                // 自动生成条码
                barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.MACHINERY.getValue(),
                        machinery.getId(), machinery.getCode(), machinery.getName());
                respVO.getCreateCodes().add(importItem.getCode());
            } else if (updateSupport) {
                // 3.4.2 更新
                MesDvMachineryDO updateObj = BeanUtils.toBean(importItem, MesDvMachineryDO.class);
                updateObj.setId(existMachinery.getId());
                updateObj.setMachineryTypeId(machineryType.getId());
                updateObj.setWorkshopId(workshop.getId());
                machineryMapper.updateById(updateObj);
                respVO.getUpdateCodes().add(importItem.getCode());
            } else {
                // 不支持更新
                respVO.getFailureCodes().put(key, "设备编码已存在");
            }
        });
        return respVO;
    }

}
