package cn.iocoder.yudao.module.wms.service.inventory;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.history.WmsInventoryHistoryPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryHistoryDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryHistoryMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * WMS 库存流水 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class WmsInventoryHistoryServiceImpl implements WmsInventoryHistoryService {

    @Resource
    private WmsInventoryHistoryMapper inventoryHistoryMapper;

    @Override
    public PageResult<WmsInventoryHistoryDO> getInventoryHistoryPage(WmsInventoryHistoryPageReqVO pageReqVO) {
        return inventoryHistoryMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInventoryHistoryList(List<WmsInventoryHistoryDO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        inventoryHistoryMapper.insertBatch(list);
    }

}
