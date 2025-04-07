package cn.iocoder.yudao.module.wms.service.stock.bin.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move.WmsStockBinMoveMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 库位移动 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockBinMoveServiceImpl implements WmsStockBinMoveService {

    @Resource
    private WmsStockBinMoveMapper stockBinMoveMapper;

    @Override
    public Long createStockBinMove(WmsStockBinMoveSaveReqVO createReqVO) {
        // 插入
        WmsStockBinMoveDO stockBinMove = BeanUtils.toBean(createReqVO, WmsStockBinMoveDO.class);
        stockBinMoveMapper.insert(stockBinMove);
        // 返回
        return stockBinMove.getId();
    }

    @Override
    public void updateStockBinMove(WmsStockBinMoveSaveReqVO updateReqVO) {
        // 校验存在
        validateStockBinMoveExists(updateReqVO.getId());
        // 更新
        WmsStockBinMoveDO updateObj = BeanUtils.toBean(updateReqVO, WmsStockBinMoveDO.class);
        stockBinMoveMapper.updateById(updateObj);
    }

    @Override
    public void deleteStockBinMove(Long id) {
        // 校验存在
        validateStockBinMoveExists(id);
        // 删除
        stockBinMoveMapper.deleteById(id);
    }

    private void validateStockBinMoveExists(Long id) {
        if (stockBinMoveMapper.selectById(id) == null) {
//            throw exception(STOCK_BIN_MOVE_NOT_EXISTS);
        }
    }

    @Override
    public WmsStockBinMoveDO getStockBinMove(Long id) {
        return stockBinMoveMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockBinMoveDO> getStockBinMovePage(WmsStockBinMovePageReqVO pageReqVO) {
        return stockBinMoveMapper.selectPage(pageReqVO);
    }

}