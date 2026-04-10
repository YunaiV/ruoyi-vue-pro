package cn.iocoder.yudao.module.mes.service.md.workstation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.MesMdWorkstationPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.MesMdWorkstationSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkshopDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.workstation.MesMdWorkstationMapper;
import cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 工作站 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdWorkstationServiceImpl implements MesMdWorkstationService {

    @Resource
    private MesMdWorkstationMapper workstationMapper;

    @Resource
    private MesMdWorkstationMachineService workstationMachineService;
    @Resource
    private MesMdWorkstationToolService workstationToolService;
    @Resource
    private MesMdWorkstationWorkerService workstationWorkerService;

    @Resource
    private MesMdWorkshopService workshopService;
    @Resource
    private MesWmWarehouseService warehouseService;
    @Resource
    private MesWmWarehouseLocationService locationService;
    @Resource
    private MesWmWarehouseAreaService areaService;
    @Resource
    private MesWmBarcodeService barcodeService;
    @Resource
    private MesProProcessService processService;

    @Override
    public Long createWorkstation(MesMdWorkstationSaveReqVO createReqVO) {
        // 校验数据
        validateWorkstationSaveData(null, createReqVO);

        // 插入
        MesMdWorkstationDO workstation = BeanUtils.toBean(createReqVO, MesMdWorkstationDO.class);
        workstationMapper.insert(workstation);

        // 自动生成条码
        barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.WORKSTATION.getValue(),
                workstation.getId(), workstation.getCode(), workstation.getName());
        return workstation.getId();
    }

    @Override
    public void updateWorkstation(MesMdWorkstationSaveReqVO updateReqVO) {
        // 校验存在
        validateWorkstationExists(updateReqVO.getId());
        // 校验数据
        validateWorkstationSaveData(updateReqVO.getId(), updateReqVO);

        // 更新
        MesMdWorkstationDO updateObj = BeanUtils.toBean(updateReqVO, MesMdWorkstationDO.class);
        workstationMapper.updateById(updateObj);
    }

    private void validateWorkstationSaveData(Long id, MesMdWorkstationSaveReqVO reqVO) {
        // 校验编码唯一
        validateWorkstationCodeUnique(id, reqVO.getCode());
        // 校验名称唯一
        validateWorkstationNameUnique(id, reqVO.getName());
        // 校验车间存在
        validateWorkshopExists(reqVO.getWorkshopId());
        // 校验工序存在
        processService.validateProcessExists(reqVO.getProcessId());
        // 处理仓库层级（未指定仓库时自动设置虚拟线边库）
        handleWarehouseHierarchy(reqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkstation(Long id) {
        // 校验存在
        validateWorkstationExists(id);

        // 级联删除子资源
        workstationMachineService.deleteWorkstationMachineByWorkstationId(id);
        workstationToolService.deleteWorkstationToolByWorkstationId(id);
        workstationWorkerService.deleteWorkstationWorkerByWorkstationId(id);
        // 删除工作站
        workstationMapper.deleteById(id);
    }

    @Override
    public MesMdWorkstationDO validateWorkstationExists(Long id) {
        MesMdWorkstationDO workstation = workstationMapper.selectById(id);
        if (workstation == null) {
            throw exception(MD_WORKSTATION_NOT_EXISTS);
        }
        return workstation;
    }

    private void validateWorkshopExists(Long workshopId) {
        MesMdWorkshopDO workshop = workshopService.getWorkshop(workshopId);
        if (workshop == null) {
            throw exception(MD_WORKSHOP_NOT_EXISTS);
        }
    }

    /**
     * 校验并处理仓库层级关系
     *
     * 仓库层级结构：仓库 -> 库区 -> 库位
     * 处理规则：
     * 1. 如果仓库/库区/库位都未填写，则自动设置虚拟线边库
     * 2. 如果填写了库区，必须填写仓库，且库区必须属于该仓库
     * 3. 如果填写了库位，必须填写库区，且库位必须属于该库区
     *
     * @param reqVO 工作站保存请求对象
     */
    private void handleWarehouseHierarchy(MesMdWorkstationSaveReqVO reqVO) {
        // 1.1 获取仓库、库区、库位 ID
        Long warehouseId = reqVO.getWarehouseId();
        Long locationId = reqVO.getLocationId();
        Long areaId = reqVO.getAreaId();
        // 1.2 如果都为空，则自动设置虚拟线边库
        if (warehouseId == null && locationId == null && areaId == null) {
            MesWmWarehouseDO warehouse = warehouseService.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE);
            MesWmWarehouseLocationDO location = locationService.getWarehouseLocationByCode(
                    MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION);
            MesWmWarehouseAreaDO area = areaService.getWarehouseAreaByCode(
                    MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA);
            reqVO.setWarehouseId(warehouse.getId());
            reqVO.setLocationId(location.getId());
            reqVO.setAreaId(area.getId());
            return;
        }

        // 2. 校验仓库是否存在
        if (warehouseId != null) {
            warehouseService.validateWarehouseExists(warehouseId);
        }

        // 3. 校验库区：如果填写了库区，必须填写仓库，且库区必须属于该仓库
        MesWmWarehouseLocationDO location;
        if (locationId != null) {
            // 3.1 校验库区是否存在
            location = locationService.validateWarehouseLocationExists(locationId);
            // 3.2 校验必须填写仓库
            if (warehouseId == null) {
                throw exception(WM_WAREHOUSE_REQUIRED);
            }
            // 3.3 校验库区是否属于该仓库
            if (ObjUtil.notEqual(location.getWarehouseId(), warehouseId)) {
                throw exception(WM_WAREHOUSE_LOCATION_RELATION_INVALID);
            }
        }

        // 4. 校验库位：如果填写了库位，必须填写库区，且库位必须属于该库区
        if (areaId == null) {
            return;
        }
        // 4.1 校验库位是否存在
        MesWmWarehouseAreaDO area = areaService.validateWarehouseAreaExists(areaId);
        // 4.2 校验必须填写库区
        if (locationId == null) {
            throw exception(WM_WAREHOUSE_LOCATION_REQUIRED);
        }
        // 4.3 校验库位是否属于该库区
        if (ObjUtil.notEqual(area.getLocationId(), locationId)) {
            throw exception(WM_WAREHOUSE_AREA_RELATION_INVALID);
        }
    }

    private void validateWorkstationCodeUnique(Long id, String code) {
        MesMdWorkstationDO workstation = workstationMapper.selectByCode(code);
        if (workstation == null) {
            return;
        }
        if (ObjUtil.notEqual(workstation.getId(), id)) {
            throw exception(MD_WORKSTATION_CODE_DUPLICATE);
        }
    }

    private void validateWorkstationNameUnique(Long id, String name) {
        MesMdWorkstationDO workstation = workstationMapper.selectByName(name);
        if (workstation == null) {
            return;
        }
        if (ObjUtil.notEqual(workstation.getId(), id)) {
            throw exception(MD_WORKSTATION_NAME_DUPLICATE);
        }
    }

    @Override
    public MesMdWorkstationDO getWorkstation(Long id) {
        return workstationMapper.selectById(id);
    }

    @Override
    public PageResult<MesMdWorkstationDO> getWorkstationPage(MesMdWorkstationPageReqVO pageReqVO) {
        return workstationMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesMdWorkstationDO> getWorkstationListByStatus(Integer status) {
        return workstationMapper.selectListByStatus(status);
    }

    @Override
    public Long getWorkstationCountByWarehouseId(Long warehouseId) {
        return workstationMapper.selectCountByWarehouseId(warehouseId);
    }

    @Override
    public Long getWorkstationCountByLocationId(Long locationId) {
        return workstationMapper.selectCountByLocationId(locationId);
    }

    @Override
    public Long getWorkstationCountByAreaId(Long areaId) {
        return workstationMapper.selectCountByAreaId(areaId);
    }

    @Override
    public List<MesMdWorkstationDO> getWorkstationList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return workstationMapper.selectByIds(ids);
    }

}
