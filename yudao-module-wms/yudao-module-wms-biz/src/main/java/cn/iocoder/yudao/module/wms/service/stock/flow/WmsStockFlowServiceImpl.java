package cn.iocoder.yudao.module.wms.service.stock.flow;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.flow.WmsStockFlowMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 库存流水 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockFlowServiceImpl implements WmsStockFlowService {

    @Resource
    private WmsStockFlowMapper stockFlowMapper;

    /**
     * @sign : 5CDC0A12A8B023F4
     */
    @Override
    public WmsStockFlowDO createStockFlow(WmsStockFlowSaveReqVO createReqVO) {
        // 插入
        WmsStockFlowDO stockFlow = BeanUtils.toBean(createReqVO, WmsStockFlowDO.class);
        stockFlowMapper.insert(stockFlow);
        // 返回
        return stockFlow;
    }

    /**
     * @sign : 92744785BC7A4404
     */
    @Override
    public WmsStockFlowDO updateStockFlow(WmsStockFlowSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockFlowDO exists = validateStockFlowExists(updateReqVO.getId());
        // 更新
        WmsStockFlowDO stockFlow = BeanUtils.toBean(updateReqVO, WmsStockFlowDO.class);
        stockFlowMapper.updateById(stockFlow);
        // 返回
        return stockFlow;
    }

    /**
     * @sign : 2C67EEBE8FF6B925
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockFlow(Long id) {
        // 校验存在
        WmsStockFlowDO stockFlow = validateStockFlowExists(id);
        // 删除
        stockFlowMapper.deleteById(id);
    }

    /**
     * @sign : 9FD17EF243CE9F52
     */
    private WmsStockFlowDO validateStockFlowExists(Long id) {
        WmsStockFlowDO stockFlow = stockFlowMapper.selectById(id);
        if (stockFlow == null) {
            throw exception(STOCK_FLOW_NOT_EXISTS);
        }
        return stockFlow;
    }

    @Override
    public WmsStockFlowDO getStockFlow(Long id) {
        return stockFlowMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockFlowDO> getStockFlowPage(WmsStockFlowPageReqVO pageReqVO) {
        return stockFlowMapper.selectPage(pageReqVO);
    }
}