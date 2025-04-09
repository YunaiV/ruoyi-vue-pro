package cn.iocoder.yudao.module.wms.service.inventory;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 盘点 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInventoryServiceImpl implements WmsInventoryService {

    @Resource
    private WmsInventoryMapper inventoryMapper;

    @Override
    public Long createInventory(WmsInventorySaveReqVO createReqVO) {
        // 插入
        WmsInventoryDO inventory = BeanUtils.toBean(createReqVO, WmsInventoryDO.class);
        inventoryMapper.insert(inventory);
        // 返回
        return inventory.getId();
    }

    @Override
    public void updateInventory(WmsInventorySaveReqVO updateReqVO) {
        // 校验存在
        validateInventoryExists(updateReqVO.getId());
        // 更新
        WmsInventoryDO updateObj = BeanUtils.toBean(updateReqVO, WmsInventoryDO.class);
        inventoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteInventory(Long id) {
        // 校验存在
        validateInventoryExists(id);
        // 删除
        inventoryMapper.deleteById(id);
    }

    private void validateInventoryExists(Long id) {
        if (inventoryMapper.selectById(id) == null) {
            //throw exception(INVENTORY_NOT_EXISTS);
        }
    }

    @Override
    public WmsInventoryDO getInventory(Long id) {
        return inventoryMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryDO> getInventoryPage(WmsInventoryPageReqVO pageReqVO) {
        return inventoryMapper.selectPage(pageReqVO);
    }

}