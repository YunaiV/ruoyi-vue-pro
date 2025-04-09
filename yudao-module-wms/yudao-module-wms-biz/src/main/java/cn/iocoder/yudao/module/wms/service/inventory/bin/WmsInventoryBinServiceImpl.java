package cn.iocoder.yudao.module.wms.service.inventory.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.bin.WmsInventoryBinMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 库位盘点 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInventoryBinServiceImpl implements WmsInventoryBinService {

    @Resource
    private WmsInventoryBinMapper inventoryBinMapper;

    @Override
    public Long createInventoryBin(WmsInventoryBinSaveReqVO createReqVO) {
        // 插入
        WmsInventoryBinDO inventoryBin = BeanUtils.toBean(createReqVO, WmsInventoryBinDO.class);
        inventoryBinMapper.insert(inventoryBin);
        // 返回
        return inventoryBin.getId();
    }

    @Override
    public void updateInventoryBin(WmsInventoryBinSaveReqVO updateReqVO) {
        // 校验存在
        validateInventoryBinExists(updateReqVO.getId());
        // 更新
        WmsInventoryBinDO updateObj = BeanUtils.toBean(updateReqVO, WmsInventoryBinDO.class);
        inventoryBinMapper.updateById(updateObj);
    }

    @Override
    public void deleteInventoryBin(Long id) {
        // 校验存在
        validateInventoryBinExists(id);
        // 删除
        inventoryBinMapper.deleteById(id);
    }

    private void validateInventoryBinExists(Long id) {
        if (inventoryBinMapper.selectById(id) == null) {
            //throw exception(INVENTORY_BIN_NOT_EXISTS);
        }
    }

    @Override
    public WmsInventoryBinDO getInventoryBin(Long id) {
        return inventoryBinMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryBinDO> getInventoryBinPage(WmsInventoryBinPageReqVO pageReqVO) {
        return inventoryBinMapper.selectPage(pageReqVO);
    }

}