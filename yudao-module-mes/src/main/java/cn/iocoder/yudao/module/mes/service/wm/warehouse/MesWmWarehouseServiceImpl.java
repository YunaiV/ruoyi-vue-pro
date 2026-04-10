package cn.iocoder.yudao.module.mes.service.wm.warehouse;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.MesWmWarehousePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.MesWmWarehouseSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.warehouse.MesWmWarehouseMapper;
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
 * MES 仓库 Service 实现类
 */
@Service
@Validated
public class MesWmWarehouseServiceImpl implements MesWmWarehouseService {

    @Resource
    private MesWmWarehouseMapper warehouseMapper;

    @Resource
    private MesWmWarehouseLocationService locationService;
    @Resource
    @Lazy
    private MesMdWorkstationService workstationService;
    @Resource
    private MesWmMaterialStockService materialStockService;
    @Resource
    private MesWmBarcodeService barcodeService;

    @Override
    public Long createWarehouse(MesWmWarehouseSaveReqVO createReqVO) {
        // 校验虚拟仓库不允许新增
        validateNotVirtual(createReqVO.getCode());
        // 校验数据
        validateWarehouseSaveData(createReqVO);

        // 插入
        MesWmWarehouseDO warehouse = BeanUtils.toBean(createReqVO, MesWmWarehouseDO.class);
        warehouseMapper.insert(warehouse);

        // 自动生成条码
        barcodeService.autoGenerateBarcode(BarcodeBizTypeEnum.WAREHOUSE.getValue(),
                warehouse.getId(), warehouse.getCode(), warehouse.getName());
        return warehouse.getId();
    }

    @Override
    public void updateWarehouse(MesWmWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        MesWmWarehouseDO warehouse = validateWarehouseExists(updateReqVO.getId());
        // 校验虚拟仓库不允许修改
        validateNotVirtual(warehouse.getCode());
        // 校验数据
        validateWarehouseSaveData(updateReqVO);

        // 更新
        MesWmWarehouseDO updateObj = BeanUtils.toBean(updateReqVO, MesWmWarehouseDO.class);
        warehouseMapper.updateById(updateObj);
    }

    private void validateWarehouseSaveData(MesWmWarehouseSaveReqVO reqVO) {
        // 校验编码唯一
        validateWarehouseCodeUnique(reqVO.getId(), reqVO.getCode());
        // 校验名称唯一
        validateWarehouseNameUnique(reqVO.getId(), reqVO.getName());
    }

    private void validateNotVirtual(String code) {
        if (MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE.equals(code)) {
            throw exception(WM_WAREHOUSE_IS_VIRTUAL);
        }
    }

    @Override
    public void deleteWarehouse(Long id) {
        // 校验存在
        MesWmWarehouseDO warehouse = validateWarehouseExists(id);
        // 校验虚拟仓库不允许删除
        validateNotVirtual(warehouse.getCode());
        // 校验是否有库区
        if (locationService.getWarehouseLocationCountByWarehouseId(id) > 0) {
            throw exception(WM_WAREHOUSE_HAS_LOCATION);
        }
        // 校验是否被工作站引用
        if (workstationService.getWorkstationCountByWarehouseId(id) > 0) {
            throw exception(WM_WAREHOUSE_HAS_WORKSTATION);
        }
        // 校验是否有库存记录
        if (materialStockService.getMaterialStockCountByWarehouseId(id) > 0) {
            throw exception(WM_WAREHOUSE_HAS_MATERIAL_STOCK);
        }

        // 删除
        warehouseMapper.deleteById(id);
    }

    @Override
    public MesWmWarehouseDO validateWarehouseExists(Long id) {
        MesWmWarehouseDO warehouse = warehouseMapper.selectById(id);
        if (warehouse == null) {
            throw exception(WM_WAREHOUSE_NOT_EXISTS);
        }
        return warehouse;
    }

    private void validateWarehouseCodeUnique(Long id, String code) {
        MesWmWarehouseDO warehouse = warehouseMapper.selectByCode(code);
        if (warehouse == null) {
            return;
        }
        if (ObjUtil.notEqual(warehouse.getId(), id)) {
            throw exception(WM_WAREHOUSE_CODE_DUPLICATE);
        }
    }

    private void validateWarehouseNameUnique(Long id, String name) {
        MesWmWarehouseDO warehouse = warehouseMapper.selectByName(name);
        if (warehouse == null) {
            return;
        }
        if (ObjUtil.notEqual(warehouse.getId(), id)) {
            throw exception(WM_WAREHOUSE_NAME_DUPLICATE);
        }
    }

    @Override
    public MesWmWarehouseDO getWarehouse(Long id) {
        return warehouseMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmWarehouseDO> getWarehousePage(MesWmWarehousePageReqVO pageReqVO) {
        return warehouseMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWmWarehouseDO> getWarehouseList() {
        return warehouseMapper.selectSimpleList();
    }

    @Override
    public List<MesWmWarehouseDO> getWarehouseList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return warehouseMapper.selectListByIds(ids);
    }

    @Override
    public MesWmWarehouseDO getWarehouseByCode(String code) {
        // 1. 查询仓库，存在则直接返回
        MesWmWarehouseDO warehouse = warehouseMapper.selectByCode(code);
        if (warehouse != null) {
            return warehouse;
        }

        // 2. 如果是虚拟线边库编码，则自动初始化
        if (MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE.equals(code)) {
            MesWmWarehouseDO newWarehouse = MesWmWarehouseDO.builder()
                    .code(code).name("虚拟线边仓库").frozen(false)
                    .remark("系统自动初始化的虚拟线边仓库（用于生产报工与在制品管理解耦）")
                    .build();
            warehouseMapper.insert(newWarehouse);
            return newWarehouse;
        }
        return null;
    }

}
