package cn.iocoder.yudao.module.mes.service.wm.warehouse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.area.MesWmWarehouseAreaPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.area.MesWmWarehouseAreaSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.warehouse.MesWmWarehouseAreaMapper;
import cn.iocoder.yudao.module.mes.enums.wm.BarcodeBizTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.wm.barcode.MesWmBarcodeService;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 库位 Service 实现类
 */
@Service
@Validated
public class MesWmWarehouseAreaServiceImpl implements MesWmWarehouseAreaService {

    @Resource
    private MesWmWarehouseAreaMapper areaMapper;

    @Resource
    @Lazy
    private MesMdWorkstationService workstationService;
    @Resource
    @Lazy
    private MesWmMaterialStockService materialStockService;
    @Resource
    @Lazy
    private MesWmWarehouseLocationService locationService;
    @Resource
    private MesWmBarcodeService barcodeService;

    @Override
    public Long createWarehouseArea(MesWmWarehouseAreaSaveReqVO createReqVO) {
        // 校验虚拟库位不允许新增
        validateNotVirtual(createReqVO.getCode());
        // 校验数据
        validateWarehouseAreaSaveData(createReqVO);

        // 插入
        MesWmWarehouseAreaDO area = BeanUtils.toBean(createReqVO, MesWmWarehouseAreaDO.class);
        areaMapper.insert(area);

        // 自动生成条码
        barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.AREA.getValue(),
                area.getId(), area.getCode(), area.getName());
        return area.getId();
    }

    @Override
    public void updateWarehouseArea(MesWmWarehouseAreaSaveReqVO updateReqVO) {
        // 校验存在
        MesWmWarehouseAreaDO area = validateWarehouseAreaExists(updateReqVO.getId());
        // 校验虚拟库位不允许修改
        validateNotVirtual(area.getCode());
        // 校验数据
        validateWarehouseAreaSaveData(updateReqVO);

        // 更新
        MesWmWarehouseAreaDO updateObj = BeanUtils.toBean(updateReqVO, MesWmWarehouseAreaDO.class);
        areaMapper.updateById(updateObj);
    }

    private void validateWarehouseAreaSaveData(MesWmWarehouseAreaSaveReqVO reqVO) {
        // 校验库区存在
        locationService.validateWarehouseLocationExists(reqVO.getLocationId());
        // 校验编码唯一
        validateWarehouseAreaCodeUnique(reqVO.getId(), reqVO.getLocationId(), reqVO.getCode());
        // 校验名称唯一
        validateWarehouseAreaNameUnique(reqVO.getId(), reqVO.getLocationId(), reqVO.getName());
    }

    private void validateNotVirtual(String code) {
        if (MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA.equals(code)) {
            throw exception(WM_WAREHOUSE_AREA_IS_VIRTUAL);
        }
    }

    @Override
    public void deleteWarehouseArea(Long id) {
        // 校验存在
        MesWmWarehouseAreaDO area = validateWarehouseAreaExists(id);
        // 校验虚拟库位不允许删除
        validateNotVirtual(area.getCode());
        // 校验是否被工作站引用
        if (workstationService.getWorkstationCountByAreaId(id) > 0) {
            throw exception(WM_WAREHOUSE_AREA_HAS_WORKSTATION);
        }
        // 校验是否有库存记录
        if (materialStockService.getMaterialStockCountByAreaId(id) > 0) {
            throw exception(WM_WAREHOUSE_AREA_HAS_MATERIAL_STOCK);
        }

        // 删除
        areaMapper.deleteById(id);
    }

    @Override
    public MesWmWarehouseAreaDO validateWarehouseAreaExists(Long id) {
        MesWmWarehouseAreaDO area = areaMapper.selectById(id);
        if (area == null) {
            throw exception(WM_WAREHOUSE_AREA_NOT_EXISTS);
        }
        return area;
    }

    private void validateWarehouseAreaCodeUnique(Long id, Long locationId, String code) {
        MesWmWarehouseAreaDO area = areaMapper.selectByCode(locationId, code);
        if (area == null) {
            return;
        }
        if (ObjUtil.notEqual(area.getId(), id)) {
            throw exception(WM_WAREHOUSE_AREA_CODE_DUPLICATE);
        }
    }

    private void validateWarehouseAreaNameUnique(Long id, Long locationId, String name) {
        MesWmWarehouseAreaDO area = areaMapper.selectByName(locationId, name);
        if (area == null) {
            return;
        }
        if (ObjUtil.notEqual(area.getId(), id)) {
            throw exception(WM_WAREHOUSE_AREA_NAME_DUPLICATE);
        }
    }

    @Override
    public MesWmWarehouseAreaDO getWarehouseArea(Long id) {
        return areaMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmWarehouseAreaDO> getWarehouseAreaPage(MesWmWarehouseAreaPageReqVO pageReqVO) {
        return areaMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWmWarehouseAreaDO> getWarehouseAreaList(Long locationId) {
        return areaMapper.selectSimpleList(locationId);
    }

    @Override
    public List<MesWmWarehouseAreaDO> getWarehouseAreaList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return areaMapper.selectListByIds(ids);
    }

    @Override
    public Long getWarehouseAreaCountByLocationId(Long locationId) {
        return areaMapper.selectCountByLocationId(locationId);
    }

    @Override
    public void validateWarehouseAreaExists(Long warehouseId, Long locationId, Long areaId) {
        if (areaId == null) {
            return;
        }
        // 1.1 校验库位存在
        MesWmWarehouseAreaDO area = validateWarehouseAreaExists(areaId);
        // 1.2 校验库位所属的库区是否匹配
        if (locationId != null && ObjUtil.notEqual(area.getLocationId(), locationId)) {
            throw exception(WM_WAREHOUSE_AREA_RELATION_INVALID);
        }
        // 2. 校验库区存在
        if (locationId != null) {
            locationService.validateWarehouseLocationExists(locationId);
        }
        // 3. 校验仓库存在（通过库区的 locationId 关联查询）
        if (warehouseId != null) {
            MesWmWarehouseLocationDO location = locationService.validateWarehouseLocationExists(area.getLocationId());
            if (location != null && ObjUtil.notEqual(location.getWarehouseId(), warehouseId)) {
                throw exception(WM_WAREHOUSE_AREA_WAREHOUSE_MISMATCH);
            }
        }
    }

    @Override
    public void updateByLocationId(Long locationId, Boolean allowItemMixing, Boolean allowBatchMixing) {
        // 校验库区存在
        locationService.validateWarehouseLocationExists(locationId);
        // 构建更新对象
        MesWmWarehouseAreaDO updateObj = new MesWmWarehouseAreaDO();
        if (allowItemMixing != null) {
            updateObj.setAllowItemMixing(allowItemMixing);
        }
        if (allowBatchMixing != null) {
            updateObj.setAllowBatchMixing(allowBatchMixing);
        }
        // 批量更新库位
        areaMapper.updateByLocationId(locationId, updateObj);
    }

    @Override
    public MesWmWarehouseAreaDO getWarehouseAreaByCode(String code) {
        // 1. 查询库位，存在则直接返回
        MesWmWarehouseAreaDO area = areaMapper.selectByCode(code);
        if (area != null) {
            return area;
        }

        // 2. 如果是虚拟线边库位编码，则自动初始化
        if (MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA.equals(code)) {
            // 2.1 先确保虚拟库区存在（getWarehouseLocationByCode 会级联创建仓库 + 库区）
            MesWmWarehouseLocationDO location = locationService.getWarehouseLocationByCode(
                    MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION);
            Assert.notNull(location, "虚拟库区必须存在");
            // 2.2 自动初始化
            MesWmWarehouseAreaDO newArea = MesWmWarehouseAreaDO.builder()
                    .locationId(location.getId()).code(code).name("虚拟线边库位")
                    .status(CommonStatusEnum.ENABLE.getStatus()).frozen(false)
                    .allowItemMixing(true).allowBatchMixing(true)
                    .remark("系统自动初始化的虚拟线边库位")
                    .build();
            areaMapper.insert(newArea);
            return newArea;
        }
        return null;
    }

}
