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

    @Override
    public Long createStockFlow(WmsStockFlowSaveReqVO createReqVO) {
        // 插入
        WmsStockFlowDO stockFlow = BeanUtils.toBean(createReqVO, WmsStockFlowDO.class);
        stockFlowMapper.insert(stockFlow);
        // 返回
        return stockFlow.getId();
    }

    @Override
    public void updateStockFlow(WmsStockFlowSaveReqVO updateReqVO) {
        // 校验存在
        validateStockFlowExists(updateReqVO.getId());
        // 更新
        WmsStockFlowDO updateObj = BeanUtils.toBean(updateReqVO, WmsStockFlowDO.class);
        stockFlowMapper.updateById(updateObj);
    }

    @Override
    public void deleteStockFlow(Long id) {
        // 校验存在
        validateStockFlowExists(id);
        // 删除
        stockFlowMapper.deleteById(id);
    }

    private void validateStockFlowExists(Long id) {
        if (stockFlowMapper.selectById(id) == null) {
            //throw exception(STOCK_FLOW_NOT_EXISTS);
        }
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