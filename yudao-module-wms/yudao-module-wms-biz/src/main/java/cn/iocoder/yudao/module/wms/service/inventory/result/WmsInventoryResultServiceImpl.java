package cn.iocoder.yudao.module.wms.service.inventory.result;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.vo.WmsInventoryResultPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.result.vo.WmsInventoryResultSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.result.WmsInventoryResultDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.result.WmsInventoryResultMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 库存盘点结果 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInventoryResultServiceImpl implements WmsInventoryResultService {

    @Resource
    private WmsInventoryResultMapper inventoryResultMapper;

    @Override
    public Long createInventoryResult(WmsInventoryResultSaveReqVO createReqVO) {
        // 插入
        WmsInventoryResultDO inventoryResult = BeanUtils.toBean(createReqVO, WmsInventoryResultDO.class);
        inventoryResultMapper.insert(inventoryResult);
        // 返回
        return inventoryResult.getId();
    }

    @Override
    public void updateInventoryResult(WmsInventoryResultSaveReqVO updateReqVO) {
        // 校验存在
        validateInventoryResultExists(updateReqVO.getId());
        // 更新
        WmsInventoryResultDO updateObj = BeanUtils.toBean(updateReqVO, WmsInventoryResultDO.class);
        inventoryResultMapper.updateById(updateObj);
    }

    @Override
    public void deleteInventoryResult(Long id) {
        // 校验存在
        validateInventoryResultExists(id);
        // 删除
        inventoryResultMapper.deleteById(id);
    }

    private void validateInventoryResultExists(Long id) {
        if (inventoryResultMapper.selectById(id) == null) {
//            throw exception(INVENTORY_RESULT_NOT_EXISTS);
        }
    }

    @Override
    public WmsInventoryResultDO getInventoryResult(Long id) {
        return inventoryResultMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryResultDO> getInventoryResultPage(WmsInventoryResultPageReqVO pageReqVO) {
        return inventoryResultMapper.selectPage(pageReqVO);
    }

}