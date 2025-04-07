package cn.iocoder.yudao.module.wms.service.inventory.result.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.item.vo.WmsInventoryResultItemPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.item.vo.WmsInventoryResultItemSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result.item.WmsInventoryResultItemDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.result.item.WmsInventoryResultItemMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 库存盘点结果详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInventoryResultItemServiceImpl implements WmsInventoryResultItemService {

    @Resource
    private WmsInventoryResultItemMapper inventoryResultItemMapper;

    @Override
    public Long createInventoryResultItem(WmsInventoryResultItemSaveReqVO createReqVO) {
        // 插入
        WmsInventoryResultItemDO inventoryResultItem = BeanUtils.toBean(createReqVO, WmsInventoryResultItemDO.class);
        inventoryResultItemMapper.insert(inventoryResultItem);
        // 返回
        return inventoryResultItem.getId();
    }

    @Override
    public void updateInventoryResultItem(WmsInventoryResultItemSaveReqVO updateReqVO) {
        // 校验存在
        validateInventoryResultItemExists(updateReqVO.getId());
        // 更新
        WmsInventoryResultItemDO updateObj = BeanUtils.toBean(updateReqVO, WmsInventoryResultItemDO.class);
        inventoryResultItemMapper.updateById(updateObj);
    }

    @Override
    public void deleteInventoryResultItem(Long id) {
        // 校验存在
        validateInventoryResultItemExists(id);
        // 删除
        inventoryResultItemMapper.deleteById(id);
    }

    private void validateInventoryResultItemExists(Long id) {
        if (inventoryResultItemMapper.selectById(id) == null) {
//            throw exception(INVENTORY_RESULT_ITEM_NOT_EXISTS);
        }
    }

    @Override
    public WmsInventoryResultItemDO getInventoryResultItem(Long id) {
        return inventoryResultItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryResultItemDO> getInventoryResultItemPage(WmsInventoryResultItemPageReqVO pageReqVO) {
        return inventoryResultItemMapper.selectPage(pageReqVO);
    }

}